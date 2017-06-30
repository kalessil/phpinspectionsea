package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class IfReturnReturnSimplificationInspector extends BasePhpInspection {
    private static final String messagePattern = "An if-return construct can be replaced with 'return %c%'.";

    @NotNull
    public String getShortName() {
        return "IfReturnReturnSimplificationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {
                /* skip ifs with alternative branches */
                if (ExpressionSemanticUtil.hasAlternativeBranches(ifStatement)) {
                    return;
                }


                /* Skip ifs without group statement */
                final GroupStatement groupStatement = ExpressionSemanticUtil.getGroupStatement(ifStatement);
                if (null == groupStatement) {
                    return;
                }


                /* or condition is not an binary expression */
                final PsiElement conditions = ExpressionSemanticUtil.getExpressionTroughParenthesis(ifStatement.getCondition());
                /* or maybe try resolving type when not on-the-fly analysis is running */
                /* TODO: resolve type of other expressions */
                if (!(conditions instanceof BinaryExpression)) {
                    return;
                }


                /* next expression is not return */
                final PsiElement nextExpression = ifStatement.getNextPsiSibling();
                if (!(nextExpression instanceof PhpReturn)) {
                    return;
                }


                /* when if has preceding if-return we assume it's code style */
                final PsiElement previousExpression = ifStatement.getPrevPsiSibling();
                if (previousExpression instanceof If && !ExpressionSemanticUtil.hasAlternativeBranches((If) previousExpression)) {
                    final GroupStatement previousIfBody = ExpressionSemanticUtil.getGroupStatement(previousExpression);
                    if (null != previousIfBody) {
                        final PsiElement previousReturnCandidate = ExpressionSemanticUtil.getLastStatement(previousIfBody);
                        if (previousReturnCandidate instanceof PhpReturn) {
                            return;
                        }
                    }
                }


                /* or return not a boolean */
                final PhpReturn secondReturn         = (PhpReturn) nextExpression;
                final boolean isSecondReturnUsesBool = PhpLanguageUtil.isBoolean(secondReturn.getArgument());
                if (!isSecondReturnUsesBool) {
                    return;
                }


                /* analyse if structure contains only one expression */
                final int intCountExpressionsInCurrentGroup = ExpressionSemanticUtil.countExpressionsInGroup(groupStatement);
                if (intCountExpressionsInCurrentGroup != 1) {
                    return;
                }
                /* and it's a return expression */
                PhpReturn firstReturn = null;
                for (PsiElement ifChild : groupStatement.getChildren()) {
                    if (ifChild instanceof PhpReturn) {
                        firstReturn = (PhpReturn) ifChild;
                        break;
                    }
                }
                if (null == firstReturn) {
                    return;
                }


                /* check if first return also boolean */
                final boolean isFirstReturnUsesBool = PhpLanguageUtil.isBoolean(firstReturn.getArgument());
                if (!isFirstReturnUsesBool) {
                    return;
                }


                /* point the problem out */
                final boolean isInverted = PhpLanguageUtil.isFalse(firstReturn.getArgument());
                String message = isInverted ? messagePattern.replace("%c%", "!(%c%)") : messagePattern;

                message = message.replace("%c%", ifStatement.getCondition().getText());
                holder.registerProblem(ifStatement.getFirstChild(), message, ProblemHighlightType.WEAK_WARNING,
                        new TheLocalFix(ifStatement, secondReturn, isInverted));
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private SmartPsiElementPointer<If> ifExpression;
        final private SmartPsiElementPointer<PhpReturn> returnExpression;
        final boolean isInverted;

        TheLocalFix(@NotNull If ifExpression, @NotNull PhpReturn returnExpression, boolean isInverted) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(ifExpression.getProject());

            this.ifExpression     = factory.createSmartPsiElementPointer(ifExpression, ifExpression.getContainingFile());
            this.returnExpression = factory.createSmartPsiElementPointer(returnExpression, returnExpression.getContainingFile());
            this.isInverted       = isInverted;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use suggested simplification";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final If ifExpression            = this.ifExpression.getElement();
            final PhpReturn returnExpression = this.returnExpression.getElement();
            if (null == ifExpression || null == returnExpression) {
                return;
            }

            PsiElement conditions           = ExpressionSemanticUtil.getExpressionTroughParenthesis(ifExpression.getCondition());
            final PsiElement returnArgument = returnExpression.getArgument();
            if (null != conditions && null != returnArgument) {
                @SuppressWarnings("UnnecessaryLocalVariable")
                boolean invertCondition = this.isInverted;

                final PsiElement replacement;
                if (invertCondition) {
                    final String pattern = "(!(" + conditions.getText() + "))";
                    replacement
                        = PhpPsiElementFactory.createPhpPsiFromText(project, ParenthesizedExpression.class, pattern).getArgument();
                } else {
                    replacement = conditions.copy();
                }


                /* all good, modify code */
                if (null != replacement) {
                    /* fix return first */
                    returnArgument.replace(replacement);

                    /* now drop if and succeeding whitespaces */
                    if (ifExpression.getNextSibling() instanceof PsiWhiteSpace) {
                        ifExpression.getNextSibling().delete();
                    }
                    ifExpression.delete();
                }
            }
        }
    }
}