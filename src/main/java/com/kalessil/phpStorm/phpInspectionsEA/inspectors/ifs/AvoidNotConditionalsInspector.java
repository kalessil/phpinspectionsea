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
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.GroupStatementImpl;
import com.jetbrains.php.lang.psi.elements.impl.UnaryExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class AvoidNotConditionalsInspector extends BasePhpInspection {
    private static final String suggestionMessage = "This negative if conditional could be avoided";

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpElse(final Else elseStatement) {
                // Ignores if `else` is on reality an `else if` (note the spacing), because this `if` will checked after.
                if (elseStatement.getStatement() instanceof If) {
                    return;
                }

                // Basically, get the `if` related to `else`.
                ControlStatement controlStatement = (ControlStatement) elseStatement.getParent();

                // If this `if` have `elseif` branches, then will suggests over the last branch only.
                final List<ElseIf> controlElseIfs = Arrays.asList(((If) controlStatement).getElseIfBranches());
                if (controlElseIfs.size() > 0) {
                    controlStatement = controlElseIfs.get(controlElseIfs.size() - 1);
                }

                // Then get the condition, and continues only if it is an Unary Expression.
                final PsiElement statementCondition = controlStatement.getCondition();
                if (!(statementCondition instanceof UnaryExpressionImpl)) {
                    return;
                }

                // Finally, check if this Unary Expression uses the not-operator.
                final LeafPsiElement conditionOperation = (LeafPsiElement) ((UnaryExpressionImpl) statementCondition).getOperation();
                if (null != conditionOperation && conditionOperation.getText().equals("!")) {
                    problemsHolder.registerProblem(statementCondition, suggestionMessage, ProblemHighlightType.WEAK_WARNING,
                                                   new TheLocalQuickFix(controlStatement, elseStatement, (UnaryExpressionImpl) statementCondition));
                }
            }
        };
    }

    class TheLocalQuickFix implements LocalQuickFix {

        private final SmartPsiElementPointer<ControlStatement>    controlStatement;
        private final SmartPsiElementPointer<Else>                elseStatement;
        private final SmartPsiElementPointer<UnaryExpressionImpl> unaryExpression;

        TheLocalQuickFix(@NotNull final ControlStatement controlStatement, @NotNull  final Else elseStatement, final UnaryExpressionImpl unaryExpression) {
            super();

            final SmartPointerManager factory = SmartPointerManager.getInstance(controlStatement.getProject());

            this.controlStatement = factory.createSmartPsiElementPointer(controlStatement, controlStatement.getContainingFile());
            this.elseStatement = factory.createSmartPsiElementPointer(elseStatement, elseStatement.getContainingFile());
            this.unaryExpression = factory.createSmartPsiElementPointer(unaryExpression, unaryExpression.getContainingFile());
        }

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "Flip if-else to avoid not-operator";
        }

        @Nls
        @NotNull
        @Override
        public String getName() {
            return getFamilyName();
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor problemDescriptor) {
            final ControlStatement    controlStatementElement = controlStatement.getElement();
            final Else                elseStatementElement    = elseStatement.getElement();
            final UnaryExpressionImpl unaryExpressionElement  = unaryExpression.getElement();

            if (null == controlStatementElement || null == elseStatementElement || null == unaryExpressionElement) {
                return;
            }

            final PsiElement unaryExpressionOperation = unaryExpressionElement.getOperation();
            if (null == unaryExpressionOperation) {
                return;
            }

            unaryExpressionOperation.delete();

            final Statement controlTheStatement = controlStatementElement.getStatement();
            final Statement elseTheStatement    = (Statement) elseStatementElement.getStatement();

            if (null == controlTheStatement || null == elseTheStatement) {
                return;
            }

            final Statement controlNewStatement = getStatementAsText(project, controlTheStatement);
            final Statement elseNewStatement    = getStatementAsText(project, elseTheStatement);

            controlTheStatement.replace(elseNewStatement);
            elseTheStatement.replace(controlNewStatement);
        }

        @NotNull
        private Statement getStatementAsText(@NotNull final Project project, final Statement controlTheStatement) {
            final String controlStatementText = controlTheStatement instanceof GroupStatementImpl
                                                ? controlTheStatement.getText()
                                                : '{' + controlTheStatement.getText() + '}';

            return PhpPsiElementFactory.createStatement(project, controlStatementText);
        }
    }
}
