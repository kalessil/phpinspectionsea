package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPlatformResolverUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.HashSet;

public class UnSafeIsSetOverArrayInspector extends BasePhpInspection {
    // configuration flags automatically saved by IDE
    public boolean SUGGEST_TO_USE_ARRAY_KEY_EXISTS = true;
    public boolean SUGGEST_TO_USE_NULL_COMPARISON = true;

    // static messages for triggered messages
    private static final String strProblemDescription                     = "Probably 'array_key_exists(...)' construction should be used for better data *structure* control";
    private static final String strProblemDescriptionUseNullComparison    = "Probably it can be 'null === %s%' construction used instead";
    private static final String strProblemDescriptionUseNotNullComparison = "Probably it can be 'null !== %s%' construction used instead";
    private static final String strProblemDescriptionConcatenationInIndex = "Concatenation is used as an index, should be moved to a variable";

    @NotNull
    public String getShortName() {
        return "UnSafeIsSetOverArrayInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIsset(PhpIsset issetExpression) {
                /*
                 * if no parameters, we shall not check;
                 * if multiple parameters, perhaps if-inspection fulfilled and issets were merged
                 */
                if (issetExpression.getVariables().length != 1) {
                    return;
                }

                /* gather context information */
                PsiElement issetParent       = issetExpression.getParent();
                final boolean isResultStored = (issetParent instanceof AssignmentExpression || issetParent instanceof PhpReturn);
                boolean issetInverted        = false;
                if (issetParent instanceof UnaryExpression) {
                    PsiElement objOperation = ((UnaryExpression) issetParent).getOperation();
                    if (null != objOperation && PhpTokenTypes.opNOT == objOperation.getNode().getElementType()) {
                        issetInverted = true;
                    }

                    issetParent = issetParent.getParent();
                }

                /* do not report ternaries using isset-or-null semantics, there Array_key_exist can introduce bugs  */
                boolean isTernaryCondition = issetParent instanceof TernaryExpression && issetExpression == ((TernaryExpression) issetParent).getCondition();
                if (isTernaryCondition) {
                    TernaryExpression ternary = (TernaryExpression) issetParent;
                    PsiElement nullCandidate  = issetInverted ? ternary.getTrueVariant() : ternary.getFalseVariant();
                    if (nullCandidate instanceof ConstantReference && PhpLangUtil.isNull((ConstantReference) nullCandidate)) {
                        return;
                    }
                }


                for (PsiElement parameter : issetExpression.getVariables()) {
                    parameter = ExpressionSemanticUtil.getExpressionTroughParenthesis(parameter);
                    if (null == parameter) {
                        continue;
                    }

                    if (!(parameter instanceof ArrayAccessExpression)) {
                        if (parameter instanceof FieldReference) {
                            FieldReference issetArgument = (FieldReference) parameter;
                            /* if field is not resolved, it's probably dynamic and isset have a purpose */
                            if (null == issetArgument.getReference() || null == issetArgument.getReference().resolve()) {
                                continue;
                            }
                        }

                        if (SUGGEST_TO_USE_NULL_COMPARISON) {
                            /* decide which message to use */
                            String strError = strProblemDescriptionUseNotNullComparison;
                            if (issetInverted) {
                                strError = strProblemDescriptionUseNullComparison;
                            }
                            /* personalize message for each parameter */
                            strError = strError.replace("%s%", parameter.getText());

                            holder.registerProblem(parameter, strError, ProblemHighlightType.WEAK_WARNING);
                        }
                        continue;
                    }

                    /** TODO: has method/function reference as index */
                    if (!isResultStored && this.hasConcatenationAsIndex((ArrayAccessExpression) parameter)) {
                        holder.registerProblem(parameter, strProblemDescriptionConcatenationInIndex, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        continue;
                    }

                    if (SUGGEST_TO_USE_ARRAY_KEY_EXISTS && !isArrayAccess((ArrayAccessExpression) parameter)) {
                        holder.registerProblem(parameter, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }

            /** checks if any of indexes is concatenation expression */
            /** TODO: iterator for array access expression */
            private boolean hasConcatenationAsIndex(ArrayAccessExpression objExpression) {
                PsiElement objExpressionToInspect = objExpression;
                while (objExpressionToInspect instanceof ArrayAccessExpression) {
                    ArrayIndex objIndex = ((ArrayAccessExpression) objExpressionToInspect).getIndex();
                    if (null != objIndex && objIndex.getValue() instanceof BinaryExpression) {
                        PsiElement objOperation = ((BinaryExpression) objIndex.getValue()).getOperation();
                        if (null != objOperation && objOperation.getNode().getElementType() == PhpTokenTypes.opCONCAT) {
                            return true;
                        }
                    }

                    objExpressionToInspect =  objExpressionToInspect.getParent();
                }

                return false;
            }

            // partially duplicates semanticalAnalysis.OffsetOperationsInspector.isContainerSupportsArrayAccess()
            private  boolean isArrayAccess(@NotNull ArrayAccessExpression expression) {
                // ok JB parses `$var[]= ...` always as array, lets make it working properly and report them later
                PsiElement container = expression.getValue();
                if (null == container) {
                    return false;
                }

                HashSet<String> containerTypes = new HashSet<String>();
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

                    // assume class has what's needed, OffsetOperationsInspector shall report if not
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
            suggestToUseArrayKeyExists.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    SUGGEST_TO_USE_ARRAY_KEY_EXISTS = suggestToUseArrayKeyExists.isSelected();
                }
            });
            optionsPanel.add(suggestToUseArrayKeyExists, "wrap");

            suggestToUseNullComparison = new JCheckBox("Suggest to use null-comparison", SUGGEST_TO_USE_NULL_COMPARISON);
            suggestToUseNullComparison.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    SUGGEST_TO_USE_NULL_COMPARISON = suggestToUseNullComparison.isSelected();
                }
            });
            optionsPanel.add(suggestToUseNullComparison, "wrap");
        }

        public JPanel getComponent() {
            return optionsPanel;
        }
    }
}