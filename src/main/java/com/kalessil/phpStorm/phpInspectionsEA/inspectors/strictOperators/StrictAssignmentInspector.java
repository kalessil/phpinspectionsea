package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators.util.PhpExpressionTypes;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrictAssignmentInspector extends BasePhpInspection {
    private static final String strProblemDescriptionAssignment = "Variable type of (%t1%) doesn't match assigning value type (%t2%).";

    @NotNull
    public String getShortName() {
        return "StrictAssignmentInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpAssignmentExpression(final AssignmentExpression expr) {
                final PhpExpressionTypes varT = new PhpExpressionTypes(expr.getVariable(), holder);
                final PhpExpressionTypes valueT = new PhpExpressionTypes(expr.getValue(), holder);
                if (varT.isMixed() || varT.equals(valueT) || valueT.instanceOf(varT)) {
                    return;
                }
                if (varT.isFloat() && valueT.isInt()) {
                    return;
                }

                final String strWarning = strProblemDescriptionAssignment
                        .replace("%t1%", varT.toString())
                        .replace("%t2%", valueT.toString());
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
