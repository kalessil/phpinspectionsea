package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IfReturnReturnSimplificationInspector extends BasePhpInspection {
    private static final String strProblemDescription = "If and following return can be replaced with 'return %c%'";

    @NotNull
    public String getShortName() {
        return "IfReturnReturnSimplificationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {

                SimpleIfStatement statement = SimpleIfStatement.createFromStatement(ifStatement);
                if (statement == null) {
                    return;
                }

                String message = statement.isInverted ? strProblemDescription.replace("%c%", "!(%c%)") : strProblemDescription;
                message = message.replace("%c%", ifStatement.getCondition().getText());
                holder.registerProblem(ifStatement.getFirstChild(), message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix());
            }
        };
    }


    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Simplify if statement";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            // Detect target if statement
            PsiElement psiElement = descriptor.getPsiElement();
            if (psiElement == null) {
                return;
            }
            PsiElement ifStatement = psiElement.getParent();
            if (!(ifStatement instanceof If)) {
                return;
            }

            SimpleIfStatement simpleIfStatement = SimpleIfStatement.createFromStatement((If) ifStatement);
            if (simpleIfStatement == null) {
                return;
            }
            // We apply fix to the correct statement


            If targetStatement = simpleIfStatement.ifStatement;

            // Replace unnecessary whitespace nodes.
            PsiElement nextSibling = targetStatement.getNextSibling();
            if (nextSibling instanceof PsiWhiteSpace) {
                nextSibling.delete();
                nextSibling = targetStatement.getNextSibling();
            }

            // Replace last return
            if (nextSibling instanceof PhpReturn) {
                nextSibling.delete();
            }

            PhpPsiElement condition = targetStatement.getCondition();


            boolean useBrackets = false;

            if (simpleIfStatement.isInverted) {
                useBrackets = true;
            }

            // If we have multiple expression inside condition it is good to wrap it into the brackets
            List<PhpExpression> expressionList = PsiTreeUtil.getChildrenOfAnyType(condition, PhpExpression.class);
            if (expressionList.size() > 1) {
                useBrackets = true;
            }


            // Build our return statement
            String conditionText = condition.getText();
            if (useBrackets || simpleIfStatement.isInverted) {
                conditionText = "(" + conditionText + ")";
                if (simpleIfStatement.isInverted) {
                    conditionText = "!" + conditionText;
                }
            }

            // Replace if statement
            PhpReturn returnStatement = PhpPsiElementFactory.createReturnStatement(project, "return " + conditionText + ";");
            targetStatement.replace(returnStatement);
        }
    }


    /**
     * Wrapper around if statement. Used for inspector and fixer
     */
    private static class SimpleIfStatement {

        private final If ifStatement;
        private final boolean isInverted;

        SimpleIfStatement(If ifStatement, boolean isInverted) {
            this.ifStatement = ifStatement;
            this.isInverted = isInverted;
        }

        @Nullable
        static SimpleIfStatement createFromStatement(If ifStatement) {

            /** skip ifs with alternative branches */
            if (ExpressionSemanticUtil.hasAlternativeBranches(ifStatement)) {
                return null;
            }


            /** Skip ifs without group statement */
            GroupStatement objGroupStatement = ExpressionSemanticUtil.getGroupStatement(ifStatement);
            if (null == objGroupStatement) {
                return null;
            }


            /** or condition is not an binary expression */
            final PsiElement objCondition = ExpressionSemanticUtil.getExpressionTroughParenthesis(ifStatement.getCondition());
            /** or maybe try resolving type when not on-the-fly analysis is running */
            if (!(objCondition instanceof BinaryExpression)) {
                return null;
            }


            /** next expression is not return */
            PhpPsiElement objNextExpression = ifStatement.getNextPsiSibling();
            if (!(objNextExpression instanceof PhpReturn)) {
                return null;
            }

            /** or return not a boolean */
            PhpReturn objSecondReturn = (PhpReturn) objNextExpression;
            final boolean isSecondReturnUsesBool = (
                    objSecondReturn.getArgument() instanceof ConstantReference &&
                            ExpressionSemanticUtil.isBoolean((ConstantReference) objSecondReturn.getArgument())
            );
            if (!isSecondReturnUsesBool) {
                return null;
            }


            /** analyse if structure contains only one expression */
            int intCountExpressionsInCurrentGroup = ExpressionSemanticUtil.countExpressionsInGroup(objGroupStatement);
            if (intCountExpressionsInCurrentGroup != 1) {
                return null;
            }
            /** and it's a return expression */
            PhpReturn objFirstReturn = null;
            for (PsiElement objIfChild : objGroupStatement.getChildren()) {
                if (objIfChild instanceof PhpReturn) {
                    objFirstReturn = (PhpReturn) objIfChild;
                    break;
                }
            }
            if (null == objFirstReturn) {
                return null;
            }


            /** check if first return also boolean */
            final boolean isFirstReturnUsesBool = (
                    objFirstReturn.getArgument() instanceof ConstantReference &&
                            ExpressionSemanticUtil.isBoolean((ConstantReference) objFirstReturn.getArgument())
            );
            if (!isFirstReturnUsesBool) {
                return null;
            }

            /** point the problem out */
            boolean isInverted = PhpLangUtil.isFalse((ConstantReference) objFirstReturn.getArgument());

            return new SimpleIfStatement(ifStatement, isInverted);
        }

    }
}