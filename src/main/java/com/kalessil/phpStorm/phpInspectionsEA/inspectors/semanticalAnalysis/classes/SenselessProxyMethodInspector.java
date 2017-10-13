package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.DropMethodFix;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

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
                if (clazz.isInterface() || clazz.isTrait()) {
                    return;
                }

                for (Method method : clazz.getOwnMethods()) {
                    final PsiElement methodNameNode = NamedElementUtil.getNameIdentifier(method);
                    if (null == methodNameNode || method.isAbstract() || method.getAccess().isPrivate()) {
                        continue;
                    }

                    /* we expect the method to have just one expression - parent invocation */
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                    if (null == body || 1 != ExpressionSemanticUtil.countExpressionsInGroup(body)) {
                        continue;
                    }
                    final PsiElement lastStatement = ExpressionSemanticUtil.getLastStatement(body);
                    if (null == lastStatement) {
                        continue;
                    }

                    /* parent invocation can be both direct or via return */
                    final PsiElement parentReferenceCandidate;
                    if (lastStatement instanceof PhpReturn) {
                        parentReferenceCandidate = ExpressionSemanticUtil.getReturnValue((PhpReturn) lastStatement);
                    } else {
                        parentReferenceCandidate = lastStatement.getFirstChild();
                    }
                    if (!(parentReferenceCandidate instanceof MethodReference)) {
                        continue;
                    }

                    final MethodReference reference = (MethodReference) parentReferenceCandidate;
                    final String referenceVariable  = reference.getFirstChild().getText().trim();
                    final String referenceName      = reference.getName();
                    if (null == referenceName || !referenceVariable.equals("parent") || !referenceName.equals(method.getName())) {
                        continue;
                    }

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
                        final PsiElement referenceResolved = OpenapiResolveUtil.resolveReference(referenceToMethod);
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
                                /* analyze if parameters definition has been changed (only ignore naming changes) */
                                if (methodParameters.length > 0) {
                                    for (int index = 0; index < parentParameters.length; ++index) {
                                        /* by-reference declaration changes: not allowed by PHP, hence not checked */

                                        /* default values changes */
                                        final PsiElement parentDefault = parentParameters[index].getDefaultValue();
                                        final PsiElement methodDefault = methodParameters[index].getDefaultValue();
                                        if ((null == parentDefault || null == methodDefault) && parentDefault != methodDefault) {
                                            isChangingSignature = true;
                                            break;
                                        }
                                        if (null != methodDefault && !OpeanapiEquivalenceUtil.areEqual(parentDefault, methodDefault)) {
                                            isChangingSignature = true;
                                            break;
                                        }

                                        /* type definition changes */
                                        final PhpType parentType = parentParameters[index].getDeclaredType();
                                        final PhpType methodType = methodParameters[index].getDeclaredType();
                                        if (!parentType.equals(methodType)) {
                                            isChangingSignature = true;
                                            break;
                                        }
                                    }
                                }

                                /* verify returned type declaration */
                                if (!isChangingSignature) {
                                    final PsiElement methodReturn = PhpPsiUtil.getChildByCondition(method, ClassReference.INSTANCEOF);
                                    final PsiElement parentReturn = PhpPsiUtil.getChildByCondition(nestedMethod, ClassReference.INSTANCEOF);
                                    if (methodReturn != parentReturn) {
                                        isChangingSignature =
                                                methodReturn == null || parentReturn == null ||
                                                !OpeanapiEquivalenceUtil.areEqual(methodReturn, parentReturn);
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
                        holder.registerProblem(methodNameNode, message, ProblemHighlightType.WEAK_WARNING, new DropMethodFix());
                    }
                }
            }
        };
    }
}
