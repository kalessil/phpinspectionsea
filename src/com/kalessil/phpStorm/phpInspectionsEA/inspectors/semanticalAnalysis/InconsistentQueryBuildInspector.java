package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class InconsistentQueryBuildInspector extends BasePhpInspection {
    private static final String strProblemDescription = "'ksort(%a%, SORT_STRING)' should be used instead, " +
            "so http_build_query() produces result independent from key types";

    @NotNull
    public String getShortName() {
        return "InconsistentQueryBuildInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                PsiElement[] parameters = reference.getParameters();
                String function = reference.getName();
                if (1 == parameters.length && !StringUtil.isEmpty(function) && function.equals("ksort")) {
                    // pre-condition satisfied, now check if http_build_query used in the scope

                    Function scope = ExpressionSemanticUtil.getScope(reference);
                    if (null != scope) {
                        Collection<FunctionReference> calls = PsiTreeUtil.findChildrenOfType(scope, FunctionReference.class);
                        if (calls.size() > 0) {
                            for (FunctionReference oneCall : calls) {
                                /* skip inspected call and calls without arguments */
                                if (oneCall == reference || 0 == oneCall.getParameters().length) {
                                    continue;
                                }

                                /* skip non-target function */
                                String currentFunction = oneCall.getName();
                                if (StringUtil.isEmpty(currentFunction) || !currentFunction.equals("http_build_query")) {
                                    continue;
                                }

                                if (PsiEquivalenceUtil.areElementsEquivalent(oneCall.getParameters()[0], parameters[0])) {
                                    String message = strProblemDescription.replace("%a%", parameters[0].getText());
                                    holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                                    break;
                                }
                            }

                            calls.clear();
                        }
                    }
                }
            }
        };
    }
}

