package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class TypesCastingWithFunctionsInspector extends BasePhpInspection {
    private static final String strProblemDescription = "'(%s) $...' construction shall be used instead";

    private static HashMap<String, String> mapping = null;
    private static HashMap<String, String> getMapping() {
        if (null == mapping) {
            mapping = new HashMap<>();

            mapping.put("intval",    "int");
            mapping.put("floatval",  "float");
            mapping.put("doubleval", "float");
            mapping.put("strval",    "string");
        }

        return mapping;
    }

    @NotNull
    public String getDisplayName() {
        return "Performance: types casting via PHP4 functions";
    }

    @NotNull
    public String getShortName() {
        return "TypesCastingWithFunctionsInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /** check construction requirements */
                final int intArgumentsCount = reference.getParameters().length;
                final String strFunction = reference.getName();
                if (intArgumentsCount != 1 || StringUtil.isEmpty(strFunction)) {
                    return;
                }

                /** check if inspection subject*/
                HashMap<String, String> typesMap = getMapping();
                if (typesMap.containsKey(strFunction)) {
                    String strWarning = strProblemDescription.replace("%s", typesMap.get(strFunction));
                    holder.registerProblem(reference, strWarning, ProblemHighlightType.LIKE_DEPRECATED);
                }
            }
        };
    }
}