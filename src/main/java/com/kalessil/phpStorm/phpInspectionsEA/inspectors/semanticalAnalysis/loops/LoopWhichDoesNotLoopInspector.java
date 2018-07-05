package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class LoopWhichDoesNotLoopInspector extends BasePhpInspection {
    private static final String message = "This loop does not loop.";

    private static final Set<String> foreachExceptions = new HashSet<>();
    static {
        foreachExceptions.add("\\Generator");
        foreachExceptions.add("\\Traversable");
        foreachExceptions.add("\\Iterator");
    }

    @NotNull
    public String getShortName() {
        return "LoopWhichDoesNotLoopInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement loop) {
                if (this.isNotLooping(loop)) {
                    /* false-positive: return first element from generator, iterable and co */
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(loop);
                    final PsiElement last     = body == null ? null : ExpressionSemanticUtil.getLastStatement(body);
                    if (last != null && !(last instanceof PhpThrow)) {
                        final PsiElement source = loop.getArray();
                        if (source instanceof PhpTypedElement) {
                            final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) source, source.getProject());
                            final boolean isValid  = resolved != null && resolved.filterUnknown().getTypes().stream()
                                    .anyMatch(type -> foreachExceptions.contains(Types.getType(type)));
                            if (isValid) {
                                return;
                            }
                        }
                    }

                    holder.registerProblem(loop.getFirstChild(), message);
                }
            }

            @Override
            public void visitPhpFor(@NotNull For loop) {
                if (this.isNotLooping(loop)) {
                    holder.registerProblem(loop.getFirstChild(), message);
                }
            }

            @Override
            public void visitPhpWhile(@NotNull While loop) {
                if (this.isNotLooping(loop)) {
                    holder.registerProblem(loop.getFirstChild(), message);
                }
            }

            @Override
            public void visitPhpDoWhile(@NotNull DoWhile loop) {
                if (this.isNotLooping(loop)) {
                    holder.registerProblem(loop.getFirstChild(), message);
                }
            }

            private boolean isNotLooping(@NotNull PhpPsiElement loop) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(loop);
                if (null == body) {
                    return false;
                }

                final PsiElement lastExpression                  = ExpressionSemanticUtil.getLastStatement(body);
                final boolean isLoopTerminatedWithLastExpression = lastExpression instanceof PhpBreak ||
                                                                   lastExpression instanceof PhpReturn ||
                                                                   lastExpression instanceof PhpThrow;
                /* loop is empty or terminates on first iteration */
                if (null != lastExpression && !isLoopTerminatedWithLastExpression) {
                    return false;
                }

                /* detect continue statements, which makes the loop looping */
                for (final PhpContinue expression : PsiTreeUtil.findChildrenOfType(body, PhpContinue.class)) {
                    int nestingLevel  = 0;
                    PsiElement parent = expression.getParent();
                    while (null != parent && !(parent instanceof Function) && !(parent instanceof PsiFile)) {
                        if (OpenapiTypesUtil.isLoop(parent)) {
                            ++nestingLevel;

                            if (parent == loop) {
                                /* extract level of continuation from the statement */
                                int continueLevel         = 1;
                                final PsiElement argument = expression.getArgument();
                                if (null != argument) {
                                    try {
                                        continueLevel = Integer.parseInt(argument.getText());
                                    } catch (final NumberFormatException notParsed) {
                                        continueLevel = 1;
                                    }
                                }

                                /* matched continue for the current loop */
                                if (continueLevel == nestingLevel) {
                                    return false;
                                }
                            }
                        }
                        parent = parent.getParent();
                    }
                }

                return true;
            }
        };
    }
}
