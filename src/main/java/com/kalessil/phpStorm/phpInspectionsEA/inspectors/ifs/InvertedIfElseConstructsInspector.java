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
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
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

public class InvertedIfElseConstructsInspector extends BasePhpInspection {
    private static final String message = "The if-else workflow is driven by inverted conditions, consider avoiding invertions.";

    @NotNull
    public String getShortName() {
        return "InvertedIfElseConstructsInspection";
    }

    @NotNull
    @Override
    public final PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpElse(@NotNull Else elseStatement) {
                final PsiElement elseBody = elseStatement.getStatement();
                if (elseBody instanceof GroupStatement) {
                    /* find if/elseif as main branch */
                    ControlStatement ifStatement = (ControlStatement) elseStatement.getParent();
                    final ElseIf[] elseIfs       = ((If) ifStatement).getElseIfBranches();
                    if (elseIfs.length > 0) {
                        ifStatement = elseIfs[elseIfs.length - 1];
                    }

                    final PsiElement ifBody    = ifStatement.getStatement();
                    final PsiElement condition = ifStatement.getCondition();
                    if (ifBody instanceof GroupStatement && condition instanceof UnaryExpression) {
                        final PsiElement operation = ((UnaryExpression) condition).getOperation();
                        if (operation != null && operation.getNode().getElementType() == PhpTokenTypes.opNOT) {
                            problemsHolder.registerProblem(
                                elseStatement.getFirstChild(),
                                message,
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                new NormalizeWorkflowFix((GroupStatement) ifBody, (GroupStatement) elseBody, (UnaryExpression) condition)
                            );
                        }
                    }
                }
            }
        };
    }

    private static class NormalizeWorkflowFix implements LocalQuickFix {
        private final SmartPsiElementPointer<GroupStatement> ifBody;
        private final SmartPsiElementPointer<GroupStatement> elseBody;
        private final SmartPsiElementPointer<UnaryExpression> condition;

        NormalizeWorkflowFix(@NotNull GroupStatement ifBody, @NotNull GroupStatement elseBody, @NotNull UnaryExpression condition) {
            final SmartPointerManager factory = SmartPointerManager.getInstance(condition.getProject());

            this.ifBody    = factory.createSmartPsiElementPointer(ifBody);
            this.elseBody  = factory.createSmartPsiElementPointer(elseBody);
            this.condition = factory.createSmartPsiElementPointer(condition);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return "Flip if-else to avoid not-operator";
        }

        @NotNull
        @Override
        public String getName() {
            return getFamilyName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            final PsiElement ifBody         = this.ifBody.getElement();
            final PsiElement elseBody       = this.elseBody.getElement();
            final UnaryExpression condition = this.condition.getElement();

            if (ifBody != null && elseBody != null && condition != null) {
                final PsiElement unwrappedCondition = ExpressionSemanticUtil.getExpressionTroughParenthesis(condition.getValue());
                if (unwrappedCondition != null) {
                    condition.replace(unwrappedCondition);

                    final PsiElement ifBodyCopy = ifBody.copy();
                    ifBody.replace(elseBody);
                    elseBody.replace(ifBodyCopy);
                }
            }
        }
    }
}
