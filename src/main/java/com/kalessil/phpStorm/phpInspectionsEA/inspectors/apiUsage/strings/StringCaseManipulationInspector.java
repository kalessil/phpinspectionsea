package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class StringCaseManipulationInspector extends BasePhpInspection {
    private static final String messagePattern  = "'%e%' should be used instead.";

    private static final Map<String, String> functions = new HashMap<>();
    private static final Set<String> innerFunctions    = new HashSet<>();
    static {
        functions.put("strpos",     "stripos");
        functions.put("mb_strpos",  "mb_stripos");
        functions.put("strrpos",    "strripos");
        functions.put("mb_strrpos", "mb_strripos");

        innerFunctions.add("strtolower");
        innerFunctions.add("mb_strtolower");
        innerFunctions.add("strtoupper");
        innerFunctions.add("mb_strtoupper");
    }

    @NotNull
    public String getShortName() {
        return "StringCaseManipulationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (params.length == 2 && functionName != null && functions.containsKey(functionName)) {
                    final PsiElement first  = this.getSubject(params[0]);
                    final PsiElement second = this.getSubject(params[1]);
                    if (first != null || second != null) {
                        final String replacement = "%f%(%a1%, %a2%)"
                            .replace("%a2%", (second == null ? params[1] : second).getText())
                            .replace("%a1%", (first == null ? params[0] : first).getText())
                            .replace("%f%", functions.get(functionName));
                        final String message = messagePattern.replace("%e%", replacement);
                        holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new SimplifyFix(replacement));
                    }
                }
            }

            @Nullable
            private PsiElement getSubject(@NotNull PsiElement expression) {
                PsiElement result = null;
                if (OpenapiTypesUtil.isFunctionReference(expression)) {
                    final FunctionReference reference = (FunctionReference) expression;
                    final String functionName         = reference.getName();
                    final PsiElement[] params         = reference.getParameters();
                    if (params.length == 1 && functionName != null && innerFunctions.contains(functionName)) {
                        result = params[0];
                    }
                }
                return result;
            }
        };
    }

    private class SimplifyFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Simplify unnecessary case manipulation";
        }

        SimplifyFix(@NotNull String expression) {
            super(expression);
        }
    }
}
