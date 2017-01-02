package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class CaseInsensitiveStringFunctionsMissUseInspector extends BasePhpInspection {
    private static final String messagePattern = "'%f%(...)' should be used instead (the pattern does not contain alphabet characters).";

    @NotNull
    public String getShortName() {
        return "CaseInsensitiveStringFunctionsMissUseInspection";
    }

    private static final Map<String, String> mapping = new HashMap<>();
    static {
        mapping.put("stristr",  "strstr");
        mapping.put("stripos",  "strpos");
        mapping.put("strripos", "strrpos");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final PsiElement[] params = reference.getParameters();
                final String functionName = reference.getName();
                if (
                    (2 != params.length && 3 != params.length) ||
                    StringUtil.isEmpty(functionName) || !mapping.containsKey(functionName)
                ) {
                    return;
                }

                // resolve second parameter
                final StringLiteralExpression pattern = ExpressionSemanticUtil.resolveAsStringLiteral(params[1]);
                // might be not available - in another file (PhpStorm limitations)
                if (null == pattern || pattern.getContainingFile() != params[1].getContainingFile()) {
                    return;
                }

                final String patternString = pattern.getContents();
                if (!StringUtil.isEmpty(patternString) && !patternString.matches(".*\\p{L}.*")) {
                    final String message = messagePattern.replace("%f%", mapping.get(functionName));
                    holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(functionName));
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private String suggestedName;

        TheLocalFix(@NotNull String suggestedName) {
            super();
            this.suggestedName = suggestedName;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use suggested replacement";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference) {
                ((FunctionReference) expression).handleElementRename(this.suggestedName);
            }
        }
    }
}
