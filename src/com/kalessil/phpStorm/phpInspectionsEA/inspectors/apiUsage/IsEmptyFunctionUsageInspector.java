package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpEmpty;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPsiResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

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
                PhpExpression[] arrValues = emptyExpression.getVariables();
                if (arrValues.length == 1) {
                    PhpIndex objIndex = PhpIndex.getInstance(holder.getProject());
                    LinkedList<String> objResolvedTypes = new LinkedList<>();
                    TypeFromPsiResolvingUtil.resolveExpressionType(arrValues[0], objIndex, objResolvedTypes);


                    /** debug */
                    /*String strResolvedTypes = "Resolved: ";
                    String strGlue = "";
                    for (String strOneType : objResolvedTypes) {
                        strResolvedTypes += strOneType + strGlue;
                        strGlue = "|";
                    }
                    holder.registerProblem(emptyExpression, strResolvedTypes, ProblemHighlightType.LIKE_DEPRECATED);*/
                    /** /debug  */


                    /** TODO: when it's class, suggest to use null comparison */

                    if (objResolvedTypes.size() == 1 && objResolvedTypes.get(0).equals(Types.strArray)) {
                        holder.registerProblem(emptyExpression, strProblemDescriptionUseCount, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        return;
                    }
                }

                holder.registerProblem(emptyExpression, strProblemDescriptionDoNotUse, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
