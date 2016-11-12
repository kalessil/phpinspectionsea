package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.UnaryExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class IfReturnReturnSimplificationInspector extends BasePhpInspection {
    private static final String messagePattern = "If and following return can be replaced with 'return %c%'";

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
                final GroupStatement objGroupStatement = ExpressionSemanticUtil.getGroupStatement(ifStatement);
                if (null == objGroupStatement) {
                    return;
                }


                /* or condition is not an binary expression */
                final PsiElement conditions = ExpressionSemanticUtil.getExpressionTroughParenthesis(ifStatement.getCondition());
                /* or maybe try resolving type when not on-the-fly analysis is running */
                /* TODO: function/method/property/constant reference, ternary and etc? Do we check not null?  */
                if (!(conditions instanceof BinaryExpression)) {
                    return;
                }


                /* next expression is not return */
                final PsiElement objNextExpression = ifStatement.getNextPsiSibling();
                if (!(objNextExpression instanceof PhpReturn)) {
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
                final PhpReturn secondReturn = (PhpReturn) objNextExpression;
                final boolean isSecondReturnUsesBool = (
                    secondReturn.getArgument() instanceof ConstantReference &&
                    ExpressionSemanticUtil.isBoolean((ConstantReference) secondReturn.getArgument())
                );
                if (!isSecondReturnUsesBool) {
                    return;
                }


                /* analyse if structure contains only one expression */
                final int intCountExpressionsInCurrentGroup = ExpressionSemanticUtil.countExpressionsInGroup(objGroupStatement);
                if (intCountExpressionsInCurrentGroup != 1) {
                    return;
                }
                /* and it's a return expression */
                PhpReturn firstReturn = null;
                for (PsiElement objIfChild : objGroupStatement.getChildren()) {
                    if (objIfChild instanceof PhpReturn) {
                        firstReturn = (PhpReturn) objIfChild;
                        break;
                    }
                }
                if (null == firstReturn) {
                    return;
                }


                /* check if first return also boolean */
                final boolean isFirstReturnUsesBool = (
                    firstReturn.getArgument() instanceof ConstantReference &&
                    ExpressionSemanticUtil.isBoolean((ConstantReference) firstReturn.getArgument())
                );
                if (!isFirstReturnUsesBool) {
                    return;
                }


                /* point the problem out */
                final boolean isInverted = PhpLangUtil.isFalse((ConstantReference) firstReturn.getArgument());
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

                /* inverting will be needed, check if it leads to double inverting */
                /*
                if (this.isInverted && conditions instanceof UnaryExpression) {
                    final PsiElement operation =((UnaryExpression) conditions).getOperation();
                    if (null != operation && PhpTokenTypes.opNOT == operation.getNode().getElementType()) {
                        *//* un-box condition and cancel inverting *//*
                        final PsiElement unboxedConditions = ((UnaryExpression) conditions).getValue();
                        if (null != unboxedConditions) {
                            invertCondition = false;
                            conditions = unboxedConditions;
                        }
                    }
                }
                */

                final PsiElement replacement;
                if (invertCondition) {
                    final String pattern = "!(" + conditions.getText() + ")";
                    replacement = PhpPsiElementFactory.createFromText(project, UnaryExpressionImpl.class, pattern);
                } else {
                    replacement = conditions.copy();
                }

                /* all good, modify code */
                if (null != replacement) {
                    /* fix return first */
                    returnArgument.replace(replacement);

                    /* now drop if and succeeding whitespaces */
                    if (ifExpression.getNextSibling() instanceof PsiWhiteSpaceImpl) {
                        ifExpression.getNextSibling().delete();
                    }
                    ifExpression.delete();
                }
            }
        }
    }
}