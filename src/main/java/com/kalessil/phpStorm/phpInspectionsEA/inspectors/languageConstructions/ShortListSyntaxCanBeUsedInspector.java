package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ForeachStatement;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.MultiassignmentExpression;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShortListSyntaxCanBeUsedInspector extends BasePhpInspection {
    private static final String messageForeach = "'foreach (... as [...])' can be used here.";
    private static final String messageAssign  = "'[...] = ...' can be used here.";

    @NotNull
    public String getShortName() {
        return "ShortListSyntaxCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMultiassignmentExpression(MultiassignmentExpression multiassignmentExpression) {
                /* ensure php version is at least PHP 7.1 */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (phpVersion.compareTo(PhpLanguageLevel.PHP710) < 0) {
                    return;
                }

                /* verify if it's dedicated statement and it's the list(...) construction */
                final PsiElement parent = multiassignmentExpression.getParent();
                if (!OpenapiTypesUtil.isStatementImpl(parent)) {
                    return;
                }
                final PsiElement listKeyword = multiassignmentExpression.getFirstChild();
                if (null != listKeyword && PhpTokenTypes.kwLIST == listKeyword.getNode().getElementType()) {
                    holder.registerProblem(listKeyword, messageAssign, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
                }
            }

            public void visitPhpForeach(ForeachStatement foreach) {
                /* ensure php version is at least PHP 7.1 */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (phpVersion.compareTo(PhpLanguageLevel.PHP710) < 0) {
                    return;
                }

                final List<Variable> variables = foreach.getVariables();
                if (variables.size() > 0) {
                    PsiElement childNode = foreach.getFirstChild();
                    while (null != childNode) {
                        if (childNode.getClass() == LeafPsiElement.class && PhpTokenTypes.kwLIST == childNode.getNode().getElementType()) {
                            holder.registerProblem(childNode, messageForeach, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
                            break;
                        }

                        childNode = childNode.getNextSibling();
                        if (childNode instanceof GroupStatement) {
                            break;
                        }
                    }
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use [...] instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement listKeyword = descriptor.getPsiElement();
            if (null == listKeyword) {
                return;
            }

            /* find needed brackets */
            PsiElement openingBracket = null;
            PsiElement closingBracket = null;
            PsiElement current        = listKeyword;
            int nestingLevel          = 0;
            while (null != current) {
                final IElementType nodeType = current.getNode().getElementType();

                if (PhpTokenTypes.chLPAREN == nodeType) {
                    if (0 == nestingLevel) {
                        openingBracket = current;
                    }
                    ++nestingLevel;
                }
                if (PhpTokenTypes.chRPAREN == nodeType) {
                    --nestingLevel;
                    if (0 == nestingLevel) {
                        closingBracket = current;
                    }
                }

                current = current.getNextSibling();
            }

            /* apply refactoring */
            if (null != openingBracket && null != closingBracket) {
                final PsiElement openBracket  = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "[");
                final PsiElement closeBracket = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "]");
                if (null != openBracket && null != closeBracket) {
                    /* cleanup following-up space */
                    final PsiElement after = listKeyword.getNextSibling();
                    if (after instanceof PsiWhiteSpace) {
                        after.delete();
                    }
                    /* transform brackets, drop list keyword */
                    openingBracket.replace(openBracket);
                    closingBracket.replace(closeBracket);
                    listKeyword.delete();
                }
            }
        }
    }

}
