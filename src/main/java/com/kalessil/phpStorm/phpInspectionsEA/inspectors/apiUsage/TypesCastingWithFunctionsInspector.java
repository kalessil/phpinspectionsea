package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class TypesCastingWithFunctionsInspector extends BasePhpInspection {
    private static final String messagePattern = "'(%s) ...' construction should be used instead (up to 6x times faster).";

    @NotNull
    public String getShortName() {
        return "TypesCastingWithFunctionsInspection";
    }

    private static final HashMap<String, String> mapping = new HashMap<>();
    static {
        mapping.put("intval",   "int");
        mapping.put("floatval", "float");
        mapping.put("strval",   "string");
        mapping.put("boolval",  "bool");
        mapping.put("settype",  "<needed type>");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check construction requirements */
                final PsiElement[] params   = reference.getParameters();
                final String functionName   = reference.getName();
                final int intArgumentsCount = params.length;
                if ((intArgumentsCount != 1 && intArgumentsCount != 2) || StringUtils.isEmpty(functionName)) {
                    return;
                }
                if (intArgumentsCount == 2 && !functionName.equals("settype")) {
                    return;
                }

                /* check if inspection subject*/
                if (mapping.containsKey(functionName)) {
                    final String suggestedType = mapping.get(functionName);
                    final String message       = messagePattern.replace("%s", suggestedType);

                    if (functionName.equals("settype")) {
                        if (params[1] instanceof StringLiteralExpression) {
                            holder.registerProblem(reference, message, ProblemHighlightType.LIKE_DEPRECATED);
                        }
                    } else {
                        holder.registerProblem(reference, message, ProblemHighlightType.LIKE_DEPRECATED, new TheLocalFix(suggestedType));
                    }
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private String suggestedType;

        TheLocalFix(@NotNull String suggestedType) {
            super();
            this.suggestedType = suggestedType;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use type casting";
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
                PsiElement parameter = ((FunctionReference) expression).getParameters()[0];
                if (parameter instanceof BinaryExpression || parameter instanceof UnaryExpression || parameter instanceof TernaryExpression) {
                    PsiElement wrappedParameter = PhpPsiElementFactory.createFromText(project, ParenthesizedExpression.class, "(null)");
                    if (null != wrappedParameter) {
                        ((ParenthesizedExpression) wrappedParameter).getArgument().replace(parameter);
                        parameter = wrappedParameter;
                    }
                }

                final String castingPattern  = '(' + this.suggestedType + ") null";
                final PsiElement replacement = PhpPsiElementFactory.createFromText(project, UnaryExpression.class, castingPattern);
                //noinspection ConstantConditions - expression is hardcoded so we safe from NPE here
                ((UnaryExpression) replacement).getValue().replace(parameter);

                expression.replace(replacement);
            }
        }
    }
}