package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnSafeIsSetOverArrayInspector extends BasePhpInspection {
    // Inspection options.
    public boolean SUGGEST_TO_USE_ARRAY_KEY_EXISTS = false;
    public boolean SUGGEST_TO_USE_NULL_COMPARISON  = true;
    public boolean REPORT_CONCATENATION_IN_INDEXES = true;

    // static messages for triggered messages
    private static final String messageUseArrayKeyExists    = "'array_key_exists(...)' construction should be used for better data *structure* control.";
    private static final String messageConcatenationInIndex = "Concatenation is used in an index, it should be moved to a variable.";
    private static final String patternUseNullComparison    = "'%s' construction should be used instead.";

    @NotNull
    public String getShortName() {
        return "UnSafeIsSetOverArrayInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpIsset(@NotNull PhpIsset issetExpression) {
                /*
                 * if no parameters, we don't check;
                 * if multiple parameters, perhaps if-inspection fulfilled and isset's were merged
                 *
                 * TODO: still needs analysis regarding concatenations in indexes
                 */
                final PhpExpression[] arguments = issetExpression.getVariables();
                if (arguments.length != 1) {
                    return;
                }

                /* gather context information */
                PsiElement issetParent = issetExpression.getParent();
                boolean issetInverted  = false;
                if (issetParent instanceof UnaryExpression) {
                    final PsiElement operator = ((UnaryExpression) issetParent).getOperation();
                    if (OpenapiTypesUtil.is(operator, PhpTokenTypes.opNOT)) {
                        issetInverted = true;
                        issetParent   = issetParent.getParent();
                    }
                }
                boolean isResultStored = (issetParent instanceof AssignmentExpression || issetParent instanceof PhpReturn);

                /* false-positives:  ternaries using isset-or-null semantics, there array_key_exist can introduce bugs */
                final PsiElement conditionCandidate = issetInverted ? issetExpression.getParent() : issetExpression;
                boolean isTernaryCondition          = issetParent instanceof TernaryExpression && conditionCandidate == ((TernaryExpression) issetParent).getCondition();
                if (isTernaryCondition) {
                    final TernaryExpression ternary = (TernaryExpression) issetParent;
                    final PsiElement nullCandidate  = issetInverted ? ternary.getTrueVariant() : ternary.getFalseVariant();
                    if (PhpLanguageUtil.isNull(nullCandidate)) {
                        return;
                    }
                }

                /* do analyze  */
                final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(arguments[0]);
                if (argument == null) {
                    return;
                }
                /* false positives: variables in template/global context - too unreliable */
                if (argument instanceof Variable && ExpressionSemanticUtil.getBlockScope(argument) == null) {
                    return;
                }

                if (!(argument instanceof ArrayAccessExpression)) {
                    if (argument instanceof FieldReference) {
                        /* if field is not resolved, it's probably dynamic and isset has a purpose */
                        final PsiReference referencedField = argument.getReference();
                        final PsiElement resolvedField     = referencedField == null ? null : OpenapiResolveUtil.resolveReference(referencedField);
                        if (resolvedField == null || !(ExpressionSemanticUtil.getBlockScope(resolvedField) instanceof PhpClass)) {
                            return;
                        }
                    }

                    if (SUGGEST_TO_USE_NULL_COMPARISON) {
                        /* false-positives: finally, perhaps fallback to initialization in try */
                        if (PsiTreeUtil.getParentOfType(issetExpression, Finally.class) == null) {
                            final List<String> fragments = Arrays.asList(argument.getText(), issetInverted ? "===" : "!==", "null");
                            if (!ComparisonStyle.isRegular()) {
                                Collections.reverse(fragments);
                            }
                            final String replacement = String.join(" ", fragments);
                            holder.registerProblem(
                                    issetInverted ? issetExpression.getParent() : issetExpression,
                                    String.format(patternUseNullComparison, replacement),
                                    ProblemHighlightType.WEAK_WARNING,
                                    new CompareToNullFix(replacement)
                            );
                        }
                    }
                    return;
                }

                /* TODO: has method/function reference as index */
                if (REPORT_CONCATENATION_IN_INDEXES && !isResultStored && this.hasConcatenationAsIndex((ArrayAccessExpression) argument)) {
                    holder.registerProblem(argument, messageConcatenationInIndex);
                    return;
                }

                if (SUGGEST_TO_USE_ARRAY_KEY_EXISTS && !isArrayAccess((ArrayAccessExpression) argument)) {
                    holder.registerProblem(argument, messageUseArrayKeyExists, ProblemHighlightType.WEAK_WARNING);
                }
            }

            /* checks if any of indexes is concatenation expression */
            /* TODO: iterator for array access expression */
            private boolean hasConcatenationAsIndex(@NotNull ArrayAccessExpression expression) {
                PsiElement expressionToInspect = expression;
                while (expressionToInspect instanceof ArrayAccessExpression) {
                    final ArrayIndex index = ((ArrayAccessExpression) expressionToInspect).getIndex();
                    if (index != null && index.getValue() instanceof BinaryExpression) {
                        final PsiElement operation = ((BinaryExpression) index.getValue()).getOperation();
                        if (operation != null && operation.getNode().getElementType() == PhpTokenTypes.opCONCAT) {
                            return true;
                        }
                    }

                    expressionToInspect = expressionToInspect.getParent();
                }

                return false;
            }

            // TODO: partially duplicates semanticalAnalysis.OffsetOperationsInspector.isContainerSupportsArrayAccess()
            private boolean isArrayAccess(@NotNull ArrayAccessExpression expression) {
                /* ok JB parses `$var[]= ...` always as array, lets make it working properly and report them later */
                final PsiElement container = expression.getValue();
                if (!(container instanceof PhpTypedElement)) {
                    return false;
                }

                final Set<String> containerTypes = new HashSet<>();
                final PhpType resolved           = OpenapiResolveUtil.resolveType((PhpTypedElement) container, container.getProject());
                if (resolved != null) {
                    resolved.filterUnknown().getTypes().forEach(t -> containerTypes.add(Types.getType(t)));
                }
                /* failed to resolve, don't try to guess anything */
                if (containerTypes.isEmpty()) {
                    return false;
                }

                boolean supportsOffsets = false;
                for (final String typeToCheck : containerTypes) {
                    /* assume is just null-ble declaration or we shall just rust to mixed */
                    if (typeToCheck.equals(Types.strNull)) {
                        continue;
                    }
                    if (typeToCheck.equals(Types.strMixed)) {
                        supportsOffsets = true;
                        continue;
                    }

                    /* some of possible types are scalars, what's wrong */
                    if (!StringUtils.isEmpty(typeToCheck) && typeToCheck.charAt(0) != '\\') {
                        supportsOffsets = false;
                        break;
                    }

                    /* assume class has what is needed, OffsetOperationsInspector should report if not */
                    supportsOffsets = true;
                }
                containerTypes.clear();

                return supportsOffsets;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Suggest to use array_key_exists()", SUGGEST_TO_USE_ARRAY_KEY_EXISTS, (isSelected) -> SUGGEST_TO_USE_ARRAY_KEY_EXISTS = isSelected);
            component.addCheckbox("Suggest to use null-comparison", SUGGEST_TO_USE_NULL_COMPARISON, (isSelected) -> SUGGEST_TO_USE_NULL_COMPARISON = isSelected);
            component.addCheckbox("Report concatenations in indexes", REPORT_CONCATENATION_IN_INDEXES, (isSelected) -> REPORT_CONCATENATION_IN_INDEXES = isSelected);
        });
    }

    private static final class CompareToNullFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use null comparison instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        CompareToNullFix(@NotNull String expression) {
            super(expression);
        }
    }
}
