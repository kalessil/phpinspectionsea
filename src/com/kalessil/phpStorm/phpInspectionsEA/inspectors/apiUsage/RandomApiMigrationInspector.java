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

public class RandomApiMigrationInspector extends BasePhpInspection {
    private static final String strProblemDescription  = "'%o%(...)' has recommended replacement '%n%(...)', consider migrating";

    @NotNull
    public String getShortName() {
        return "RandomApiMigrationInspection";
    }

    private static HashMap<String, String> mapping = null;
    private static HashMap<String, String> getMapping() {
        if (null == mapping) {
            mapping = new HashMap<String, String>();

            mapping.put("rand",       "mt_rand");
            mapping.put("srand",      "mt_srand");
            mapping.put("getrandmax", "mt_getrandmax");
        }

        return mapping;
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
                if (mapFunctions.containsKey(strFunctionName)) {
                    String strMessage = strProblemDescription
                            .replace("%o%", strFunctionName)
                            .replace("%n%", mapFunctions.get(strFunctionName));
                    holder.registerProblem(reference, strMessage, ProblemHighlightType.LIKE_DEPRECATED);
                }
            }
        };
    }
}
