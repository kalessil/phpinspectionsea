package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops;

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
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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
                final PhpPsiElement[] conditions = forStatement.getConditionalExpressions();
                if (conditions.length == 1 && conditions[0] instanceof BinaryExpression) {
                    final BinaryExpression condition = (BinaryExpression) conditions[0];
                    if (
                        OpenapiTypesUtil.isFunctionReference(condition.getRightOperand()) ||
                        OpenapiTypesUtil.isFunctionReference(condition.getLeftOperand())
                    ) {
                        problemsHolder.registerProblem(condition, message, ProblemHighlightType.GENERIC_ERROR, new TheLocalFix(forStatement, condition));
                    }
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Reduce the repetitive calls";

        private final SmartPsiElementPointer<For> forStatement;
        private final SmartPsiElementPointer<BinaryExpression> condition;

        TheLocalFix(@NotNull For forStatement, @NotNull BinaryExpression condition) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(forStatement.getProject());

            this.forStatement = factory.createSmartPsiElementPointer(forStatement);
            this.condition    = factory.createSmartPsiElementPointer(condition);
        }

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final For forStatement           = this.forStatement.getElement();
            final BinaryExpression condition = this.condition.getElement();
            if (forStatement == null || condition == null) {
                return;
            }

            final boolean functionOnLeft        = condition.getLeftOperand() instanceof FunctionReference;
            final PsiElement referenceCandidate = functionOnLeft ? condition.getLeftOperand() : condition.getRightOperand();
            final PsiElement variableCandidate  = functionOnLeft ? condition.getRightOperand() : condition.getLeftOperand();
            if (variableCandidate == null || referenceCandidate == null) {
                return;
            }

            String variableName             = (variableCandidate instanceof Variable) ? ((Variable) variableCandidate).getName() : "loops";
            variableName                    = '$' + variableName + "Max";
            final Variable variableElement  = PhpPsiElementFactory.createFromText(project, Variable.class, variableName);
            final AssignmentExpression init = PhpPsiElementFactory.createFromText(project, AssignmentExpression.class, variableName + " = " + referenceCandidate.getText());
            if (variableElement == null || init == null) {
                return;
            }

            referenceCandidate.replace(variableElement);

            // Case #1 and #2: have at least one initial expression.
            final PhpPsiElement[] initialExpressions = forStatement.getInitialExpressions();
            if (initialExpressions.length > 0) {
                final LeafPsiElement commaBeforeInitializer = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, ",");
                if (commaBeforeInitializer != null) {
                    final PhpPsiElement lastExpression = initialExpressions[initialExpressions.length - 1];
                    forStatement.addAfter(init, lastExpression);
                    forStatement.addAfter(commaBeforeInitializer, lastExpression);
                }
                return;
            }

            // Case #3: don't have any initial expression (eg. for(; ...)).
            // As For.class have no way to access the initial expression "container" when it is empty, then we need hard code that.
            for (final LeafPsiElement leaf : PsiTreeUtil.findChildrenOfType(forStatement, LeafPsiElement.class)) {
                if (leaf.getElementType() == PhpTokenTypes.chLPAREN) {
                    forStatement.addAfter(init, leaf);
                    break;
                }
            }
        }
    }
}
