package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;


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

public class CaseInsensitiveStringFunctionsMissUseInspector extends BasePhpInspection {
    private static final String messagePattern = "'%f%(...)' should be used instead";

    @NotNull
    public String getShortName() {
        return "CaseInsensitiveStringFunctionsMissUseInspection";
    }

    private static HashMap<String, String> mapping = null;
    private static HashMap<String, String> getMapping() {
        if (null == mapping) {
            mapping = new HashMap<String, String>();

            mapping.put("stristr",  "strstr");
            mapping.put("stripos",  "strpos");
            mapping.put("strripos", "strrpos");
        }

        return mapping;
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final PsiElement[] parameters = reference.getParameters();
                final String strFunctionName  = reference.getName();
                if (parameters.length < 2 || StringUtil.isEmpty(strFunctionName)) {
                    return;
                }

                final HashMap<String, String> mapFunctions = getMapping();
                if (mapFunctions.containsKey(strFunctionName)) {
                    // resolve second parameter
                    final StringLiteralExpression pattern = ExpressionSemanticUtil.resolveAsStringLiteral(parameters[1]);
                    // not available / PhpStorm limitations
                    if (null == pattern || pattern.getContainingFile() != parameters[1].getContainingFile()) {
                        return;
                    }

                    final String patternString = pattern.getContents();
                    if (!StringUtil.isEmpty(patternString) && !patternString.matches(".*\\p{L}.*")) {
                        final String functionName = mapFunctions.get(strFunctionName);
                        final String message      = messagePattern.replace("%f%", functionName);
                        holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(functionName));
                    }
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        private String suggestedName;

        TheLocalFix(@NotNull String suggestedName) {
            super();
            this.suggestedName = suggestedName;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use '" + this.suggestedName + "(...)'";
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
