package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/**
    TODO: docs - https://gist.github.com/nikic/6390366
 */
public class ArgumentUnpackingCanBeUsedInspector extends BasePhpInspection {
    private static final String messagePattern = "'%e%' should be used instead (3x+ faster)";

    @NotNull
    public String getShortName() {
        return "ArgumentUnpackingCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* the feature was introduced in PHP 5.6 */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(reference.getProject()).getLanguageLevel();
                if (phpVersion.compareTo(PhpLanguageLevel.PHP560) < 0) {
                    return;
                }

                /* general structure expectation */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (2 != params.length || null== functionName || !functionName.equals("call_user_func_array")) {
                    return;
                }

                final boolean isContainerValid = params[1] instanceof Variable || params[1] instanceof ArrayCreationExpression;
                if (isContainerValid && params[0] instanceof StringLiteralExpression) {
                    /* do not process strings with injections */
                    final StringLiteralExpression targetFunction = (StringLiteralExpression) params[0];
                    if (null != targetFunction.getFirstPsiChild()){
                        return;
                    }

                    final String replacement = "%f%(...%a%)"
                        .replace("%a%", params[1].getText())
                        .replace("%f%", targetFunction.getContents());
                    final String message = messagePattern.replace("%e%", replacement);
                    /* TODO: fixer title override */
                    holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new UseSuggestedReplacementFixer(replacement));
                }
            }
        };
    }
}