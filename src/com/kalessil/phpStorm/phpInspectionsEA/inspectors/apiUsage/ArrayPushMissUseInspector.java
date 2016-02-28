package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ArrayPushMissUseInspector extends BasePhpInspection {
    private static final String strProblemDescription = "'%t%[] = ...' construction shall be used instead";
    private static final String strTargetFunctionName = "array_push";

    @NotNull
    public String getShortName() {
        return "ArrayPushMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check requirements */
                final PsiElement[] arrParams = reference.getParameters();
                final String strFunction     = reference.getName();
                if (2 != arrParams.length || StringUtil.isEmpty(strFunction) || !strFunction.equals(strTargetFunctionName)) {
                    return;
                }

                /* inspect given call */
                if (reference.getParent() instanceof StatementImpl) {
                    final String strMessage = strProblemDescription.replace("%t%", arrParams[0].getText());
                    holder.registerProblem(reference, strMessage, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
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
                final PsiElement replacement          = PhpPsiElementFactory.createFromText(project, AssignmentExpression.class, "$x[] = null");
                final AssignmentExpression assign     = (AssignmentExpression) replacement;

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
