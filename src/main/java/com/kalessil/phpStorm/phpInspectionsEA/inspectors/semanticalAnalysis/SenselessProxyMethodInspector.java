package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FileSystemUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

public class SenselessProxyMethodInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s%' method can be dropped, as it only calls parent's one.";

    @NotNull
    public String getShortName() {
        return "SenselessProxyMethodInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                if (clazz.isInterface() || clazz.isTrait() || FileSystemUtil.isTestClass(clazz)) {
                    return;
                }

                for (Method method : clazz.getOwnMethods()) {
                    final PsiElement methodNameNode = NamedElementUtil.getNameIdentifier(method);
                    if (null == methodNameNode || method.isAbstract()) {
                        continue;
                    }

                    /* we expect the method to have just one expression - parent invocation */
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                    if (null == body || 1 != ExpressionSemanticUtil.countExpressionsInGroup(body)) {
                        continue;
                    }
                    final PsiElement lastStatement = ExpressionSemanticUtil.getLastStatement(body);
                    if (null == lastStatement || !(lastStatement.getFirstChild() instanceof MethodReference)) {
                        continue;
                    }

                    final MethodReference reference = (MethodReference) lastStatement.getFirstChild();
                    final String referenceVariable  = reference.getFirstChild().getText().trim();
                    final String referenceName      = reference.getName();
                    if (null != referenceName && referenceVariable.equals("parent") && referenceName.equals(method.getName())) {
                        final Parameter[] methodParameters = method.getParameters();

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
                                    nestedMethod.isAbstract() == method.isAbstract() &&
                                    nestedMethod.isStatic()   == method.isStatic() &&
                                    nestedMethod.isFinal()    == method.isFinal() &&
                                    nestedMethod.getAccess().equals(method.getAccess())
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
                                /* we couldn't resolve parent, so we can't report anything */
                                isChangingSignature = true;
                            }
                        }

                        /* decide if need to report any issues */
                        if (isDispatchingWithoutModifications && !isChangingSignature) {
                            final String message = messagePattern.replace("%s%", method.getName());
                            holder.registerProblem(methodNameNode, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
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
                /* delete preceding PhpDoc */
                final PhpPsiElement previous = ((Method) expression).getPrevPsiSibling();
                if (previous instanceof PhpDocComment) {
                    previous.delete();
                }

                /* delete space after the method */
                PsiElement nextExpression = expression.getNextSibling();
                if (nextExpression instanceof PsiWhiteSpace) {
                    nextExpression.delete();
                }

                /* delete proxy itself */
                expression.delete();
            }
        }
    }
}
