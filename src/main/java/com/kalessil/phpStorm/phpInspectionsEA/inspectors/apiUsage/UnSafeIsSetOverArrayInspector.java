package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPlatformResolverUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashSet;

public class UnSafeIsSetOverArrayInspector extends BasePhpInspection {
    // configuration flags automatically saved by IDE
    @SuppressWarnings("WeakerAccess")
    public boolean SUGGEST_TO_USE_ARRAY_KEY_EXISTS = false;
    @SuppressWarnings("WeakerAccess")
    public boolean SUGGEST_TO_USE_NULL_COMPARISON = true;

    // static messages for triggered messages
    private static final String messageUseArrayKeyExists    = "'array_key_exists(...)' construction should be used for better data *structure* control.";
    private static final String messageUseNullComparison    = "'null === %s%' construction should be used instead.";
    private static final String messageUseNotNullComparison = "'null !== %s%' construction should be used instead.";
    private static final String messageConcatenationInIndex = "Concatenation is used in an index, it should be moved to a variable.";

    @NotNull
    public String getShortName() {
        return "UnSafeIsSetOverArrayInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIsset(PhpIsset issetExpression) {
                /*
                 * if no parameters, we don't check;
                 * if multiple parameters, perhaps if-inspection fulfilled and isset's were merged
                 *
                 * TODO: still needs analysis regarding concatenations in indexes
                 */
                if (issetExpression.getVariables().length != 1) {
                    return;
                }

                /* gather context information */
                PsiElement issetParent = issetExpression.getParent();
                boolean issetInverted  = false;
                if (issetParent instanceof UnaryExpression) {
                    final PsiElement operator = ((UnaryExpression) issetParent).getOperation();
                    if (null != operator && PhpTokenTypes.opNOT == operator.getNode().getElementType()) {
                        issetInverted = true;
                        issetParent   = issetParent.getParent();
                    }
                }
                boolean isResultStored = (issetParent instanceof AssignmentExpression || issetParent instanceof PhpReturn);

                /* do not report ternaries using isset-or-null semantics, there Array_key_exist can introduce bugs  */
                PsiElement conditionCandidate = issetInverted ? issetExpression.getParent() : issetExpression;
                boolean isTernaryCondition = issetParent instanceof TernaryExpression && conditionCandidate == ((TernaryExpression) issetParent).getCondition();
                if (isTernaryCondition) {
                    final TernaryExpression ternary = (TernaryExpression) issetParent;
                    final PsiElement nullCandidate  = issetInverted ? ternary.getTrueVariant() : ternary.getFalseVariant();
                    if (PhpLanguageUtil.isNull(nullCandidate)) {
                        return;
                    }
                }


                /* do analyze  */
                for (PsiElement parameter : issetExpression.getVariables()) {
                    parameter = ExpressionSemanticUtil.getExpressionTroughParenthesis(parameter);
                    if (null == parameter) {
                        continue;
                    }

                    if (!(parameter instanceof ArrayAccessExpression)) {
                        if (parameter instanceof FieldReference) {
                            /* if field is not resolved, it's probably dynamic and isset have a purpose */
                            final PsiReference referencedField = parameter.getReference();
                            final PsiElement resolvedField     = null == referencedField ? null : referencedField.resolve();
                            if (null == resolvedField || !(ExpressionSemanticUtil.getBlockScope(resolvedField) instanceof PhpClass)) {
                                continue;
                            }
                        }

                        if (SUGGEST_TO_USE_NULL_COMPARISON) {
                            final String message = (issetInverted ? messageUseNullComparison : messageUseNotNullComparison)
                                    .replace("%s%", parameter.getText());
                            holder.registerProblem(parameter, message, ProblemHighlightType.WEAK_WARNING);
                        }
                        continue;
                    }

                    /* TODO: has method/function reference as index */
                    if (!isResultStored && this.hasConcatenationAsIndex((ArrayAccessExpression) parameter)) {
                        holder.registerProblem(parameter, messageConcatenationInIndex, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        continue;
                    }

                    if (SUGGEST_TO_USE_ARRAY_KEY_EXISTS && !isArrayAccess((ArrayAccessExpression) parameter)) {
                        holder.registerProblem(parameter, messageUseArrayKeyExists, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }

            /* checks if any of indexes is concatenation expression */
            /* TODO: iterator for array access expression */
            private boolean hasConcatenationAsIndex(@NotNull ArrayAccessExpression expression) {
                PsiElement expressionToInspect = expression;
                while (expressionToInspect instanceof ArrayAccessExpression) {
                    ArrayIndex index = ((ArrayAccessExpression) expressionToInspect).getIndex();
                    if (null != index && index.getValue() instanceof BinaryExpression) {
                        PsiElement operation = ((BinaryExpression) index.getValue()).getOperation();
                        if (null != operation && operation.getNode().getElementType() == PhpTokenTypes.opCONCAT) {
                            return true;
                        }
                    }

                    expressionToInspect =  expressionToInspect.getParent();
                }

                return false;
            }

            // TODO: partially duplicates semanticalAnalysis.OffsetOperationsInspector.isContainerSupportsArrayAccess()
            private  boolean isArrayAccess(@NotNull ArrayAccessExpression expression) {
                // ok JB parses `$var[]= ...` always as array, lets make it working properly and report them later
                PsiElement container = expression.getValue();
                if (null == container) {
                    return false;
                }

                HashSet<String> containerTypes = new HashSet<>();
                TypeFromPlatformResolverUtil.resolveExpressionType(container, containerTypes);

                // failed to resolve, don't try to guess anything
                if (0 == containerTypes.size()) {
                    return false;
                }

                boolean supportsOffsets = false;
                for (String typeToCheck : containerTypes) {
                    // assume is just null-ble declaration or we shall just rust to mixed
                    if (typeToCheck.equals(Types.strNull)) {
                        continue;
                    }
                    if (typeToCheck.equals(Types.strMixed)) {
                        supportsOffsets = true;
                        continue;
                    }

                    // some of possible types are scalars, what's wrong
                    if (!StringUtil.isEmpty(typeToCheck) && typeToCheck.charAt(0) != '\\') {
                        supportsOffsets = false;
                        break;
                    }

                    // assume class has what is needed, OffsetOperationsInspector should report if not
                    supportsOffsets = true;
                }
                containerTypes.clear();

                return supportsOffsets;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return (new UnSafeIsSetOverArrayInspector.OptionsPanel()).getComponent();
    }

    public class OptionsPanel {
        final private JPanel optionsPanel;

        final private JCheckBox suggestToUseArrayKeyExists;
        final private JCheckBox suggestToUseNullComparison;

        public OptionsPanel() {
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new MigLayout());

            suggestToUseArrayKeyExists = new JCheckBox("Suggest to use array_key_exists()", SUGGEST_TO_USE_ARRAY_KEY_EXISTS);
            suggestToUseArrayKeyExists.addChangeListener(e -> SUGGEST_TO_USE_ARRAY_KEY_EXISTS = suggestToUseArrayKeyExists.isSelected());
            optionsPanel.add(suggestToUseArrayKeyExists, "wrap");

            suggestToUseNullComparison = new JCheckBox("Suggest to use null-comparison", SUGGEST_TO_USE_NULL_COMPARISON);
            suggestToUseNullComparison.addChangeListener(e -> SUGGEST_TO_USE_NULL_COMPARISON = suggestToUseNullComparison.isSelected());
            optionsPanel.add(suggestToUseNullComparison, "wrap");
        }

        public JPanel getComponent() {
            return optionsPanel;
        }
    }
}