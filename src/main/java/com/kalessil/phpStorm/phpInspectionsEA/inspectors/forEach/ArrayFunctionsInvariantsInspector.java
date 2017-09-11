package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class ArrayFunctionsInvariantsInspector extends BasePhpInspection {
    private static final String messageArrayFill    = "'array_fill(...)' should be used instead.";
    private static final String messageArrayReplace = "'array_replace(...)' should be used instead.";

    @NotNull
    public String getShortName() {
        return "ArrayFunctionsInvariantsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpForeach(ForeachStatement foreach) {
                final Variable key        = foreach.getKey();
                final Variable value      = foreach.getValue();
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(foreach);
                /* evaluate code structure requirements */
                if (
                    null == body || null == key || null == value ||
                    StringUtils.isEmpty(key.getName()) || StringUtils.isEmpty(value.getName())
                ) {
                    return;
                }

                /* investigate the body and assignment structure */
                if (1 == ExpressionSemanticUtil.countExpressionsInGroup(body)) {
                    PsiElement expression = ExpressionSemanticUtil.getLastStatement(body);
                    expression = (null == expression ? null : expression.getFirstChild());
                    if (!(expression instanceof AssignmentExpression)) {
                        return;
                    }
                    // TODO: operator needs to be "=", no assignments by reference
                    AssignmentExpression assign = (AssignmentExpression) expression;
                    if (!(assign.getVariable() instanceof ArrayAccessExpression)) {
                        return;
                    }

                    /* at this point we need to investigate the assignment */
                    if (isArrayReplaceInvariant(assign, key, value)) {
                        // TODO: same, but array_flip
                        // TODO: resolve source type, report only arrays
                        holder.registerProblem(foreach.getFirstChild(), messageArrayReplace);
                    } else if (isArrayFillInvariant(assign, key, value)) {
                        holder.registerProblem(foreach.getFirstChild(), messageArrayFill);
                    }

                }
            }

            private boolean isArrayFillInvariant(@NotNull AssignmentExpression assign, @NotNull Variable key, @NotNull Variable value) {
                return false;
            }

            private boolean isArrayReplaceInvariant(@NotNull AssignmentExpression assign, @NotNull Variable key, @NotNull Variable value) {
                final PsiElement rawContainer = assign.getVariable();
                if (rawContainer instanceof ArrayAccessExpression) {
                    final ArrayAccessExpression container = (ArrayAccessExpression) rawContainer;
                    final ArrayIndex indexContainer       = container.getIndex();
                    if (null != indexContainer) {
                        /* Ensure index matches the loop key */
                        final PsiElement rawIndex = indexContainer.getValue();
                        if (!(rawIndex instanceof Variable) || !((Variable) rawIndex).getName().equals(key.getName())) {
                            return false;
                        }

                        /* Ensure assigned value matches the loop value */
                        final PsiElement assignedValue = assign.getValue();
                        if (!(assignedValue instanceof Variable) || !((Variable) assignedValue).getName().equals(value.getName())) {
                            return false;
                        }

                        /* pattern matched */
                        return true;
                    }

                }

                return false;
            }
        };
    }
}
