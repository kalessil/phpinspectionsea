package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class SenselessParenthesesInspector extends BasePhpInspection {
    @NotNull
    public String getShortName() {
        return "SenselessParenthesesInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpParenthesizedExpression(ParenthesizedExpression expression) {
                String fileName = holder.getFile().getName();
                if (fileName.endsWith(".blade.php")) {
                    /* syntax injection there is not done properly for elseif, causing false-positives */
                    return;
                }

                PhpPsiElement argument = expression.getArgument();
                PsiElement parent      = expression.getParent();
                if (null == argument || null == parent) {
                    return;
                }

                /* Following will be reported:
                    argument instanceof StringLiteralExpression ||
                    argument instanceof Variable ||
                    argument instanceof PhpIsset ||
                    argument instanceof PhpUnset ||
                    argument instanceof PhpEmpty ||
                    argument instanceof MethodReference ||
                    argument instanceof ArrayAccessExpression ||
                    argument instanceof FieldReference ||
                    argument instanceof FunctionReference ||
                    argument instanceof ParenthesizedExpression || parent instanceof ParenthesizedExpression ||
                    parent instanceof If ||
                    parent instanceof ElseIf ||
                    parent instanceof ParameterList ||
                    parent instanceof PhpReturn ||
                    parent instanceof ForeachStatement ||
                    parent instanceof While ||
                    parent instanceof Constant ||
                    parent.getParent() instanceof ArrayHashElement ||
                    parent.getParent() instanceof ArrayCreationExpression ||
                    parent instanceof ArrayIndex
                */

                /*
                    this matrix mostly contains reasonable variants,
                    couple of them might be ambiguous, but let's keep logic simple
                */
                boolean knowsLegalCases = (
                    (
                        argument instanceof BinaryExpression   ||
                        argument instanceof TernaryExpression  ||
                        argument instanceof UnaryExpression    ||
                        argument instanceof AssignmentExpression
                    ) && (
                        parent instanceof BinaryExpression     ||
                        parent instanceof TernaryExpression    ||
                        parent instanceof UnaryExpression      ||
                        parent instanceof AssignmentExpression ||
                        parent instanceof PhpReturn
                    )
                );
                knowsLegalCases =
                    knowsLegalCases ||
                    /* some of questionable constructs, but lets start first with them */
                    parent instanceof Include ||
                    parent instanceof PhpCase ||
                    parent instanceof PhpEchoStatement ||
                    parent instanceof PhpPrintExpression ||
                    (parent instanceof ParameterList && argument instanceof TernaryExpression)
                ;
                if (knowsLegalCases) {
                    return;
                }

                holder.registerProblem(expression, "This parentheses can be removed to keep code more clear", ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
