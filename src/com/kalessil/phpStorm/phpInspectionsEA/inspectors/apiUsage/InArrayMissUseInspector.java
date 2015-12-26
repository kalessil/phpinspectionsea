package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class InArrayMissUseInspector extends BasePhpInspection {
    private static final String strProblemComparison  = "'%v% === %e%' should be used instead";
    private static final String strProblemKeyExists   = "This is array_key_exists(...) call equivalent";

    @NotNull
    public String getShortName() {
        return "InArrayMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /** try filtering by args count first */
                PsiElement[] parameters = reference.getParameters();
                final int intParamsCount = parameters.length;
                if (intParamsCount < 2 || intParamsCount > 3) {
                    return;
                }
                /** now naming filter */
                final String strFunctionName = reference.getName();
                if (StringUtil.isEmpty(strFunctionName) || !strFunctionName.equals("in_array")) {
                    return;
                }


                /** === test array_key_exists equivalence === */
                if (parameters[1] instanceof FunctionReference) {
                    String strSubCallName = ((FunctionReference) parameters[1]).getName();
                    if (!StringUtil.isEmpty(strSubCallName) && strSubCallName.equals("array_keys")) {
                        holder.registerProblem(reference, strProblemKeyExists, ProblemHighlightType.WEAK_WARNING);
                        return;
                    }
                }


                /** === test comparison equivalence === */
                if (parameters[1] instanceof ArrayCreationExpression) {
                    int itemsCount = 0;
                    PsiElement lastItem = null;
                    for (PsiElement oneItem : parameters[1].getChildren()) {
                        if (oneItem instanceof PhpPsiElement) {
                            ++itemsCount;
                            lastItem = oneItem;
                        }
                    }

                    lastItem = lastItem instanceof ArrayHashElement ? ((ArrayHashElement) lastItem).getValue() : lastItem;
                    if (itemsCount <= 1 && null != lastItem) {
                        String message = strProblemComparison
                                .replace("%v%", lastItem.getText())
                                .replace("%e%", parameters[0].getText());
                        holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING);
                        // return;
                    }
                }
            }
        };
    }
}
