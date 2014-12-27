package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpEmpty;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class IsEmptyFunctionUsageInspector extends BasePhpInspection {
    private static final String strProblemDescriptionDoNotUse =
            "'empty(...)' is not type safe and brings N-path complexity due to multiple types supported." +
            " Consider refactoring this code.";

    private static final String strProblemDescriptionUseCount =
            "Use 'count() <comparison> 0' construction instead";

    @NotNull
    public String getDisplayName() {
        return "API: 'empty(...)' usage";
    }

    @NotNull
    public String getShortName() {
        return "IsEmptyFunctionUsageInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpEmpty(PhpEmpty emptyExpression) {
                /**
                 * TODO - dedicate types resolver, for using here.
                 * TODO - review before re-publishing
                 */
                PhpExpression[] arrValues = emptyExpression.getVariables();
                if (
                    arrValues.length == 1 &&
                    arrValues[0] instanceof Variable &&
                    ((Variable) arrValues[0]).getSignature().equals("#C\\array")
                ) {
                    holder.registerProblem(emptyExpression, strProblemDescriptionUseCount, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }

                holder.registerProblem(emptyExpression, strProblemDescriptionDoNotUse, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
