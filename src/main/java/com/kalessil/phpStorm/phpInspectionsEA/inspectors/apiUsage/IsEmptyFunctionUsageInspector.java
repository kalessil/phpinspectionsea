package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
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
    public boolean REPORT_EMPTY_USAGE                  = false;
    public boolean SUGGEST_TO_USE_COUNT_CHECK          = false;
    public boolean SUGGEST_TO_USE_NULL_COMPARISON      = true;
    public boolean SUGGEST_NULL_COMPARISON_FOR_SCALARS = true;

    private static final String messageDoNotUse    = "'empty(...)' counts too many values as empty, consider refactoring with type sensitive checks.";
    private static final String patternAlternative = "You should probably use '%s' instead.";

    @NotNull
    @Override
    public String getShortName() {
        return "IsEmptyFunctionUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'empty(...)' usage";
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
                        /* currently, php docs lacks of array structure notations, skip it */
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
                    if (SUGGEST_TO_USE_COUNT_CHECK && this.isCountableType(resolvedTypes)) {
                        final String comparison  = isInverted ? "!==" : "===";
                        final String replacement = ComparisonStyle.isRegular()
                                                   ? String.format("count(%s) %s 0", subject.getText(), comparison)
                                                   : String.format("0 %s count(%s)", comparison, subject.getText());
                        final PsiElement target  = isInverted ? parent : emptyExpression;
                        holder.registerProblem(
                                target,
                                String.format(MessagesPresentationUtil.prefixWithEa(patternAlternative), replacement),
                                new UseCountFix(replacement)
                        );
                        resolvedTypes.clear();
                        return;
                    }

                    /* case 2: nullable classes, nullable target core types */
                    if (
                        SUGGEST_TO_USE_NULL_COMPARISON &&
                        ((SUGGEST_NULL_COMPARISON_FOR_SCALARS && this.isNullableCoreType(resolvedTypes)) || TypesSemanticsUtil.isNullableObjectInterface(resolvedTypes))
                    ) {
                        /* false-positive: a field reference used in the subject expression */
                        PsiElement base = subject;
                        while (base instanceof PhpPsiElement) {
                            if (base instanceof FieldReference) {
                                break;
                            }
                            base = ((PhpPsiElement) base).getFirstPsiChild();
                        }
                        if (! (base instanceof FieldReference)) {
                            final String comparison  = isInverted ? "!==" : "===";
                            final String replacement = ComparisonStyle.isRegular()
                                                       ? String.format("%s %s null", subject.getText(), comparison)
                                                       : String.format("null %s %s", comparison, subject.getText());
                            holder.registerProblem(
                                    isInverted ? parent : emptyExpression,
                                    String.format(MessagesPresentationUtil.prefixWithEa(patternAlternative), replacement),
                                    new CompareToNullFix(replacement)
                            );
                        }
                        resolvedTypes.clear();
                        return;
                    }
                    resolvedTypes.clear();
                }

                if (REPORT_EMPTY_USAGE) {
                    holder.registerProblem(
                            emptyExpression,
                            MessagesPresentationUtil.prefixWithEa(messageDoNotUse)
                    );
                }
            }

            private boolean isCountableType(@NotNull Set<String> resolvedTypesSet) {
                if (! resolvedTypesSet.isEmpty()) {
                    return resolvedTypesSet.stream().allMatch( t -> {
                        boolean isIterable = false;
                        if (t.equals(Types.strArray)) {
                            isIterable = true;
                        } else if (t.startsWith("\\")) {
                            final List<PhpClass> resolved = OpenapiResolveUtil.resolveClassesAndInterfacesByFQN(t, PhpIndex.getInstance(holder.getProject()));
                            isIterable = resolved.stream().anyMatch(r -> InterfacesExtractUtil.getCrawlInheritanceTree(r, true).stream().anyMatch(c -> c.getFQN().equals("\\Countable")));
                        }
                        return isIterable;
                    });
                }
                return false;
            }

            private boolean isNullableCoreType(@NotNull Set<String> resolvedTypesSet) {
                boolean result = false;
                if (resolvedTypesSet.size() == 2 && resolvedTypesSet.contains(Types.strNull)) {
                    result = resolvedTypesSet.contains(Types.strInteger) ||
                             resolvedTypesSet.contains(Types.strFloat)   ||
                             resolvedTypesSet.contains(Types.strBoolean) ||
                             resolvedTypesSet.contains(Types.strResource);

                }
                return result;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Report empty() usage", REPORT_EMPTY_USAGE, (isSelected) -> REPORT_EMPTY_USAGE = isSelected);
            component.addCheckbox("Suggest to use count()-comparison", SUGGEST_TO_USE_COUNT_CHECK, (isSelected) -> SUGGEST_TO_USE_COUNT_CHECK = isSelected);
            component.addCheckbox("Suggest to use null-comparison", SUGGEST_TO_USE_NULL_COMPARISON, (isSelected) -> SUGGEST_TO_USE_NULL_COMPARISON = isSelected);
            component.addCheckbox("Suggest null-comparison for scalars", SUGGEST_NULL_COMPARISON_FOR_SCALARS, (isSelected) -> SUGGEST_NULL_COMPARISON_FOR_SCALARS = isSelected);
        });
    }

    private static final class CompareToNullFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use null comparison instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        CompareToNullFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class UseCountFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use count(...) instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseCountFix(@NotNull String expression) {
            super(expression);
        }
    }
}
