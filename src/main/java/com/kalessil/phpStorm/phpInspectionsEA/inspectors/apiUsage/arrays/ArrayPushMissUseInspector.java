package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

public class ArrayPushMissUseInspector extends BasePhpInspection {
    private static final String messagePattern = "'%t%[] = ...' should be used instead (2x faster).";

    @NotNull
    public String getShortName() {
        return "ArrayPushMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                /* check requirements */
                final PsiElement[] params = reference.getParameters();
                final String function     = reference.getName();
                if (params.length != 2 || function == null || !function.equals("array_push")) {
                    return;
                }

                /* inspect given call: single instruction, 2nd parameter is not variadic */
                if (OpenapiTypesUtil.isStatementImpl(reference.getParent())) {
                    PsiElement variadicCandidate = params[1].getPrevSibling();
                    if (variadicCandidate instanceof PsiWhiteSpace) {
                        variadicCandidate = variadicCandidate.getPrevSibling();
                    }
                    /* do not report cases with variadic 2nd parameter */
                    if (OpenapiTypesUtil.is(variadicCandidate, PhpTokenTypes.opVARIADIC)) {
                        return;
                    }

                    final String message = messagePattern.replace("%t%", params[0].getText());
                    holder.registerProblem(reference, message, new TheLocalFix());
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Use []=";
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
                final PsiElement replacement      = PhpPsiElementFactory.createFromText(project, AssignmentExpression.class, "$x[] = null");
                final AssignmentExpression assign = (AssignmentExpression) replacement;

                //noinspection ConstantConditions - expression is hardcoded so we safe from NPE here and below
                final ArrayAccessExpression container = (ArrayAccessExpression) assign.getVariable();
                //noinspection ConstantConditions
                container.getValue().replace(((FunctionReference) expression).getParameters()[0]);
                //noinspection ConstantConditions
                assign.getValue().replace(((FunctionReference) expression).getParameters()[1]);

                //noinspection ConstantConditions
                expression.replace(replacement);
            }
        }
    }
}
