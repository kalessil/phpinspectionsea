package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class IllusionOfChoiceInspector extends BasePhpInspection {

    static private final Set<IElementType> targetOperations = new HashSet<>();
    static {
        targetOperations.add(PhpTokenTypes.opIDENTICAL);
        targetOperations.add(PhpTokenTypes.opNOT_IDENTICAL);
        targetOperations.add(PhpTokenTypes.opEQUAL);
        targetOperations.add(PhpTokenTypes.opNOT_EQUAL);
        targetOperations.add(PhpTokenTypes.opLESS);
        targetOperations.add(PhpTokenTypes.opLESS_OR_EQUAL);
        targetOperations.add(PhpTokenTypes.opGREATER);
        targetOperations.add(PhpTokenTypes.opGREATER_OR_EQUAL);
    }

    @NotNull
    public String getShortName() {
        return "IllusionOfChoiceInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
            }

            @Override
            public void visitPhpIf(@NotNull If expression) {
                final PsiElement condition = expression.getCondition();
                if (condition instanceof BinaryExpression) {
                    final BinaryExpression binary = (BinaryExpression) condition;
                    if (targetOperations.contains(binary.getOperationType()) && expression.getElseIfBranches().length == 0) {
                        final GroupStatement ifBody = ExpressionSemanticUtil.getGroupStatement(expression);
                        final PsiElement ifLast     = ifBody == null ? null : ExpressionSemanticUtil.getLastStatement(ifBody);
                        if (ifLast instanceof PhpReturn && ExpressionSemanticUtil.countExpressionsInGroup(ifBody) == 1) {
                            final PsiElement elseStatement = expression.getElseBranch();
                            if (elseStatement != null) {
                                /* both if-else are returns */
                                final GroupStatement elseBody = ExpressionSemanticUtil.getGroupStatement(elseStatement);
                                final PsiElement elseLast     = elseBody == null ? null : ExpressionSemanticUtil.getLastStatement(elseBody);
                                if (elseLast instanceof PhpReturn) {
                                    final PsiElement ifReturnValue   = ExpressionSemanticUtil.getReturnValue((PhpReturn) ifLast);
                                    final PsiElement elseReturnValue = ExpressionSemanticUtil.getReturnValue((PhpReturn) elseLast);
                                    if (ifReturnValue != null && elseReturnValue != null) {
                                        this.analyze(expression.getFirstChild(), ifReturnValue, elseReturnValue);
                                    }
                                }
                            } else {
                                /* both if-next are returns */
                            }
                        }
                    }
                }
            }

            private void analyze(@NotNull PsiElement target, @NotNull PsiElement trueVarian, @NotNull PsiElement falseVarian) {
                holder.registerProblem(target, "Nope!");
            }
        };
    }

    /* SenselessTernaryOperatorInspector + https://github.com/kalessil/phpinspectionsea/issues/869 */

}
