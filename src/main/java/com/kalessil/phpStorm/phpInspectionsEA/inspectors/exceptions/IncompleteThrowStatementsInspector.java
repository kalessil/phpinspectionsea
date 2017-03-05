package com.kalessil.phpStorm.phpInspectionsEA.inspectors.exceptions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class IncompleteThrowStatementsInspector extends BasePhpInspection {
    private static final String messageThrow   = "It's probably intended to throw an exception here.";
    private static final String messageSprintf = "It's probably intended to use sprintf here.";

    @NotNull
    public String getShortName() {
        return "IncompleteThrowStatementsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpNewExpression(NewExpression expression) {
                final PsiElement parent       = expression.getParent();
                final ClassReference argument = expression.getClassReference();
                if (null == argument || null == parent) {
                    return;
                }

                /* pattern '... new Exception('...%s...'[, ...]);' */
                final PsiElement[] params = expression.getParameters();
                if (params.length > 0 && params[0] instanceof StringLiteralExpression) {
                    boolean containsPlaceholders = ((StringLiteralExpression) params[0]).getContents().contains("%s");
                    if (containsPlaceholders && isExceptionClass(argument)) {
                        final String replacement = "sprintf(" + params[0].getText() + ", )";
                        holder.registerProblem(params[0], messageSprintf, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new AddMissingSprintfFix(replacement));
                    }
                }

                /* pattern 'new Exception(...);' */
                if (parent.getClass() == StatementImpl.class && isExceptionClass(argument)) {
                    holder.registerProblem(expression, messageThrow, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new AddMissingThrowFix());
                }
            }

            private boolean isExceptionClass(@NotNull ClassReference reference) {
                final PsiElement resolved = reference.resolve();
                if (resolved instanceof PhpClass) {
                    final Set<PhpClass> inheritanceChain = InterfacesExtractUtil.getCrawlCompleteInheritanceTree((PhpClass) resolved, true);
                    for (PhpClass clazz : inheritanceChain) {
                        if (clazz.getFQN().equals("\\Exception")) {
                            inheritanceChain.clear();
                            return true;
                        }
                    }
                    inheritanceChain.clear();
                }

                return false;
            }
        };
    }

    private class AddMissingSprintfFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Wrap with sprintf(...)";
        }

        AddMissingSprintfFix(@NotNull String expression) {
            super(expression);
        }
    }

    private class AddMissingThrowFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Add missing throw keyword";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (null != expression) {
                final PhpThrow throwExpression = PhpPsiElementFactory.createFromText(project, PhpThrow.class, "throw $x;");
                if (null != throwExpression) {
                    //noinspection ConstantConditions as we dealing with hard-coded expressions here
                    throwExpression.getArgument().replace(expression.copy());
                    expression.getParent().replace(throwExpression);
                }
            }
        }
    }
}
