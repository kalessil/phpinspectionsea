package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiPsiSearchUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DynamicInvocationViaScopeResolutionInspector extends BasePhpInspection {
    private static final String strProblemScopeResolutionUsed = "'$this->%m%(...)' should be used instead.";
    private static final String strProblemExpressionUsed      = "'...->%m%(...)' should be used instead.";

    @NotNull
    public String getShortName() {
        return "DynamicInvocationViaScopeResolutionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethodReference(MethodReference reference) {
                final PsiElement operator = OpenapiPsiSearchUtil.findResolutionOperator(reference);
                if (null == operator || PhpTokenTypes.SCOPE_RESOLUTION != operator.getNode().getElementType()) {
                    return;
                }

                final PsiReference objReference = reference.getReference();
                final String methodName         = reference.getName();
                if (null != objReference && !StringUtils.isEmpty(methodName)) {
                    final PsiElement objResolvedRef = objReference.resolve();
                    /* resolved method is static but called with $ this*/
                    if (objResolvedRef instanceof Method) {
                        final Method method  = (Method) objResolvedRef;
                        final PhpClass clazz = method.getContainingClass();
                        /* non-static methods and contract interfaces must not be reported */
                        if (null == clazz || clazz.isInterface() || method.isStatic() || method.isAbstract()) {
                            return;
                        }

                        /* check first pattern static::dynamic */
                        final PsiElement staticCandidate = reference.getFirstChild();
                        final String candidateContent    = staticCandidate.getText();
                        if (candidateContent.equals("static") || candidateContent.equals("self")) {
                            final Function scope = ExpressionSemanticUtil.getScope(reference);
                            if (!(scope instanceof Method)) {
                                return;
                            }

                            if (((Method) scope).isStatic()) {
                                final String message = strProblemExpressionUsed.replace("%m%", reference.getName());
                                holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);
                            } else {
                                final String message = strProblemScopeResolutionUsed.replace("%m%", methodName);
                                holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(operator, staticCandidate));
                            }

                            return;
                        }

                        /* check second pattern <expression>::dynamic */
                        final PsiElement objectExpression = reference.getFirstPsiChild();
                        if (null != objectExpression && !(objectExpression instanceof FunctionReference) && !(staticCandidate instanceof ClassReference)) {
                            final String message = strProblemExpressionUsed.replace("%m%", reference.getName());
                            holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR, new TheLocalFix(operator, null));
                        }
                    }
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @Nullable
        private final SmartPsiElementPointer<PsiElement> object;
        @NotNull
        private final SmartPsiElementPointer<PsiElement> operator;

        TheLocalFix(@NotNull PsiElement operator, @Nullable PsiElement object) {
            super();
            SmartPointerManager manager =  SmartPointerManager.getInstance(operator.getProject());

            this.object   = null == object ? null : manager.createSmartPsiElementPointer(object);
            this.operator = manager.createSmartPsiElementPointer(operator);
        }

        @NotNull
        @Override
        public String getName() {
            return "Use ->";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement operator = this.operator.getElement();
            final PsiElement arrow    = PhpPsiElementFactory.createArrow(project);
            //noinspection ConstantConditions - createArrow CAN return null
            if (null == operator || null == arrow) {
                return;
            }

            operator.replace(arrow);

            final PsiElement object       = null == this.object ? null : this.object.getElement();
            final PsiElement thisVariable = PhpPsiElementFactory.createVariable(project, "this", true);
            //noinspection ConstantConditions - let's secureourselves here as well
            if (null != object && null != thisVariable) {
                object.replace(thisVariable);
            }
        }
    }
}