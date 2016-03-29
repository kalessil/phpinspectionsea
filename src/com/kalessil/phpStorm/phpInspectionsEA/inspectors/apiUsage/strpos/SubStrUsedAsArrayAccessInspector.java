package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strpos;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.impl.PhpExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class SubStrUsedAsArrayAccessInspector extends BasePhpInspection {
    private static final String messagePattern = "'%c%[%i%]' should be used instead";

    @NotNull
    public String getShortName() {
        return "SubStrUsedAsArrayAccessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check if it's the target function */
                final String function     = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (3 != params.length || StringUtil.isEmpty(function) || !function.equals("substr")) {
                    return;
                }

                if (params[2] instanceof PhpExpressionImpl) {
                    final String number = params[2].getText().replaceAll("\\s+","");
                    if (number.equals("1") || number.equals("-1")) {
                        final String message = messagePattern
                                .replace("%c%", params[0].getText())
                                .replace("%i%", params[1].getText())
                        ;
                        holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
        };
    }
}
