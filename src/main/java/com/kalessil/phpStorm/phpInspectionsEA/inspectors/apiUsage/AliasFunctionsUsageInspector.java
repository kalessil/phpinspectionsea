package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class AliasFunctionsUsageInspector extends BasePhpInspection {
    private static final String messagePattern = "'%a%(...)' is an alias function. Use '%f%(...)' instead.";

    @NotNull
    public String getShortName() {
        return "AliasFunctionsUsageInspection";
    }

    private static final HashMap<String, String> mapping = new HashMap<>();
    static {
        mapping.put("is_double",            "is_float");
        mapping.put("is_integer",           "is_int");
        mapping.put("is_long",              "is_int");
        mapping.put("is_real",              "is_float");
        mapping.put("sizeof",               "count");
        mapping.put("doubleval",            "floatval");
        mapping.put("fputs",                "fwrite");
        mapping.put("join",                 "implode");
        mapping.put("key_exists",           "array_key_exists");
        mapping.put("chop",                 "rtrim");
        mapping.put("close",                "closedir");
        mapping.put("ini_alter",            "ini_set");
        mapping.put("is_writeable",         "is_writable");
        mapping.put("magic_quotes_runtime", "set_magic_quotes_runtime");
        mapping.put("pos",                  "current");
        mapping.put("show_source",          "highlight_file");
        mapping.put("strchr",               "strstr");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String functionName = reference.getName();
                if (!StringUtil.isEmpty(functionName) && mapping.containsKey(functionName)) {
                    /* avoid complaining to imported functions */
                    final PsiElement function = reference.resolve();
                    if (function instanceof Function && !((Function) function).getFQN().equals('\\' + functionName)) {
                        return;
                    }

                    /* fire message */
                    final String suggestedName = mapping.get(functionName);
                    final String message = messagePattern
                            .replace("%a%", functionName)
                            .replace("%f%", suggestedName);
                    holder.registerProblem(reference, message, ProblemHighlightType.LIKE_DEPRECATED, new TheLocalFix(suggestedName));
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
            return "Use origin function";
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
