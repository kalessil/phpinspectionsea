package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
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

public class SubStrUsedAsStrPosInspector extends BasePhpInspection {
    // Inspection options.
    private static final String messagePattern = "'%s' can be used instead (improves maintainability).";

    private static final Set<String> functions      = new HashSet<>();
    private static final Set<String> outerFunctions = new HashSet<>();
    static {
        functions.add("substr");
        functions.add("mb_substr");

        outerFunctions.add("strtolower");
        outerFunctions.add("strtoupper");
        outerFunctions.add("mb_strtolower");
        outerFunctions.add("mb_strtoupper");
        outerFunctions.add("mb_convert_case");
    }

    @NotNull
    public String getShortName() {
        return "SubStrUsedAsStrPosInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName == null || !functions.contains(functionName)) {
                    return;
                }
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length != 3 && arguments.length != 4) {
                    return;
                }

                /* checking 2nd and 3rd arguments is not needed/simplified:
                 *   - 2nd re-used as it is (should be a positive number!)
                 *   - 3rd is not important, as we'll rely on parent comparison operand instead
                 */
                final String index = arguments[1].getText();
                if (!OpenapiTypesUtil.isNumber(arguments[1]) || !index.equals("0")) {
                    return;
                }

                /* prepare variables, so we could properly process polymorphic pattern */
                PsiElement highLevelCall    = reference;
                PsiElement parentExpression = reference.getParent();
                if (parentExpression instanceof ParameterList) {
                    parentExpression = parentExpression.getParent();
                }

                /* if the call wrapped with case manipulation, propose to use stripos */
                boolean caseManipulated = false;
                if (OpenapiTypesUtil.isFunctionReference(parentExpression)) {
                    final FunctionReference parentCall = (FunctionReference) parentExpression;
                    final PsiElement[] parentArguments = parentCall.getParameters();
                    final String parentName            = parentCall.getName();
                    if (parentName != null && parentArguments.length == 1 && outerFunctions.contains(parentName)) {
                        caseManipulated  = true;
                        highLevelCall    = parentExpression;
                        parentExpression = parentExpression.getParent();
                    }
                }

                /* check parent expression, to ensure pattern matched */
                if (parentExpression instanceof BinaryExpression) {
                    final BinaryExpression parent = (BinaryExpression) parentExpression;
                    if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(parent.getOperationType())) {
                        final PsiElement secondOperand = OpenapiElementsUtil.getSecondOperand(parent, highLevelCall);
                        final PsiElement operationNode = parent.getOperation();
                        if (secondOperand != null && operationNode != null) {
                            final String operator      = operationNode.getText();
                            final boolean isMbFunction = functionName.equals("mb_substr");
                            final boolean hasEncoding  = isMbFunction && arguments.length == 4;

                            final String call          = String.format(
                                    "%s(%s, %s%s)",
                                    (isMbFunction ? "mb_" : "") + (caseManipulated ? "stripos" : "strpos"),
                                    arguments[0].getText(),
                                    secondOperand.getText(),
                                    hasEncoding ? (", " + arguments[3].getText()) : ""
                            );
                            final boolean isRegular    = ComparisonStyle.isRegular();
                            final String replacement   = String.format(
                                    "%s %s %s",
                                    isRegular ? call : index,
                                    operator.length() == 2 ? (operator + '=') : operator,
                                    isRegular ? index : call
                            );
                            holder.registerProblem(
                                    parentExpression,
                                    String.format(messagePattern, replacement),
                                    new UseStringSearchFix(replacement)
                            );
                        }
                    }
                }
            }
        };
    }

    private static final class UseStringSearchFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use substring search instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseStringSearchFix(@NotNull String expression) {
            super(expression);
        }
    }
}