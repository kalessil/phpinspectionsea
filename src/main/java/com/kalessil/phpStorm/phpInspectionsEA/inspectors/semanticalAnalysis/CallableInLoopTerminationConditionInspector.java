package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;

import java.util.Collection;

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

public class CallableInLoopTerminationConditionInspector extends BasePhpInspection {
    private static final String message = "Avoid callables in loop conditionals for better performance.";

    @NotNull
    public String getShortName() {
        return "CallableInLoopTerminationConditionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFor(final For forStatement) {
                /* TODO: re-evaluate searching in tree for catching more cases */
                final PhpPsiElement[] conditions = forStatement.getConditionalExpressions();

                if ((conditions.length != 1) ||
                    !(conditions[0] instanceof BinaryExpression)) {
                    return;
                }

                final BinaryExpression condition = (BinaryExpression) conditions[0];

                if (OpenapiTypesUtil.isFunctionReference(condition.getRightOperand()) ||
                    OpenapiTypesUtil.isFunctionReference(condition.getLeftOperand())) {
                    problemsHolder.registerProblem(condition, message, ProblemHighlightType.GENERIC_ERROR,
                                                   new TheLocalFix(forStatement, condition));
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        private final SmartPsiElementPointer<For>              forStatement;
        private final SmartPsiElementPointer<BinaryExpression> condition;

        TheLocalFix(@NotNull final For forStatement, final BinaryExpression condition) {
            final SmartPointerManager factory = SmartPointerManager.getInstance(forStatement.getProject());

            this.forStatement = factory.createSmartPsiElementPointer(forStatement);
            this.condition = factory.createSmartPsiElementPointer(condition);
        }

        @NotNull
        @Override
        public String getName() {
            return "Store the callable result to a variable";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final For              forStatementElement = forStatement.getElement();
            final BinaryExpression conditionElement    = condition.getElement();

            assert forStatementElement != null;
            assert conditionElement != null;

            final boolean    functionOnLeft     = conditionElement.getLeftOperand() instanceof FunctionReference;
            final PsiElement referenceCandidate = functionOnLeft ? conditionElement.getLeftOperand() : conditionElement.getRightOperand();
            final PsiElement variableCandidate  = functionOnLeft ? conditionElement.getRightOperand() : conditionElement.getLeftOperand();
            final PsiElement operation          = conditionElement.getOperation();


            assert operation != null;
            assert variableCandidate != null;
            assert referenceCandidate != null;

            final String variableName = (variableCandidate instanceof Variable)
                                        ? ('$' + ((Variable) variableCandidate).getName() + "Max")
                                        : "$loopsMax";
            final Variable variableElement = PhpPsiElementFactory.createFromText(project, Variable.class, variableName);

            assert variableElement != null;

            referenceCandidate.replace(variableElement);

            final PhpPsiElement[] initialExpressions = forStatementElement.getInitialExpressions();
            final AssignmentExpression assignmentInitializer =
                PhpPsiElementFactory.createFromText(project, AssignmentExpression.class, variableName + " = " + referenceCandidate.getText());

            assert assignmentInitializer != null;

            // Case #1 and #2: have at least one initial expression.
            if (initialExpressions.length >= 1) {
                final PhpPsiElement  lastExpression         = initialExpressions[initialExpressions.length - 1];
                final LeafPsiElement commaBeforeInitializer = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, ",");

                assert commaBeforeInitializer != null;

                forStatementElement.addAfter(assignmentInitializer, lastExpression);
                forStatementElement.addAfter(commaBeforeInitializer, lastExpression);

                return;
            }

            // Case #3: don't have any initial expression (eg. for(; ...)).
            // As For.class have no way to access the initial expression "container" when it is empty, then we need hard code that.
            final Collection<LeafPsiElement> forStatementLeafs = PsiTreeUtil.findChildrenOfType(forStatementElement, LeafPsiElement.class);
            for (final LeafPsiElement forStatementLeaf : forStatementLeafs) {
                if ("(".equals(forStatementLeaf.getText())) {
                    forStatementElement.addAfter(assignmentInitializer, forStatementLeaf);
                    break;
                }
            }
        }
    }
}
