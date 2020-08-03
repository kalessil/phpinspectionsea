package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
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

public class IsIterableCanBeUsedInspector extends BasePhpInspection {
    private static final String message = "'is_array(%s) || %s instanceof Traversable' can be replaced by 'is_iterable(%s)'.";

    @NotNull
    @Override
    public String getShortName() {
        return "IsIterableCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'is_iterable(...)' can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("is_array")) {
                    final boolean isTargetVersion = PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP710);
                    if (isTargetVersion) {
                        final PsiElement[] arguments = reference.getParameters();
                        final PsiElement parent      = reference.getParent();
                        if (parent instanceof BinaryExpression && arguments.length == 1) {
                            final BinaryExpression binary = (BinaryExpression) parent;
                            final IElementType operation  = binary.getOperationType();
                            if (operation == PhpTokenTypes.opOR) {
                                /* find the high-level binary expression */
                                BinaryExpression context = binary;
                                while (context instanceof BinaryExpression) {
                                    PsiElement up = context.getParent();
                                    while (up instanceof ParenthesizedExpression) {
                                        up = up.getParent();
                                    }
                                    if (up instanceof BinaryExpression && ((BinaryExpression) up).getOperationType() == PhpTokenTypes.opOR) {
                                        context = (BinaryExpression) up;
                                    } else {
                                        break;
                                    }
                                }
                                /* check the pattern */
                                final List<PsiElement> fragments = this.extract(context, PhpTokenTypes.opOR);
                                if (! fragments.isEmpty()) {
                                    if (fragments.size() > 1) {
                                        for (final PsiElement fragment : fragments) {
                                            if (fragment != reference && fragment instanceof BinaryExpression) {
                                                final BinaryExpression candidate = (BinaryExpression) fragment;
                                                if (candidate.getOperationType() == PhpTokenTypes.kwINSTANCEOF) {
                                                    final PsiElement clazz = candidate.getRightOperand();
                                                    if (clazz instanceof ClassReference && "Traversable".equals(((ClassReference) clazz).getName())) {
                                                        final PsiElement subject = candidate.getLeftOperand();
                                                        if (subject != null && OpenapiEquivalenceUtil.areEqual(subject, arguments[0])) {
                                                            final String argument = subject.getText();
                                                            holder.registerProblem(
                                                                    reference,
                                                                    String.format(MessagesPresentationUtil.prefixWithEa(message), argument, argument, argument)
                                                            );
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    fragments.clear();
                                }
                            }
                        }
                    }
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
                                result.addAll(this.extract((BinaryExpression) expression, operator));
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
