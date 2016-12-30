package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class FilePutContentsMissUseInspector extends BasePhpInspection {
    private static final String messagePattern = "'%e%' would consume less cpu and memory resources here";

    @NotNull
    public String getShortName() {
        return "FilePutContentsMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* validate parameters amount and function name (file) */
                final PsiElement[] params = reference.getParameters();
                final String functionName = reference.getName();
                if (2 != params.length || StringUtil.isEmpty(functionName) || !functionName.equals("file_put_contents")) {
                    return;
                }

                /* analyze the call */
                if (params[1] instanceof FunctionReference) {
                    final FunctionReference innerReference = (FunctionReference) params[1];
                    final String innerName                 = innerReference.getName();
                    final PsiElement[] innerParams         = innerReference.getParameters();
                    /* check if matches the target pattern */
                    if (1 == innerParams.length && !StringUtil.isEmpty(innerName) && innerName.equals("file_get_contents")) {
                        final String pattern = "copy(%s%, %d%)"
                                .replace("%s%", innerParams[0].getText())
                                .replace("%d%", params[0].getText());
                        final String message = messagePattern.replace("%e%", pattern);
                        holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);
                    }
                }
            }
        };
    }
}
