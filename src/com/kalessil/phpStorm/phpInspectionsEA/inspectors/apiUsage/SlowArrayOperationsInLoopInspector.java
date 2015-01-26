package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class SlowArrayOperationsInLoopInspector extends BasePhpInspection {
    private static final String strProblemDescription = "'%s%(...)' is used in loop - resources greedy construction. Check inspection description for a solution.";

    private HashSet<String> functionsSet = null;
    private HashSet<String> getFunctionsSet() {
        if (null == functionsSet) {
            functionsSet = new HashSet<>();

            functionsSet.add("array_merge");
        }

        return functionsSet;
    }

    @NotNull
    public String getDisplayName() {
        return "Performance: resources consuming array function used in loop";
    }

    @NotNull
    public String getShortName() {
        return "SlowArrayOperationsInLoopInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String strFunctionName = reference.getName();
                if (StringUtil.isEmpty(strFunctionName) || !getFunctionsSet().contains(strFunctionName)) {
                    return;
                }

                PsiElement objParent = reference.getParent();
                while (null != objParent && !(objParent instanceof PhpFile)) {
                    if (objParent instanceof Function) {
                        return;
                    }

                    /** TODO: allow usage when wrapped with conditional statements? */

                    if (objParent instanceof ForeachStatement || objParent instanceof For || objParent instanceof While) {
                        String strError = strProblemDescription.replace("%s%", strFunctionName);
                        holder.registerProblem(reference, strError, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                        return;
                    }

                    objParent = objParent.getParent();
                }
            }
        };
    }
}