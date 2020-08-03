package com.kalessil.phpStorm.phpInspectionsEA.inspectors;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class PackedHashtableOptimizationInspector extends BasePhpInspection {
    private static final String messageReorder        = "Reordering keys in natural ascending order would enable array optimizations here.";
    private static final String messageUseNumericKeys = "Using integer keys would enable array optimizations here.";

    @NotNull
    @Override
    public String getShortName() {
        return "PackedHashtableOptimizationInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Packed hashtable optimizations";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* TODO: docs, http://blog.jpauli.tech/2016/04/08/hashtables.html#packed-hashtable-optimization */

            @Override
            public void visitPhpArrayCreationExpression(@NotNull ArrayCreationExpression expression) {
                /* requires PHP7 */
                if (PhpLanguageLevel.get(holder.getProject()).below(PhpLanguageLevel.PHP700)) {
                    return;
                }
                /* requires at least 3 children - let array to grow enough */
                final PsiElement[] children = expression.getChildren();
                if (children.length < 3) {
                    return;
                }

                /* false-positives: test classes */
                if (this.isTestContext(expression)) {
                    return;
                }

                /* step 1: collect indexes and verify array structure */
                final List<PhpPsiElement> indexes = new ArrayList<>();
                for (final PsiElement pairCandidate : children) {
                    if (pairCandidate instanceof ArrayHashElement) {
                        final PhpPsiElement key = ((ArrayHashElement) pairCandidate).getKey();
                        if ((key instanceof StringLiteralExpression && key.getFirstPsiChild() == null) || OpenapiTypesUtil.isNumber(key)) {
                            indexes.add(key);
                            continue;
                        }
                    }
                    break;
                }
                if (indexes.size() != children.length) {
                    indexes.clear();
                    return;
                }

                /* step 2: analyze collected indexes */
                // if string literal is not numeric => stop
                boolean hasStringIndexes       = false;
                boolean hasIncreasingIndexes   = true;
                int lastIndex                  = Integer.MIN_VALUE;
                for (PhpPsiElement index : indexes) {
                    final String numericIndex;
                    final int integerIndex;

                    /* extract text representation of the index */
                    if (index instanceof StringLiteralExpression) {
                        hasStringIndexes = true;
                        numericIndex     = ((StringLiteralExpression) index).getContents();

                        /* '01' and etc cases can not be converted */
                        if (numericIndex.length() > 1 && '0' == numericIndex.charAt(0)) {
                            indexes.clear();
                            return;
                        }
                    } else {
                        numericIndex = index.getText().replaceAll("\\s+", "");
                    }

                    /* try converting into integer */
                    try {
                        integerIndex = Integer.parseInt(numericIndex);
                    } catch (NumberFormatException error) {
                        indexes.clear();
                        return;
                    }

                    if (integerIndex < lastIndex) {
                        hasIncreasingIndexes = false;
                    }
                    lastIndex = integerIndex;
                }

                /* report if criteria are met */
                if (!hasIncreasingIndexes) {
                    holder.registerProblem(
                            expression.getFirstChild(),
                            MessagesPresentationUtil.prefixWithEa(messageReorder)
                    );
                }
                if (hasIncreasingIndexes && hasStringIndexes) {
                    holder.registerProblem(
                            expression.getFirstChild(),
                            MessagesPresentationUtil.prefixWithEa(messageUseNumericKeys)
                    );
                }
            }
        };
    }
}
