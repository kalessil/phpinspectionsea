package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class NestedNotOperatorsInspector extends BasePhpInspection {
    private static final String strProblemBoolCasting = "Can be replaced with (bool)%e%";
    private static final String strProblemSingleNot = "Can be replaced with !%e%";

    @NotNull
    public String getShortName() {
        return "NestedNotOperatorsInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpUnaryExpression(UnaryExpression expr) {
                /* process ony not operations */
                PsiElement operator = expr.getOperation();
                if (null == operator || operator.getNode().getElementType() != PhpTokenTypes.opNOT) {
                    return;
                }

                /* process only deepest not-operator */
                PhpPsiElement value = expr.getValue();
                if (value instanceof UnaryExpression) {
                    operator = ((UnaryExpression) value).getOperation();
                    if (null != operator && operator.getNode().getElementType() == PhpTokenTypes.opNOT) {
                        return;
                    }
                }

                /* check nesting level */
                PsiElement target = null;
                int nestingLevel = 1;
                PsiElement parent = expr.getParent();
                while (parent instanceof UnaryExpression) {
                    expr = (UnaryExpression) parent;
                    operator = expr.getOperation();

                    if (null != operator && operator.getNode().getElementType() == PhpTokenTypes.opNOT) {
                        ++nestingLevel;
                        target = parent;
                    }

                    parent = expr.getParent();
                }
                if (nestingLevel == 1) {
                    return;
                }

                /* fire warning */
                String strError = strProblemBoolCasting;
                if (nestingLevel % 2 != 0) {
                    strError = strProblemSingleNot;
                }
                strError = strError.replace("%e%", value.getText());
                holder.registerProblem(target, strError, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
