package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class SenselessProxyMethodInspector extends BasePhpInspection {
    private static final String messagePattern = "%s% can be dropped, as it only calls parent method";

    @NotNull
    public String getShortName() {
        return "SenselessProxyMethodInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                for (Method objMethod : clazz.getOwnMethods()) {
                    final String methodName = objMethod.getName();
                    if (StringUtil.isEmpty(methodName) || null == objMethod.getNameIdentifier()) {
                        continue;
                    }

                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(objMethod);
                    /*
                        skip processing methods without body (interfaces, abstract and etc.) or
                        contains not expected amount of expressions
                     */
                    if (null == body || 1 != ExpressionSemanticUtil.countExpressionsInGroup(body)) {
                        continue;
                    }

                    final PsiElement lastStatement = ExpressionSemanticUtil.getLastStatement(body);
                    if (null != lastStatement && lastStatement.getFirstChild() instanceof MethodReference) {
                        final MethodReference reference = (MethodReference) lastStatement.getFirstChild();

                        final String referenceVariable = reference.getFirstChild().getText().trim();
                        final String referenceName = reference.getName();

                        if (
                            referenceVariable.equals("parent") &&
                            !StringUtil.isEmpty(referenceName) &&  referenceName.equals(methodName)
                        ) {
                            final Parameter[] methodParameters = objMethod.getParameters();

                            /* ensure no transformations/reordering happens when dispatching parameters */
                            final PsiElement[] givenParams            = reference.getParameters();
                            boolean isDispatchingWithoutModifications = (givenParams.length == methodParameters.length);
                            if (isDispatchingWithoutModifications) {
                                /* ensure parameters re-dispatched in the same order and state */
                                for (int index = 0; index < givenParams.length; ++index) {
                                    if (
                                        !(givenParams[index] instanceof Variable) ||
                                        !((Variable) givenParams[index]).getName().equals(methodParameters[index].getName())
                                    ) {
                                        isDispatchingWithoutModifications = false;
                                        break;
                                    }
                                }
                            }

                            /* ensure no signature changes took place */
                            boolean isChangingSignature          = false;
                            final PsiReference referenceToMethod = reference.getReference();
                            if (null != referenceToMethod && isDispatchingWithoutModifications) {
                                final PsiElement referenceResolved = referenceToMethod.resolve();
                                if (referenceResolved instanceof Method) {
                                    final Method nestedMethod          = (Method) referenceResolved;
                                    final Parameter[] parentParameters = nestedMethod.getParameters();

                                    /* verify amount of parameters, visibility, static, abstract, final */
                                    if (
                                        parentParameters.length   == methodParameters.length &&
                                        nestedMethod.isAbstract() == objMethod.isAbstract() &&
                                        nestedMethod.isStatic()   == objMethod.isStatic() &&
                                        nestedMethod.isFinal()    == objMethod.isFinal() &&
                                        nestedMethod.getAccess().equals(objMethod.getAccess())
                                    ) {
                                        /* when has parameters, ensure the defined order is not changed as well */
                                        if (methodParameters.length > 0) {
                                            for (int index = 0; index < parentParameters.length; ++index) {
                                                if (!PsiEquivalenceUtil.areElementsEquivalent(parentParameters[index], methodParameters[index])) {
                                                    isChangingSignature = true;
                                                    break;
                                                }
                                            }
                                        }
                                    } else {
                                        /* okay obviously changed */
                                        isChangingSignature = true;
                                    }
                                } else {
                                    /* we couldn't resolve parent, so we have no right to report anything */
                                    isChangingSignature = true;
                                }
                            }

                            /* decide if need to report any issues */
                            if (isDispatchingWithoutModifications && !isChangingSignature) {
                                final String message = messagePattern.replace("%s%", objMethod.getName());
                                holder.registerProblem(objMethod.getNameIdentifier(), message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
                            }
                        }
                    }
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Drop it";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement().getParent();
            if (expression instanceof Method) {
                PsiElement nextExpression = expression.getNextSibling();
                if (nextExpression instanceof PsiWhiteSpace) {
                    nextExpression.delete();
                }

                expression.delete();
            }
        }
    }
}
