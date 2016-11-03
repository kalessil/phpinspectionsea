package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strpos;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class SubStrShortHandUsageInspector extends BasePhpInspection {
    private static final String strProblemSimplifyLength = "Normally '%l%' can be dropped, so '-%r%' is only left (sometimes we discovering a range bug here, see bug-report #271).";
    private static final String strProblemDropLength     = "'%l%' can be safely dropped";

    @NotNull
    public String getShortName() {
        return "SubStrShortHandUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check if it's the target function: amount of parameters and name */
                final String strFunctionName = reference.getName();
                final PsiElement[] params    = reference.getParameters();
                if (
                    3 != params.length || !(params[2] instanceof BinaryExpression) ||
                    StringUtil.isEmpty(strFunctionName) || !strFunctionName.equals("substr")
                ) {
                    return;
                }


                /* Check if 3rd argument is "strlen($search) - strlen(...)"
                 *  - "strlen($search)" is not needed
                 */
                final BinaryExpression candidate = (BinaryExpression) params[2];
                final PsiElement operation       = candidate.getOperation();
                if (null == operation || null == operation.getNode()) {
                    return;
                }

                /* should be "* - *" */
                final IElementType operationType = operation.getNode().getElementType();
                if (operationType != PhpTokenTypes.opMINUS) {
                    return;
                }

                /* should be "strlen($search) - *" */
                if (
                    candidate.getLeftOperand() instanceof FunctionReferenceImpl &&
                    null != candidate.getRightOperand()
                ) {
                    final FunctionReference leftCall  = (FunctionReference) candidate.getLeftOperand();
                    final String leftCallName         = leftCall.getName();
                    final PsiElement[] leftCallParams = leftCall.getParameters();
                    if (
                        1 == leftCallParams.length && !StringUtil.isEmpty(leftCallName) && leftCallName.equals("strlen") &&
                        PsiEquivalenceUtil.areElementsEquivalent(leftCallParams[0], params[0])
                    ) {
                        if (PsiEquivalenceUtil.areElementsEquivalent(candidate.getRightOperand(), params[1])) {
                            /* 3rd parameter not needed at all */
                            final String message = strProblemDropLength.replace("%l%", params[2].getText());
                            holder.registerProblem(params[2], message, ProblemHighlightType.LIKE_DEPRECATED, new Drop3rdParameterLocalFix(reference));
                        } else {
                            /* 3rd parameter can be simplified */
                            final String message = strProblemSimplifyLength
                                    .replace("%l%", leftCall.getText())
                                    .replace("%r%", candidate.getRightOperand().getText());
                            final Simplify3rdParameterLocalFix fix = new Simplify3rdParameterLocalFix(candidate);
                            holder.registerProblem(leftCall, message, ProblemHighlightType.LIKE_DEPRECATED, fix);
                        }

                        // return;
                    }
                }
            }
        };
    }

    private static class Drop3rdParameterLocalFix implements LocalQuickFix {
        private FunctionReference call;

        Drop3rdParameterLocalFix(@NotNull FunctionReference call){
            super();
            this.call = call;
        }

        @NotNull
        @Override
        public String getName() {
            return "Remove ambiguous 3rd parameter";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement[] params           = this.call.getParameters();
            final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, "pattern(null, null)");
            final PsiElement[] replaceParams    = replacement.getParameters();
            replaceParams[0].replace(params[0]);
            replaceParams[1].replace(params[1]);

            //noinspection ConstantConditions I'm really sure NPE will not happen due to hardcoded expression
            call.getParameterList().replace(replacement.getParameterList());

            /* release a tree node reference */
            this.call = null;
        }
    }

    private static class Simplify3rdParameterLocalFix implements LocalQuickFix {
        private BinaryExpression subject;

        Simplify3rdParameterLocalFix(@NotNull BinaryExpression subject){
            super();
            this.subject = subject;
        }

        @NotNull
        @Override
        public String getName() {
            return "Simplify 3rd parameter";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            /* deletion of space after operation didn't work as for Mar 2016 */

            final PsiElement dropCandidate = this.subject.getLeftOperand();
            if (null != dropCandidate) {
                dropCandidate.delete();
            }

            /* release a tree node reference */
            this.subject = null;
        }
    }
}
