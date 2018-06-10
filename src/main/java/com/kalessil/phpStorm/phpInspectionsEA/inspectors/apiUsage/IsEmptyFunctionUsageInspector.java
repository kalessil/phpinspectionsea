package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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

public class IsEmptyFunctionUsageInspector extends BasePhpInspection {
    // Inspections options.
    public boolean REPORT_EMPTY_USAGE             = false;
    public boolean SUGGEST_TO_USE_COUNT_CHECK     = false;
    public boolean SUGGEST_TO_USE_NULL_COMPARISON = true;
    public boolean PREFER_YODA_STYLE              = true;
    public boolean PREFER_REGULAR_STYLE           = false;

    private static final String messageDoNotUse    = "'empty(...)' counts too many values as empty, consider refactoring with type sensitive checks.";
    private static final String patternAlternative = "You should probably use '%s' instead.";

    @NotNull
    public String getShortName() {
        return "IsEmptyFunctionUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpEmpty(@NotNull PhpEmpty emptyExpression) {
                final PhpExpression[] values = emptyExpression.getVariables();
                if (values.length == 1) {
                    final PsiElement subject = ExpressionSemanticUtil.getExpressionTroughParenthesis(values[0]);
                    if (subject == null || subject instanceof ArrayAccessExpression) {
                        /* currently php docs lacks of array structure notations, skip it */
                        return;
                    }

                    final PsiElement parent    = emptyExpression.getParent();
                    final PsiElement operation = parent instanceof UnaryExpression ? ((UnaryExpression) parent).getOperation() : null;
                    final boolean isInverted   = OpenapiTypesUtil.is(operation, PhpTokenTypes.opNOT);

                    /* extract types */
                    final Set<String> resolvedTypes = new HashSet<>();
                    if (subject instanceof PhpTypedElement) {
                        final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) subject, holder.getProject());
                        if (resolved != null) {
                            resolved.filterUnknown().getTypes().forEach(t -> resolvedTypes.add(Types.getType(t)));
                        }
                    }

                    /* Case 1: empty(array) - hidden logic - empty array */
                    if (this.isArrayType(resolvedTypes)) {
                        resolvedTypes.clear();

                        if (SUGGEST_TO_USE_COUNT_CHECK) {
                            final String replacement = (PREFER_YODA_STYLE ? "0 %o% count(%a%)" : "count(%a%) %o% 0")
                                .replace("%a%", subject.getText())
                                .replace("%o%", isInverted ? "!==": "===");
                            final PsiElement target  = isInverted ? parent : emptyExpression;
                            holder.registerProblem(target, String.format(patternAlternative, replacement), new UseCountFix(replacement));
                        }

                        return;
                    }

                    /* case 2: nullable classes, int, float, resource */
                    if (this.isNullableCoreType(resolvedTypes) || TypesSemanticsUtil.isNullableObjectInterface(resolvedTypes)) {
                        resolvedTypes.clear();

                        if (SUGGEST_TO_USE_NULL_COMPARISON) {
                            final String replacement = (PREFER_YODA_STYLE ? "null %o% %a%" : "%a% %o% null")
                                .replace("%a%", subject.getText())
                                .replace("%o%", isInverted ? "!==" : "===");
                            final PsiElement target  = isInverted ? parent : emptyExpression;
                            holder.registerProblem(target, String.format(patternAlternative, replacement), new CompareToNullFix(replacement));
                        }

                        return;
                    }

                    resolvedTypes.clear();
                }

                if (REPORT_EMPTY_USAGE) {
                    holder.registerProblem(emptyExpression, messageDoNotUse, ProblemHighlightType.WEAK_WARNING);
                }
            }


            /** check if only array type possible */
            private boolean isArrayType(@NotNull Set<String> resolvedTypesSet) {
                return resolvedTypesSet.size() == 1 && resolvedTypesSet.contains(Types.strArray);
            }

            /** check if nullable int, float, resource */
            private boolean isNullableCoreType(@NotNull Set<String> resolvedTypesSet) {
                if (resolvedTypesSet.size() != 2 || !resolvedTypesSet.contains(Types.strNull)) {
                    return false;
                }

                return  resolvedTypesSet.contains(Types.strInteger) ||
                        resolvedTypesSet.contains(Types.strFloat)   |
                        resolvedTypesSet.contains(Types.strBoolean) ||
                        resolvedTypesSet.contains(Types.strResource);
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Report empty() usage", REPORT_EMPTY_USAGE, (isSelected) -> REPORT_EMPTY_USAGE = isSelected);
            component.addCheckbox("Suggest to use count()-comparison", SUGGEST_TO_USE_COUNT_CHECK, (isSelected) -> SUGGEST_TO_USE_COUNT_CHECK = isSelected);
            component.addCheckbox("Suggest to use null-comparison", SUGGEST_TO_USE_NULL_COMPARISON, (isSelected) -> SUGGEST_TO_USE_NULL_COMPARISON = isSelected);

            component.delegateRadioCreation((radioComponent) -> {
                radioComponent.addOption("Regular fix style", PREFER_REGULAR_STYLE, (isSelected) -> PREFER_REGULAR_STYLE = isSelected);
                radioComponent.addOption("Yoda fix style", PREFER_YODA_STYLE, (isSelected) -> PREFER_YODA_STYLE = isSelected);
            });
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
