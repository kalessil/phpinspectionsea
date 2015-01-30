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

public class AliasFunctionsUsageInspector extends BasePhpInspection {
    private static final String strProblemDescription  = "'%a%(...)' is an alias function. Use '%f%(...)' instead";

    private static HashMap<String, String> mapping = null;
    private static HashMap<String, String> getMapping() {
        if (null == mapping) {
            mapping = new HashMap<>();

            mapping.put("is_double",  "is_float");
            mapping.put("is_integer", "is_int");
            mapping.put("is_long",    "is_int");
            mapping.put("is_real",    "is_float");
            mapping.put("sizeof",     "count");
        }

        return mapping;
    }

    @NotNull
    public String getDisplayName() {
        return "Compatibility: alias functions usage";
    }

    @NotNull
    public String getShortName() {
        return "AliasFunctionsUsageInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String strFunctionName = reference.getName();
                if (StringUtil.isEmpty(strFunctionName)) {
                    return;
                }

                HashMap<String, String> mapFunctions = getMapping();
                if (!mapFunctions.containsKey(strFunctionName)) {
                    return;
                }

                String strMessage = strProblemDescription
                        .replace("%a%", strFunctionName)
                        .replace("%f%", mapFunctions.get(strFunctionName));
                holder.registerProblem(reference, strMessage, ProblemHighlightType.LIKE_DEPRECATED);
            }
        };
    }
}
