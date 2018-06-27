package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName == null || (!functionName.equals("substr") && !functionName.equals("mb_substr"))) {
                    return;
                }
                final PsiElement[] arguments = reference.getParameters();
                if ((3 != arguments.length && 4 != arguments.length) || !(arguments[2] instanceof BinaryExpression)) {
                    return;
                }


                /* Check if 3rd argument is "[mb_]strlen($search) - [mb_]strlen(...)"
                 *  - "[mb_]strlen($search)" is not needed
                 */
                final BinaryExpression candidate = (BinaryExpression) arguments[2];
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
                if (OpenapiTypesUtil.isFunctionReference(candidate.getLeftOperand()) && candidate.getRightOperand() != null) {
                    final FunctionReference leftCall  = (FunctionReference) candidate.getLeftOperand();
                    final String leftCallName         = leftCall.getName();
                    final PsiElement[] leftCallParams = leftCall.getParameters();
                    if (
                        1 == leftCallParams.length && leftCallName != null &&
                        (leftCallName.equals("strlen") || leftCallName.equals("mb_strlen")) &&
                        OpeanapiEquivalenceUtil.areEqual(leftCallParams[0], arguments[0])
                    ) {
                        if (OpeanapiEquivalenceUtil.areEqual(candidate.getRightOperand(), arguments[1])) {
                            /* 3rd parameter not needed at all */
                            final String message = patternDropLength.replace("%l%", arguments[2].getText());
                            holder.registerProblem(arguments[2], message, ProblemHighlightType.LIKE_UNUSED_SYMBOL, new Drop3rdParameterLocalFix(reference));
                        } else {
                            /* 3rd parameter can be simplified */
                            final String replacement;
                            try {
                                replacement = "-" + Integer.parseInt(candidate.getRightOperand().getText());
                            } catch (NumberFormatException notNumericOffset) {
                                return;
                            }

                            final String message = patternSimplifyLength.replace("%r%", replacement);
                            holder.registerProblem(candidate, message, new SimplifyFix(replacement));
                        }

                        // return;
                    }
                }
            }
        };
    }

    private static final class Drop3rdParameterLocalFix implements LocalQuickFix {
        private static final String title = "Remove ambiguous 3rd parameter";

        final private SmartPsiElementPointer<FunctionReference> call;

        Drop3rdParameterLocalFix(@NotNull FunctionReference call){
            super();

            this.call = SmartPointerManager.getInstance(call.getProject()).createSmartPsiElementPointer(call);
        }

        @NotNull
        @Override
        public String getName() {
            return title;
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

                call.getParameterList().replace(replacement.getParameterList());
            }
        }
    }

    private static final class SimplifyFix extends UseSuggestedReplacementFixer {
        private static final String title = "Simplify the third parameter";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        SimplifyFix(@NotNull String expression) {
            super(expression);
        }
    }
}
