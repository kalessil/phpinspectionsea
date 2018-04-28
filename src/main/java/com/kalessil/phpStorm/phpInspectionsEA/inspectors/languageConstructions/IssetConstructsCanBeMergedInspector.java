package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.PhpIsset;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class IssetConstructsCanBeMergedInspector extends BasePhpInspection {
    private static final String messageIsset        = "This can be merged into the previous 'isset(..., ...[, ...])'.";
    private static final String messageIvertedIsset = "This can be merged into the previous '!isset(..., ...[, ...])'.";

    @NotNull
    public String getShortName() {
        return "IssetConstructsCanBeMergedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                final IElementType operator = expression.getOperationType();
                if (operator == PhpTokenTypes.opAND || operator == PhpTokenTypes.opOR) {
                    /* false-positives: part of another condition */
                    final PsiElement parent = expression.getParent();
                    if (parent instanceof BinaryExpression && ((BinaryExpression) parent).getOperationType() == operator) {
                        return;
                    }

                    final List<PsiElement> fragments = this.extract(expression, operator);
                    if (fragments.size() > 1) {
                        /* handle isset && isset ... */
                        if (operator == PhpTokenTypes.opAND) {
                            int hitsCount = 0;
                            for (final PsiElement fragment : fragments) {
                                if (fragment instanceof PhpIsset && ++hitsCount > 1) {
                                    holder.registerProblem(fragment, messageIsset);
                                }
                            }
                        }
                        /* handle !isset || !isset ... */
                        else {
                            int hitsCount = 0;
                            for (final PsiElement fragment : fragments) {
                                if (fragment instanceof UnaryExpression) {
                                    final PsiElement argument  = ((UnaryExpression) fragment).getValue();
                                    final PsiElement candidate = ExpressionSemanticUtil.getExpressionTroughParenthesis(argument);
                                    if (candidate instanceof PhpIsset && ++hitsCount > 1) {
                                        holder.registerProblem(candidate, messageIvertedIsset);
                                    }
                                }
                            }
                        }

                    }
                    fragments.clear();
                }
            }

            @NotNull
            private List<PsiElement> extract(@NotNull BinaryExpression binary, @Nullable IElementType operator) {
                final List<PsiElement> result = new ArrayList<>();
                if (binary.getOperationType() == operator) {
                    Stream.of(binary.getLeftOperand(), binary.getRightOperand())
                        .filter(Objects::nonNull).map(ExpressionSemanticUtil::getExpressionTroughParenthesis)
                        .forEach(expression -> {
                            if (expression instanceof BinaryExpression) {
                                result.addAll(extract((BinaryExpression) expression, operator));
                            } else {
                                result.add(expression);
                            }
                        });
                } else {
                    result.add(binary);
                }
                return result;
            }
        };
    }
}
