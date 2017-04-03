package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.PhpEmpty;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPsiResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypesSemanticsUtil;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashSet;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class IsEmptyFunctionUsageInspector extends BasePhpInspection {
    // configuration flags automatically saved by IDE
    public boolean REPORT_EMPTY_USAGE             = false;
    public boolean SUGGEST_TO_USE_COUNT_CHECK     = false;
    public boolean SUGGEST_TO_USE_NULL_COMPARISON = true;

    // static messages for triggered messages
    private static final String strProblemDescriptionDoNotUse          = "'empty(...)' counts too many values as empty, consider refactoring with type sensitive checks.";
    private static final String strProblemDescriptionUseCount          = "'0 === count($...)' construction should be used instead.";
    private static final String strProblemDescriptionUseNullComparison = "You should probably use 'null === $...'.";

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
                    if (subject instanceof ArrayAccessExpression) {
                        /* currently php docs lacks of array structure notations, skip it */
                        return;
                    }

                    /* extract types */
                    final PhpIndex index                = PhpIndex.getInstance(holder.getProject());
                    final Function scope                = ExpressionSemanticUtil.getScope(emptyExpression);
                    final HashSet<String> resolvedTypes = new HashSet<>();
                    TypeFromPsiResolvingUtil.resolveExpressionType(subject, scope, index, resolvedTypes);

                    /* Case 1: empty(array) - hidden logic - empty array */
                    if (this.isArrayType(resolvedTypes)) {
                        resolvedTypes.clear();

                        if (SUGGEST_TO_USE_COUNT_CHECK) {
                            holder.registerProblem(emptyExpression, strProblemDescriptionUseCount, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }

                        return;
                    }

                    /* case 2: nullable classes, int, float, resource */
                    if (this.isNullableCoreType(resolvedTypes) || TypesSemanticsUtil.isNullableObjectInterface(resolvedTypes)) {
                        resolvedTypes.clear();

                        if (SUGGEST_TO_USE_NULL_COMPARISON) {
                            holder.registerProblem(emptyExpression, strProblemDescriptionUseNullComparison, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }

                        return;
                    }

                    resolvedTypes.clear();
                }

                if (REPORT_EMPTY_USAGE) {
                    holder.registerProblem(emptyExpression, strProblemDescriptionDoNotUse, ProblemHighlightType.WEAK_WARNING);
                }
            }


            /** check if only array type possible */
            private boolean isArrayType(HashSet<String> resolvedTypesSet) {
                return resolvedTypesSet.size() == 1 && resolvedTypesSet.contains(Types.strArray);
            }

            /** check if nullable int, float, resource */
            private boolean isNullableCoreType(HashSet<String> resolvedTypesSet) {
                //noinspection SimplifiableIfStatement
                if (resolvedTypesSet.size() != 2 || !resolvedTypesSet.contains(Types.strNull)) {
                    return false;
                }

                return  resolvedTypesSet.contains(Types.strInteger) ||
                        resolvedTypesSet.contains(Types.strFloat) ||
                        resolvedTypesSet.contains(Types.strString) ||
                        resolvedTypesSet.contains(Types.strBoolean) ||
                        resolvedTypesSet.contains(Types.strResource);
            }
        };
    }

    public JComponent createOptionsPanel() {
        return (new IsEmptyFunctionUsageInspector.OptionsPanel()).getComponent();
    }

    private class OptionsPanel {
        final private JPanel optionsPanel;

        final private JCheckBox reportEmptyUsage;
        final private JCheckBox suggestToUseCountComparison;
        final private JCheckBox suggestToUseNullComparison;

        public OptionsPanel() {
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new MigLayout());

            reportEmptyUsage = new JCheckBox("Report empty() usage", REPORT_EMPTY_USAGE);
            reportEmptyUsage.addChangeListener(e -> REPORT_EMPTY_USAGE = reportEmptyUsage.isSelected());
            optionsPanel.add(reportEmptyUsage, "wrap");

            suggestToUseCountComparison = new JCheckBox("Suggest to use count()-comparison", SUGGEST_TO_USE_COUNT_CHECK);
            suggestToUseCountComparison.addChangeListener(e -> SUGGEST_TO_USE_COUNT_CHECK = suggestToUseCountComparison.isSelected());
            optionsPanel.add(suggestToUseCountComparison, "wrap");

            suggestToUseNullComparison = new JCheckBox("Suggest to use null-comparison", SUGGEST_TO_USE_NULL_COMPARISON);
            suggestToUseNullComparison.addChangeListener(e -> SUGGEST_TO_USE_NULL_COMPARISON = suggestToUseNullComparison.isSelected());
            optionsPanel.add(suggestToUseNullComparison, "wrap");
        }

        JPanel getComponent() {
            return optionsPanel;
        }
    }
}
