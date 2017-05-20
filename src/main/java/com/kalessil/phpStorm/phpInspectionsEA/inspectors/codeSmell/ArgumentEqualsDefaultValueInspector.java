package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.PhpElementType;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/**
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ArgumentEqualsDefaultValueInspector extends BasePhpInspection {
    private static final String message = "The argument can be safely dropped, as it identical to the default value.";

    @NotNull
    public final String getShortName() {
        return "ArgumentEqualsDefaultValueInspection";
    }

    @NotNull
    @Override
    public final PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean onTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(final MethodReference reference) {
                this.analyze(reference);
            }

            @Override
            public void visitPhpFunctionCall(final FunctionReference reference) {
                this.analyze(reference);
            }

            private void analyze(@NotNull FunctionReference reference) {
                final PsiElement[] arguments = reference.getParameters();
                final Function function      = arguments.length > 0 ? (Function) reference.resolve() : null;
                if (function != null) {
                    PsiElement reportFrom = null;
                    PsiElement reportTo   = null;

                    final Parameter[] parameters = function.getParameters();
                    for (int index = Math.min(parameters.length, arguments.length) - 1; index >= 0; --index) {
                        final PsiElement defaultValue = parameters[index].getDefaultValue();
                        final PsiElement argument     = arguments[index];
                        if (defaultValue == null || !PsiEquivalenceUtil.areElementsEquivalent(defaultValue, argument)) {
                            break;
                        }

                        reportFrom = argument;
                        if (reportTo == null) {
                            reportTo = argument;
                        }
                    }

                    if (reportFrom != null) {
                        problemsHolder.registerProblem(
                                problemsHolder.getManager().createProblemDescriptor(
                                        reportFrom,
                                        reportTo,
                                        message,
                                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                        onTheFly,
                                        new TheLocalFix(reportFrom, reportTo)
                                )
                        );
                    }
                }
            }
        };
    }

    private class TheLocalFix implements LocalQuickFix {
        private final SmartPsiElementPointer<PsiElement> dropFrom;
        private final SmartPsiElementPointer<PsiElement> dropTo;

        private TheLocalFix(@NotNull PsiElement dropFrom, @NotNull PsiElement dropTo) {
            final SmartPointerManager manager = SmartPointerManager.getInstance(dropFrom.getProject());

            this.dropFrom = manager.createSmartPsiElementPointer(dropFrom);
            this.dropTo   = manager.createSmartPsiElementPointer(dropTo);
        }

        @NotNull
        @Override
        public String getName() {
            return "Drop unneeded arguments";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            PsiElement dropFrom     = this.dropFrom.getElement();
            final PsiElement dropTo = this.dropTo.getElement();
            if (dropFrom != null && dropTo != null) {
                PsiElement   previous = dropFrom.getPrevSibling();
                IElementType prevType = previous == null ? null : previous.getNode().getElementType();
                while (
                    (prevType == PhpTokenTypes.opCOMMA && previous != null) ||
                    previous instanceof PsiWhiteSpace || previous instanceof PsiComment
                ) {
                    dropFrom = previous;
                    previous = previous.getPrevSibling();
                    prevType = previous == null ? null : previous.getNode().getElementType();
                }
                dropFrom.getParent().deleteChildRange(dropFrom, dropTo);
            }
        }
    }
}
