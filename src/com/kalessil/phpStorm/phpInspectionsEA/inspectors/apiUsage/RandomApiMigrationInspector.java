package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class RandomApiMigrationInspector extends BasePhpInspection {
    private static final String strProblemDescription  = "'%o%(...)' has recommended replacement '%n%(...)', consider migrating";

    @NotNull
    public String getShortName() {
        return "RandomApiMigrationInspection";
    }

    private static HashMap<String, String> mapping = null;
    private static PhpLanguageLevel languageLevel = null;
    private static HashMap<String, String> getMapping(PhpLanguageLevel preferableLanguageLevel) {
        if (null == mapping || languageLevel != preferableLanguageLevel) {
            languageLevel = preferableLanguageLevel;

            mapping = new HashMap<String, String>();
            mapping.put("srand",      "mt_srand");
            mapping.put("getrandmax", "mt_getrandmax");

            if (languageLevel == PhpLanguageLevel.PHP700) {
                mapping.put("rand",    "random_int");
                mapping.put("mt_rand", "random_int");
            } else {
                mapping.put("rand",    "mt_rand");
            }
        }

        return mapping;
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String strFunctionName = reference.getName();
                if (StringUtil.isEmpty(strFunctionName)) {
                    return;
                }

                HashMap<String, String> mapFunctions = getMapping(PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel());
                if (mapFunctions.containsKey(strFunctionName)) {
                    String suggestedName = mapFunctions.get(strFunctionName);
                    /* random_int needs 2 parameters always, so check if mt_rand can be suggested */
                    if (2 != reference.getParameters().length && suggestedName.equals("random_int")) {
                        if (strFunctionName.equals("rand")) {
                            suggestedName = "mt_rand";
                        } else {
                            return;
                        }
                    }

                    final String strMessage = strProblemDescription
                            .replace("%o%", strFunctionName)
                            .replace("%n%", suggestedName);
                    holder.registerProblem(reference, strMessage, ProblemHighlightType.LIKE_DEPRECATED, new TheLocalFix(suggestedName));
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
            return "Use newer function";
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
