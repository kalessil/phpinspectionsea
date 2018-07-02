package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class OpAssignShortSyntaxInspector extends BasePhpInspection {
    private static final String messagePattern = "Can be safely refactored as '%r%'.";

    @NotNull
    public String getShortName() {
        return "OpAssignShortSyntaxInspection";
    }

    private static final HashMap<IElementType, IElementType> mapping = new HashMap<>();
    static {
        mapping.put(PhpTokenTypes.opPLUS,        PhpTokenTypes.opPLUS_ASGN);
        mapping.put(PhpTokenTypes.opMINUS,       PhpTokenTypes.opMINUS_ASGN);
        mapping.put(PhpTokenTypes.opMUL,         PhpTokenTypes.opMUL_ASGN);
        mapping.put(PhpTokenTypes.opDIV,         PhpTokenTypes.opDIV_ASGN);
        mapping.put(PhpTokenTypes.opREM,         PhpTokenTypes.opREM_ASGN);
        mapping.put(PhpTokenTypes.opCONCAT,      PhpTokenTypes.opCONCAT_ASGN);
        mapping.put(PhpTokenTypes.opBIT_AND,     PhpTokenTypes.opBIT_AND_ASGN);
        mapping.put(PhpTokenTypes.opBIT_OR,      PhpTokenTypes.opBIT_OR_ASGN);
        mapping.put(PhpTokenTypes.opBIT_XOR,     PhpTokenTypes.opBIT_XOR_ASGN);
        mapping.put(PhpTokenTypes.opSHIFT_LEFT,  PhpTokenTypes.opSHIFT_LEFT_ASGN);
        mapping.put(PhpTokenTypes.opSHIFT_RIGHT, PhpTokenTypes.opSHIFT_RIGHT_ASGN);
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {
                PsiElement value = ExpressionSemanticUtil.getExpressionTroughParenthesis(assignmentExpression.getValue());
                /* try reaching operator in binary expression, expected as value */
                if (value instanceof BinaryExpression) {
                    final BinaryExpression valueExpression = (BinaryExpression) value;
                    final PsiElement operator              = valueExpression.getOperation();
                    if (null != operator) {
                        final IElementType operation  = operator.getNode().getElementType();
                        final PsiElement leftOperand  = valueExpression.getLeftOperand();
                        final PsiElement rightOperand = valueExpression.getRightOperand();
                        final PsiElement variable     = assignmentExpression.getVariable();
                        /* ensure that's an operation we are looking for and pattern recognized */
                        if (
                            null != variable && null != leftOperand && null != rightOperand &&
                            mapping.containsKey(operation) &&
                            OpenapiEquivalenceUtil.areEqual(variable, leftOperand)
                        ) {
                            final String replacement = "%v% %o%= %e%"
                                .replace("%e%", rightOperand.getText())
                                .replace("%o%", operator.getText())
                                .replace("%v%", leftOperand.getText());
                            final String message = messagePattern.replace("%r%", replacement);
                            holder.registerProblem(assignmentExpression, message, new UseShorthandOperatorFix(replacement));
                        }
                    }
                }
            }
        };
    }

    private static final class UseShorthandOperatorFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use the short notation";

        UseShorthandOperatorFix(@NotNull String suggestedReplacement) {
            super(suggestedReplacement);
        }

        @NotNull
        @Override
        public String getName() {
            return title;
        }
    }
}
