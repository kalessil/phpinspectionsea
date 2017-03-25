package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Funivan <alotofall@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UsageOfSilenceOperatorInspector extends BasePhpInspection {
    private static final String message = "Try to avoid using the @, as it hides problems and complicates troubleshooting.";

    private static final List<String> suppressibleFunctions = Arrays.asList(
            "\\unlink",
            "\\mkdir",
            "\\trigger_error"
    );

    @NotNull
    public String getShortName() {
        return "UsageOfSilenceOperatorInspection";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            public void visitPhpUnaryExpression(UnaryExpression unaryExpression) {
                /* general structure expectations */
                final PsiElement suppressionCandidate = unaryExpression.getOperation();
                if (null == suppressionCandidate || PhpTokenTypes.opSILENCE != suppressionCandidate.getNode().getElementType()) {
                    return;
                }

                /* pattern 1: whatever but not a function call */
                final PsiElement suppressedExpression = unaryExpression.getValue();
                if (!OpenapiTypesUtil.isFunctionReference(suppressedExpression)) {
                    holder.registerProblem(suppressionCandidate, message, ProblemHighlightType.WEAK_WARNING);
                    return;
                }

                /* pattern 2: a function call */
                final FunctionReference call = (FunctionReference) suppressedExpression;
                final String functionName    = call.getFQN();
                final PsiElement[] params    = call.getParameters();
                if (params.length > 0 && !suppressibleFunctions.contains(functionName)) {
                    holder.registerProblem(suppressionCandidate, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix());
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Remove @";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement operator = descriptor.getPsiElement();
            if (null != operator && operator.getParent() instanceof UnaryExpression) {
                final UnaryExpression suppression = (UnaryExpression) operator.getParent();
                //noinspection ConstantConditions as structure guaranted
                suppression.replace(suppression.getValue().copy());
            }
        }
    }

}
