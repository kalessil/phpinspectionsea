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

public class ForgottenDebugOutputInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Please ensure this is not forgotten debug statement";

    @NotNull
    public String getShortName() {
        return "ForgottenDebugOutputInspection";
    }

    private HashMap<String, Integer> functionsRequirements = null;
    private HashMap<String, Integer> getFunctionsRequirements() {
        if (null == functionsRequirements) {
            functionsRequirements = new HashMap<String, Integer>();

            /* function name => amount of arguments considered legal */
            functionsRequirements.put("print_r",          2);
            functionsRequirements.put("var_export",       2);
            functionsRequirements.put("var_dump",        -1);
            functionsRequirements.put("debug_zval_dump", -1);
        }

        return functionsRequirements;
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String strFunction              = reference.getName();
                HashMap<String, Integer> requirements = getFunctionsRequirements();
                if (
                    !StringUtil.isEmpty(strFunction) && requirements.containsKey(strFunction) &&
                    reference.getParameters().length != requirements.get(strFunction)
                ) {
                    holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }
}
