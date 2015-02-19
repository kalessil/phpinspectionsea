package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class OpAssignShortSyntaxInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Can be safely refactored as '%v% %o%= %e%'";

    private static HashMap<IElementType, IElementType> mapping = null;
    private static HashMap<IElementType, IElementType> getMapping() {
        if (null == mapping) {
            mapping = new HashMap<IElementType, IElementType>();

            // todo: check when JB added constants for %
            mapping.put(PhpTokenTypes.opPLUS,        PhpTokenTypes.opPLUS_ASGN);
            mapping.put(PhpTokenTypes.opMINUS,       PhpTokenTypes.opMINUS_ASGN);
            mapping.put(PhpTokenTypes.opMUL,         PhpTokenTypes.opMUL_ASGN);
            mapping.put(PhpTokenTypes.opDIV,         PhpTokenTypes.opDIV_ASGN);
            mapping.put(PhpTokenTypes.opCONCAT,      PhpTokenTypes.opCONCAT_ASGN);
            mapping.put(PhpTokenTypes.opBIT_AND,     PhpTokenTypes.opBIT_AND_ASGN);
            mapping.put(PhpTokenTypes.opBIT_OR,      PhpTokenTypes.opBIT_OR_ASGN);
            mapping.put(PhpTokenTypes.opBIT_XOR,     PhpTokenTypes.opBIT_XOR_ASGN);
            mapping.put(PhpTokenTypes.opSHIFT_LEFT,  PhpTokenTypes.opSHIFT_LEFT_ASGN);
            mapping.put(PhpTokenTypes.opSHIFT_RIGHT, PhpTokenTypes.opSHIFT_RIGHT_ASGN);
        }

        return mapping;
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {
                PsiElement value = ExpressionSemanticUtil.getExpressionTroughParenthesis(assignmentExpression.getValue());
                /** try reaching operator in binary expression, expected as value */
                if (value instanceof BinaryExpression) {
                    BinaryExpression valueExpression = (BinaryExpression) value;
                    PsiElement objOperation = valueExpression.getOperation();
                    if (null != objOperation) {
                        HashMap<IElementType, IElementType> mapping = getMapping();

                        IElementType operation = objOperation.getNode().getElementType();
                        PsiElement leftOperand = valueExpression.getLeftOperand();
                        PsiElement rightOperand = valueExpression.getRightOperand();
                        PsiElement variable = assignmentExpression.getVariable();
                        /** ensure that's an operation we are looking for and pattern recognized */
                        if (
                            null != variable && null != leftOperand && null != rightOperand &&
                            mapping.containsKey(operation) &&
                            PsiEquivalenceUtil.areElementsEquivalent(variable, leftOperand)
                        ) {
                            String strMessage = strProblemDescription
                                    .replace("%v%", leftOperand.getText())
                                    .replace("%o%", objOperation.getText())
                                    .replace("%e%", rightOperand.getText());
                            holder.registerProblem(assignmentExpression, strMessage, ProblemHighlightType.WEAK_WARNING);
                        }
                    }
                }
            }
        };
    }
}
