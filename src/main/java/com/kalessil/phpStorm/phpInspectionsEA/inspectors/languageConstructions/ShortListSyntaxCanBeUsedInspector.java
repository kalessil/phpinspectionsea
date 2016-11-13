package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.ForeachStatement;
import com.jetbrains.php.lang.psi.elements.MultiassignmentExpression;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShortListSyntaxCanBeUsedInspector extends BasePhpInspection {
    private static final String messageForeach = "'foreach (... as [...])' can be used here";
    private static final String messageAssign  = "'[...] = ...' can be used here";

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
                if (!(parent instanceof StatementImpl)) {
                    return;
                }
                final PsiElement listKeyword = multiassignmentExpression.getFirstChild();
                if (null != listKeyword && listKeyword.getText().equalsIgnoreCase("list")) {
                    holder.registerProblem(listKeyword, messageAssign, ProblemHighlightType.WEAK_WARNING);
                }

            }

            public void visitPhpForeach(ForeachStatement foreach) {
                final List<Variable> variables = foreach.getVariables();
                if (variables.size() > 0) {
                    for (PsiElement node : foreach.getChildren()) {
                        if (node.getClass() == LeafPsiElement.class && node.getText().equalsIgnoreCase("list")) {
                            holder.registerProblem(node, messageForeach, ProblemHighlightType.WEAK_WARNING);
                            break;
                        }
                    }
                }
            }
        };
    }
}
