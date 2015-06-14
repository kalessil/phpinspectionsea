package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class PrintfScanfArgumentsInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Amount of expected parameters is %c%";

    @NotNull
    public String getShortName() {
        return "PrintfScanfArgumentsInspection";
    }

    private static HashMap<String, Integer> functions = null;
    private static HashMap<String, Integer> getFunctions() {
        if (null == functions) {
            /* pairs function name -> pattern position */
            functions = new HashMap<String, Integer>();

            functions.put("printf",  0);
            functions.put("sprintf", 0);
            functions.put("fprintf", 0);

            functions.put("sscanf",  1);
            functions.put("fscanf",  1);
        }

        return functions;
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String strFunctionName = reference.getName();
                HashMap<String, Integer> mapping = getFunctions();
                if (StringUtil.isEmpty(strFunctionName) || !mapping.containsKey(strFunctionName)) {
                    return;
                }

                /* resolve needed parameter */
                final int neededPosition = mapping.get(strFunctionName);
                final int minimumArgumentsForAnalysis = neededPosition + 1;
                StringLiteralExpression pattern = null;
                if (reference.getParameters().length >= minimumArgumentsForAnalysis) {
                    pattern = ExpressionSemanticUtil.resolveAsStringLiteral(reference.getParameters()[neededPosition]);
                }
                /* not available */
                if (null == pattern) {
                    return;
                }

                String content = pattern.getContents();
                if (!StringUtil.isEmpty(content)) {
                    final int parametersInPattern = StringUtil.getOccurrenceCount(content.replace("%%", ""), '%');
                    final int parametersExpected = minimumArgumentsForAnalysis + parametersInPattern;
                    if (parametersExpected != reference.getParameters().length) {
                        String strError = strProblemDescription.replace("%c%", String.valueOf(parametersExpected));
                        holder.registerProblem(reference, strError, ProblemHighlightType.GENERIC_ERROR);
                    }
                }
            }
        };
    }
}

