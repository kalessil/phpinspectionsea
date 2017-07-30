package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SubStrShortHandUsageInspector extends BasePhpInspection {
    private static final String patternSimplifyLength = "'%r%' can be used instead.";
    private static final String patternDropLength     = "'%l%' can be safely dropped.";

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
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (
                    (3 != params.length && 4 != params.length) || !(params[2] instanceof BinaryExpression) ||
                    StringUtils.isEmpty(functionName) || (!functionName.equals("substr") && !functionName.equals("mb_substr"))
                ) {
                    return;
                }


                /* Check if 3rd argument is "[mb_]strlen($search) - [mb_]strlen(...)"
                 *  - "[mb_]strlen($search)" is not needed
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

                /* should be "[mb_]strlen($search) - *" */
                if (
                    candidate.getLeftOperand() instanceof FunctionReferenceImpl &&
                    null != candidate.getRightOperand()
                ) {
                    final FunctionReference leftCall  = (FunctionReference) candidate.getLeftOperand();
                    final String leftCallName         = leftCall.getName();
                    final PsiElement[] leftCallParams = leftCall.getParameters();
                    if (
                        1 == leftCallParams.length && !StringUtils.isEmpty(leftCallName) &&
                        (leftCallName.equals("strlen") || leftCallName.equals("mb_strlen")) &&
                        PsiEquivalenceUtil.areElementsEquivalent(leftCallParams[0], params[0])
                    ) {
                        if (PsiEquivalenceUtil.areElementsEquivalent(candidate.getRightOperand(), params[1])) {
                            /* 3rd parameter not needed at all */
                            final String message = patternDropLength.replace("%l%", params[2].getText());
                            holder.registerProblem(params[2], message, ProblemHighlightType.LIKE_UNUSED_SYMBOL, new Drop3rdParameterLocalFix(reference));
                        } else {
                            /* 3rd parameter can be simplified */
                            final String replacement;
                            try {
                                replacement = "-" + (Integer.valueOf(candidate.getRightOperand().getText()) - 1);
                            } catch (NumberFormatException notNumericOffset) {
                                return;
                            }

                            final String message = patternSimplifyLength.replace("%r%", replacement);
                            holder.registerProblem(candidate, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new SimplifyFix(replacement));
                        }

                        // return;
                    }
                }
            }
        };
    }

    private static class Drop3rdParameterLocalFix implements LocalQuickFix {
        final private SmartPsiElementPointer<FunctionReference> call;

        Drop3rdParameterLocalFix(@NotNull FunctionReference call){
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(call.getProject());

            this.call = factory.createSmartPsiElementPointer(call);
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
            final FunctionReference call = this.call.getElement();
            if (null != call) {
                final PsiElement[] params           = call.getParameters();
                final String pattern                = 3 == params.length ? "pattern(null, null)" : "pattern(null, null, null, null)";
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, pattern);
                final PsiElement[] replaceParams    = replacement.getParameters();
                replaceParams[0].replace(params[0]);
                replaceParams[1].replace(params[1]);
                if (3 != params.length) {
                    replaceParams[3].replace(params[3]);
                }

                //noinspection ConstantConditions I'm really sure NPE will not happen due to hardcoded expression
                call.getParameterList().replace(replacement.getParameterList());
            }
        }
    }

    private static class SimplifyFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Simplify the third parameter";
        }

        SimplifyFix(@NotNull String expression) {
            super(expression);
        }
    }
}
