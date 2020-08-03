package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ShortListSyntaxCanBeUsedInspector extends BasePhpInspection {
    private static final String messageForeach = "'foreach (... as [...])' can be used here.";
    private static final String messageAssign  = "'[...] = ...' can be used here.";

    @NotNull
    @Override
    public String getShortName() {
        return "ShortListSyntaxCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Short list syntax can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMultiassignmentExpression(@NotNull MultiassignmentExpression assignment) {
                /* ensure php version is at least PHP 7.1 */
                if (PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP710)) {
                    /* verify if it's dedicated statement and it's the list(...) construction */
                    final PsiElement parent = assignment.getParent();
                    if (OpenapiTypesUtil.isStatementImpl(parent)) {
                        final PsiElement listKeyword = assignment.getFirstChild();
                        if (OpenapiTypesUtil.is(listKeyword, PhpTokenTypes.kwLIST)) {
                            holder.registerProblem(
                                    listKeyword,
                                    MessagesPresentationUtil.prefixWithEa(messageAssign),
                                    new TheLocalFix(holder.getProject(), assignment)
                            );
                        }
                    }
                }
            }

            @Override
            public void visitPhpForeach(@NotNull ForeachStatement foreach) {
                /* ensure php version is at least PHP 7.1 */
                if (PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP710)) {
                    final List<Variable> variables = foreach.getVariables();
                    if (!variables.isEmpty()) {
                        PsiElement childNode = foreach.getFirstChild();
                        while (childNode != null && !(childNode instanceof GroupStatement)) {
                            if (OpenapiTypesUtil.is(childNode, PhpTokenTypes.kwLIST)) {
                                holder.registerProblem(
                                        childNode,
                                        MessagesPresentationUtil.prefixWithEa(messageForeach),
                                        new TheLocalFix(holder.getProject(), foreach)
                                );
                                break;
                            }
                            childNode = childNode.getNextSibling();
                        }
                    }
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use [...] instead";

        private final SmartPsiElementPointer<PsiElement> context;

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        TheLocalFix(@NotNull Project project, @NotNull PsiElement context) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(project);
            this.context = factory.createSmartPsiElementPointer(context);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement listKeyword = descriptor.getPsiElement();
            if (listKeyword == null || project.isDisposed()) {
                return;
            }

            /* find needed brackets */
            PsiElement openingBracket = null;
            PsiElement closingBracket = null;
            PsiElement current        = listKeyword;
            int nestingLevel          = 0;
            while (current != null) {
                if (OpenapiTypesUtil.is(current, PhpTokenTypes.chLPAREN)) {
                    if (nestingLevel == 0) {
                        openingBracket = current;
                    }
                    ++nestingLevel;
                }
                if (OpenapiTypesUtil.is(current, PhpTokenTypes.chRPAREN)) {
                    --nestingLevel;
                    if (nestingLevel == 0) {
                        closingBracket = current;
                    }
                }
                current = current.getNextSibling();
            }

            /* apply refactoring */
            if (openingBracket != null && closingBracket != null) {
                final PsiElement openBracket  = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "[");
                final PsiElement closeBracket = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "]");
                if (openBracket != null && closeBracket != null) {
                    /* cleanup following-up space */
                    final PsiElement after = listKeyword.getNextSibling();
                    if (after instanceof PsiWhiteSpace) {
                        after.delete();
                    }
                    /* transform brackets, drop list keyword */
                    openingBracket.replace(openBracket);
                    closingBracket.replace(closeBracket);
                    listKeyword.delete();

                    final PsiElement context = this.context.getElement();
                    if (context instanceof AssignmentExpression) {
                        /* PhpStorm 2018.2 BC break: PSI tree structure has changed */
                        context.replace(PhpPsiElementFactory.createPhpPsiFromText(project, AssignmentExpression.class, context.getText()));
                    }
                }
            }
        }
    }
}
