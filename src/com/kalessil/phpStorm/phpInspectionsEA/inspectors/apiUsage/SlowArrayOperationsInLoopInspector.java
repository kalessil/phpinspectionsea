package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInsight.PsiEquivalenceUtil;
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
            functionsSet = new HashSet<String>();

            functionsSet.add("array_merge");
        }

        return functionsSet;
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
                if (!(objParent instanceof AssignmentExpression)) {
                    /** let's focus on assignment expressions */
                    return;
                }

                while (null != objParent && !(objParent instanceof PhpFile)) {
                    /** terminate if reached callable */
                    if (objParent instanceof Function) {
                        return;
                    }

                    if (objParent instanceof ForeachStatement || objParent instanceof For || objParent instanceof While) {
                        /** loop test is positive, check pattern */
                        PhpPsiElement objContainer = ((AssignmentExpression) reference.getParent()).getVariable();
                        if (null == objContainer) {
                            return;
                        }

                        /** pattern itself: container overridden */
                        for (PsiElement objParameter : reference.getParameters()) {
                            if (PsiEquivalenceUtil.areElementsEquivalent(objContainer, objParameter)) {
                                String strError = strProblemDescription.replace("%s%", strFunctionName);
                                holder.registerProblem(reference, strError, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                                return;
                            }
                        }
                    }

                    objParent = objParent.getParent();
                }
            }
        };
    }
}