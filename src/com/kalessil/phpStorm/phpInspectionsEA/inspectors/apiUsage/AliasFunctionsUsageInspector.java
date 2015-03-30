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
    private static final String strProblemOctdec       = "'octdec(...)' can be replaced with 'intval(..., 8)'";
    private static final String strProblemHexdec       = "'hexdec(...)' can be replaced with 'intval(..., 16)'";

    @NotNull
    public String getShortName() {
        return "AliasFunctionsUsageInspection";
    }

    private static HashMap<String, String> mapping = null;
    private static HashMap<String, String> getMapping() {
        if (null == mapping) {
            mapping = new HashMap<String, String>();

            mapping.put("is_double",            "is_float");
            mapping.put("is_integer",           "is_int");
            mapping.put("is_long",              "is_int");
            mapping.put("is_real",              "is_float");
            mapping.put("sizeof",               "count");
            mapping.put("doubleval",            "floatval");
            mapping.put("fputs",                "fwrite");
            mapping.put("join",                 "implode");
            mapping.put("key_exists",           "array_key_exists");
            mapping.put("chop",                 "rtrim");
            mapping.put("close",                "closedir");
            mapping.put("ini_alter",            "ini_set");
            mapping.put("is_writeable",         "is_writable");
            mapping.put("magic_quotes_runtime", "set_magic_quotes_runtime");
            mapping.put("pos",                  "current");
            mapping.put("show_source",          "highlight_file");
            mapping.put("strchr",               "strstr");
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
                if (!mapFunctions.containsKey(strFunctionName)) {
                    /** some special cases, my personal preferences */
                    if (strFunctionName.equals("octdec")) {
                        holder.registerProblem(reference, strProblemOctdec, ProblemHighlightType.LIKE_DEPRECATED);
                    } else if (strFunctionName.equals("hexdec")) {
                        holder.registerProblem(reference, strProblemHexdec, ProblemHighlightType.LIKE_DEPRECATED);
                    }

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
