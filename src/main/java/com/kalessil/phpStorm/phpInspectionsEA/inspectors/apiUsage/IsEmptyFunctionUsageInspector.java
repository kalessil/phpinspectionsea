package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPsiResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypesSemanticsUtil;

import javax.swing.*;
import java.util.HashSet;

import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class IsEmptyFunctionUsageInspector extends BasePhpInspection {
    // Inspections options.
    public boolean optionReportEmptyUsage       = false;
    public boolean optionSuggestToUseCountCheck = false;
    public boolean optionsToUseNullComparison   = true;

    private static final String messageDoNotUse          = "'empty(...)' counts too many values as empty, consider refactoring with type sensitive checks.";
    private static final String patternUseCount          = "You should probably use '%e%' instead.";
    private static final String patternUseNullComparison = "You should probably use '%e%' instead.";

    @NotNull
    public String getShortName() {
        return "IsEmptyFunctionUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpEmpty(PhpEmpty emptyExpression) {
                final PhpExpression[] values = emptyExpression.getVariables();
                if (values.length == 1) {
                    final PsiElement subject = ExpressionSemanticUtil.getExpressionTroughParenthesis(values[0]);
                    if (null == subject || subject instanceof ArrayAccessExpression) {
                        /* currently php docs lacks of array structure notations, skip it */
                        return;
                    }

                    final PsiElement parent    = emptyExpression.getParent();
                    final PsiElement operation = parent instanceof UnaryExpression ? ((UnaryExpression) parent).getOperation() : null;
                    final boolean isInverted   = null != operation && PhpTokenTypes.opNOT == operation.getNode().getElementType();

                    /* extract types */
                    final PhpIndex index                = PhpIndex.getInstance(holder.getProject());
                    final Function scope                = ExpressionSemanticUtil.getScope(emptyExpression);
                    final HashSet<String> resolvedTypes = new HashSet<>();
                    TypeFromPsiResolvingUtil.resolveExpressionType(subject, scope, index, resolvedTypes);

                    /* Case 1: empty(array) - hidden logic - empty array */
                    if (this.isArrayType(resolvedTypes)) {
                        resolvedTypes.clear();

                        if (optionSuggestToUseCountCheck) {
                            final String replacement = "0 %o% count(%a%)"
                                .replace("%a%", subject.getText())
                                .replace("%o%", isInverted ? "!==": "===");
                            final String message    = patternUseCount.replace("%e%", replacement);
                            final PsiElement target = isInverted ? parent : emptyExpression;
                            holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new UseCountFix(replacement));
                        }

                        return;
                    }

                    /* case 2: nullable classes, int, float, resource */
                    if (this.isNullableCoreType(resolvedTypes) || TypesSemanticsUtil.isNullableObjectInterface(resolvedTypes)) {
                        resolvedTypes.clear();

                        if (optionsToUseNullComparison) {
                            final String replacement = "null %o% %a%"
                                .replace("%a%", subject.getText())
                                .replace("%o%", isInverted ? "!==": "===");
                            final String message    = patternUseNullComparison.replace("%e%", replacement);
                            final PsiElement target = isInverted ? parent : emptyExpression;
                            holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new CompareToNullFix(replacement));
                        }

                        return;
                    }

                    resolvedTypes.clear();
                }

                if (optionReportEmptyUsage) {
                    holder.registerProblem(emptyExpression, messageDoNotUse, ProblemHighlightType.WEAK_WARNING);
                }
            }


            /** check if only array type possible */
            private boolean isArrayType(HashSet<String> resolvedTypesSet) {
                return resolvedTypesSet.size() == 1 && resolvedTypesSet.contains(Types.strArray);
            }

            /** check if nullable int, float, resource */
            private boolean isNullableCoreType(HashSet<String> resolvedTypesSet) {
                //noinspection SimplifiableIfStatement
                if (2 != resolvedTypesSet.size() || !resolvedTypesSet.contains(Types.strNull)) {
                    return false;
                }

                return  resolvedTypesSet.contains(Types.strInteger) ||
                        resolvedTypesSet.contains(Types.strFloat)   ||
                        resolvedTypesSet.contains(Types.strString)  ||
                        resolvedTypesSet.contains(Types.strBoolean) ||
                        resolvedTypesSet.contains(Types.strResource);
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.createCheckbox("Report empty() usage", optionReportEmptyUsage, (isSelected) -> optionReportEmptyUsage = isSelected);
            component.createCheckbox("Suggest to use count()-comparison", optionSuggestToUseCountCheck, (isSelected) -> optionSuggestToUseCountCheck = isSelected);
            component.createCheckbox("Suggest to use null-comparison", optionsToUseNullComparison, (isSelected) -> optionsToUseNullComparison = isSelected);
        });
    }

    private class CompareToNullFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use null comparison instead";
        }

        CompareToNullFix(@NotNull String expression) {
            super(expression);
        }
    }

    private class UseCountFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use count(...) instead";
        }

        UseCountFix(@NotNull String expression) {
            super(expression);
        }
    }
}
