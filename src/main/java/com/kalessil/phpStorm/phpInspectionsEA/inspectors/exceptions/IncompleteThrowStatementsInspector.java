package com.kalessil.phpStorm.phpInspectionsEA.inspectors.exceptions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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
    private static final String messageSprintf = "It's probably intended to use 'sprintf(...)' here.";

    @NotNull
    public String getShortName() {
        return "IncompleteThrowStatementsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpNewExpression(@NotNull NewExpression expression) {
                final PsiElement parent       = expression.getParent();
                final ClassReference argument = expression.getClassReference();
                if (argument != null && parent != null) {
                    /* pattern '... new Exception('...%s...'[, ...]);' */
                    final PsiElement[] params = expression.getParameters();
                    if (params.length > 0 && params[0] instanceof StringLiteralExpression) {
                        final String exceptionMessage = ((StringLiteralExpression) params[0]).getContents();
                        if (exceptionMessage.contains("%s") && this.isExceptionClass(argument)) {
                            final String replacement = "sprintf(" + params[0].getText() + ", )";
                            holder.registerProblem(params[0], messageSprintf, new AddMissingSprintfFix(replacement));
                        }
                    }
                    /* pattern 'new Exception(...);' */
                    if (OpenapiTypesUtil.isStatementImpl(parent) && this.isExceptionClass(argument)) {
                        holder.registerProblem(expression, messageThrow, new AddMissingThrowFix());
                    }
                }
            }

            private boolean isExceptionClass(@NotNull ClassReference reference) {
                boolean result            = false;
                final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                if (resolved instanceof PhpClass) {
                    final Set<PhpClass> inheritanceChain = InterfacesExtractUtil.getCrawlInheritanceTree((PhpClass) resolved, true);
                    for (final PhpClass clazz : inheritanceChain) {
                        if (clazz.getFQN().equals("\\Exception")) {
                            result = true;
                            break;
                        }
                    }
                    inheritanceChain.clear();
                }
                return result;
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

    private static class AddMissingThrowFix implements LocalQuickFix {
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
            if (expression != null && !project.isDisposed()) {
                final PhpThrow implant = PhpPsiElementFactory.createFromText(project, PhpThrow.class, "throw $x;");
                if (implant != null) {
                    final PsiElement socket = implant.getArgument();
                    if (socket != null) {
                        socket.replace(expression.copy());
                        expression.getParent().replace(implant);
                    }
                }
            }
        }
    }
}
