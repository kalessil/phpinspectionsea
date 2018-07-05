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
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SubStrShortHandUsageInspector extends BasePhpInspection {
    private static final String patternSimplifyLength = "'%s' can be used instead.";
    private static final String patternDropLength     = "'%s' can be safely dropped.";

    private static final Set<String> substringFunctions = new HashSet<>();
    private static final Set<String> lengthFunctions    = new HashSet<>();
    static {
        substringFunctions.add("substr");
        substringFunctions.add("mb_substr");

        lengthFunctions.add("strlen");
        lengthFunctions.add("mb_strlen");
    }

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
                if (functionName != null && substringFunctions.contains(functionName)) {
                    final PsiElement[] arguments = reference.getParameters();
                    if ((arguments.length == 3 || arguments.length == 4) && arguments[2] instanceof BinaryExpression) {
                        /* check if 3rd argument is "strlen($search) - strlen(...)": "strlen($search)" is not needed */
                        final BinaryExpression binary = (BinaryExpression) arguments[2];
                        if (binary.getOperationType() == PhpTokenTypes.opMINUS) {
                            final PsiElement left  = binary.getLeftOperand();
                            final PsiElement right = binary.getRightOperand();
                            if (left != null && right != null && OpenapiTypesUtil.isFunctionReference(left)) {
                                final FunctionReference leftCall = (FunctionReference) left;
                                final String leftName            = leftCall.getName();
                                if (leftName != null && lengthFunctions.contains(leftName)) {
                                    final PsiElement[] leftArguments = leftCall.getParameters();
                                    if (leftArguments.length == 1 && OpenapiEquivalenceUtil.areEqual(leftArguments[0], arguments[0])) {
                                        final PsiElement startOffset = arguments[1];
                                        if (OpenapiEquivalenceUtil.areEqual(right, startOffset)) {
                                            /* case: third parameter is not needed at all */
                                            holder.registerProblem(
                                                    arguments[2],
                                                    String.format(patternDropLength, arguments[2].getText()),
                                                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                                    new DropThirdParameterFix(reference)
                                            );
                                        } else if (OpenapiTypesUtil.isNumber(startOffset) && OpenapiTypesUtil.isNumber(right)) {
                                            try {
                                                int offset = Integer.parseInt(startOffset.getText()) - Integer.parseInt(right.getText());
                                                if (offset < 0) {
                                                    /* case: third parameter can be simplified */
                                                    holder.registerProblem(
                                                            binary,
                                                            String.format(patternSimplifyLength, offset),
                                                            new SimplifyFix(String.valueOf(offset))
                                                    );
                                                } else {
                                                    /* case: third parameter is not needed at all */
                                                    holder.registerProblem(
                                                            arguments[2],
                                                            String.format(patternDropLength, arguments[2].getText()),
                                                            ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                                            new DropThirdParameterFix(reference)
                                                    );
                                                }
                                            } catch (final NumberFormatException expected) {
                                                // return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class DropThirdParameterFix implements LocalQuickFix {
        private static final String title = "Remove the third parameter";

        final private SmartPsiElementPointer<FunctionReference> call;

        DropThirdParameterFix(@NotNull FunctionReference call){
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
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final FunctionReference call = this.call.getElement();
            if (call != null && !project.isDisposed()) {
                final PsiElement socket = call.getParameterList();
                if (socket != null) {
                    final PsiElement[] arguments        = call.getParameters();
                    final String pattern                = arguments.length == 3 ? "pattern(null, null)" : "pattern(null, null, null, null)";
                    final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, pattern);
                    final PsiElement[] replaceArguments = replacement.getParameters();
                    replaceArguments[0].replace(arguments[0]);
                    replaceArguments[1].replace(arguments[1]);
                    if (arguments.length != 3) {
                        replaceArguments[3].replace(arguments[3]);
                    }
                    final PsiElement donor  = replacement.getParameterList();
                    if (donor != null) {
                        socket.replace(donor);
                    }
                }
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
