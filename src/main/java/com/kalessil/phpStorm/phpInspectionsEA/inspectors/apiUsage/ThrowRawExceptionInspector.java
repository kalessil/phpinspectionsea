package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.PhpThrow;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ThrowRawExceptionInspector extends BasePhpInspection {
    private static final String messageRawException = "\\Exception is too general. Consider throwing one of SPL exceptions instead.";
    private static final String messageNoArguments  = "This exception is thrown without any message, please do yourself a favor and add it.";

    @NotNull
    public String getShortName() {
        return "ThrowRawExceptionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpThrow(PhpThrow throwStatement) {
                final PsiElement argument = throwStatement.getArgument();
                if (argument instanceof NewExpression) {
                    final NewExpression newExpression   = (NewExpression) argument;
                    final ClassReference classReference = newExpression.getClassReference();
                    final String classFqn               = null == classReference ? null : classReference.getFQN();
                    if (classFqn != null) {
                        if (classFqn.equals("\\Exception")) {
                            holder.registerProblem(classReference, messageRawException, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix());
                        } else if (newExpression.getParameters().length == 0) {
                            holder.registerProblem(newExpression, messageNoArguments, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                }
            }
        };
    }

    static private class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Throw RuntimeException instead";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof ClassReference) {
                ((ClassReference) expression).handleElementRename("RuntimeException");
            }
        }
    }
}
