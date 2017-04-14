package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.psi.elements.ControlStatement;
import com.jetbrains.php.lang.psi.elements.Else;
import com.jetbrains.php.lang.psi.elements.ElseIf;
import com.jetbrains.php.lang.psi.elements.If;
import com.jetbrains.php.lang.psi.elements.impl.UnaryExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

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
                    problemsHolder.registerProblem(statementCondition, suggestionMessage, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}
