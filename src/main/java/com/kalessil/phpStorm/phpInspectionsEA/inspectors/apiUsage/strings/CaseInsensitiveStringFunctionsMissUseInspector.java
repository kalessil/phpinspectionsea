package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.apache.commons.lang3.StringUtils;
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
    @Override
    public String getShortName() {
        return "CaseInsensitiveStringFunctionsMissUseInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'stristr(...)/stripos(...)/strripos(...)' could be replaced with 'strstr(...)/strpos()/strrpos()'";
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
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName == null || !mapping.containsKey(functionName)) {
                    return;
                }
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length != 2 && arguments.length != 3) {
                    return;
                }

                // resolve second parameter
                final StringLiteralExpression pattern = ExpressionSemanticUtil.resolveAsStringLiteral(arguments[1]);
                // might be not available - in another file (PhpStorm limitations)
                if (pattern == null || pattern.getContainingFile() != arguments[1].getContainingFile()) {
                    return;
                }

                final String patternString = pattern.getContents();
                if (!StringUtils.isEmpty(patternString) && !patternString.matches(".*\\p{L}.*")) {
                    final String replacementFunctionName = mapping.get(functionName);
                    holder.registerProblem(
                            reference,
                            MessagesPresentationUtil.prefixWithEa(messagePattern.replace("%f%", replacementFunctionName)),
                            ProblemHighlightType.WEAK_WARNING,
                            new TheLocalFix(replacementFunctionName)
                    );
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use suggested function instead";

        final private String suggestedName;

        TheLocalFix(@NotNull String suggestedName) {
            super();
            this.suggestedName = suggestedName;
        }

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference && !project.isDisposed()) {
                ((FunctionReference) expression).handleElementRename(this.suggestedName);
            }
        }
    }
}
