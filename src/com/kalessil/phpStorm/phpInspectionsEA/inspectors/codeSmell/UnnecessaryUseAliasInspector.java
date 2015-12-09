package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class UnnecessaryUseAliasInspector extends BasePhpInspection {
    private static final String strProblemDescription = "' as %a%' is redundant here";

    @NotNull
    public String getShortName() {
        return "UnnecessaryUseAliasInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpUse(PhpUse expression) {
                String clazz = expression.getOriginal();
                String alias = expression.getAliasName();
                if (!StringUtil.isEmpty(alias) && !StringUtil.isEmpty(clazz) && clazz.endsWith("\\"+alias)) {
                    String message = strProblemDescription.replace("%a%", alias);
                    holder.registerProblem(expression.getLastChild(), message, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                }
            }
        };
    }
}
