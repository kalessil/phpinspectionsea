package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;


import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CaseInsensitiveStringFunctionsMissUseInspector extends BasePhpInspection {
    private static final String strProblemDescription  = "'%f%(...)' can be used instead.";

    @NotNull
    public String getShortName() {
        return "CaseInsensitiveStringFunctionsMissUseInspection";
    }

    private static HashMap<String, String> mapping = null;
    private static HashMap<String, String> getMapping() {
        if (null == mapping) {
            mapping = new HashMap<String, String>();

            mapping.put("stristr", "strstr");
            mapping.put("stripos", "strpos");
        }

        return mapping;
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                PsiElement[] parameters = reference.getParameters();
                final String strFunctionName = reference.getName();
                if (parameters.length < 2 || StringUtil.isEmpty(strFunctionName)) {
                    return;
                }

                HashMap<String, String> mapFunctions = getMapping();
                if (mapFunctions.containsKey(strFunctionName)) {
                    // resolve second parameter
                    StringLiteralExpression pattern = ExpressionSemanticUtil.resolveAsStringLiteral(parameters[1]);
                    // not available / PhpStorm limitations
                    if (null == pattern || pattern.getContainingFile() != parameters[1].getContainingFile()) {
                        return;
                    }

                    String patternString = pattern.getContents();
                    if (!StringUtil.isEmpty(patternString) && !patternString.matches(".*\\p{L}.*")) {
                        String strMessage = strProblemDescription.replace("%f%", mapFunctions.get(strFunctionName));
                        holder.registerProblem(reference, strMessage, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }
        };
    }
}
