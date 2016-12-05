package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.Include;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class RealpathInSteamContextInspector extends BasePhpInspection {
    private static final String message = "realpath() working differently in stream context (e.g. for phar://...), consider using dirname() instead";

    @NotNull
    public String getShortName() {
        return "RealpathInSteamContextInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check general requirements */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (1 != params.length || StringUtil.isEmpty(functionName) || !functionName.equals("realpath")) {
                    return;
                }


                /* case 1: include/require context */
                /* get parent expression through () */
                PsiElement parent = reference.getParent();
                while (parent instanceof ParenthesizedExpression) {
                    parent = parent.getParent();
                }
                if (parent instanceof Include) {
                    holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);
                    return;
                }


                /* case 2: realpath applied to a relative path '..' */
                Collection<StringLiteralExpression> strings = PsiTreeUtil.findChildrenOfType(reference, StringLiteralExpression.class);
                if (strings.size() > 0) {
                    for (StringLiteralExpression oneString : strings) {
                        if (oneString.getContents().contains("..")) {
                            /* report the issue */
                            holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);
                            break;
                        }
                    }
                    strings.clear();
                }
            }
        };
    }
}