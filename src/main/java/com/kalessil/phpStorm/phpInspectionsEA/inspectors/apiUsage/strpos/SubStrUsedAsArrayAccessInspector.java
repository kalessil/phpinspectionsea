package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strpos;

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
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.jetbrains.php.lang.psi.elements.impl.PhpExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class SubStrUsedAsArrayAccessInspector extends BasePhpInspection {
    private static final String messagePattern = "'%e%' might be used instead (invalid index accesses might show up)";

    @NotNull
    public String getShortName() {
        return "SubStrUsedAsArrayAccessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check if it's the target function */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (
                    params.length < 3 || StringUtil.isEmpty(functionName) ||
                    (!functionName.equals("substr") && !functionName.equals("mb_substr"))
                ) {
                    return;
                }

                if (params[2] instanceof PhpExpressionImpl && params[2].getText().replaceAll("\\s+", "").equals("1")) {
                    /* PHP 5.3 is not supporting `call()[index]` constructs */
                    if (params[0] instanceof FunctionReference) {
                        PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                        if (php == PhpLanguageLevel.PHP530) {
                            return;
                        }
                    }

                    final boolean isNegativeOffset = params[1].getText().replaceAll("\\s+", "").startsWith("-");
                    final String expression        = (isNegativeOffset ? "%c%[%f%(%c%) %i%]" : "%c%[%i%]")
                        .replace("%f%", functionName.equals("mb_substr") ? "mb_strlen" : "strlen")
                        .replace("%c%", params[0].getText()).replace("%c%", params[0].getText())
                        .replace("%i%", params[1].getText())
                    ;

                    final String message = messagePattern.replace("%e%", expression);
                    holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix(expression));
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private String expression;

        @NotNull
        @Override
        public String getName() {
            return "Use array access";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        public TheLocalFix(@NotNull String expression) {
            super();
            this.expression = expression;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference) {
                ParenthesizedExpression replacement = PhpPsiElementFactory.createFromText(project, ParenthesizedExpression.class, "(" + this.expression + ")");
                if (null != replacement) {
                    expression.replace(replacement.getArgument());
                }
            }
        }
    }
}
