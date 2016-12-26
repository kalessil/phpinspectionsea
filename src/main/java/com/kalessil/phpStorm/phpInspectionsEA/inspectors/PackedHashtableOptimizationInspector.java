package com.kalessil.phpStorm.phpInspectionsEA.inspectors;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.PhpExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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
    // see http://jpauli.github.io/2016/04/08/hashtables.html#packed-hashtable-optimization
    // array(0 => $x, '1' => $y)
    // array('0' => '', '1' => null, '2' => null, '3' => '', '4' => null)

    @NotNull
    public String getShortName() {
        return "PackedHashtableOptimizationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* TODO: loop with decreasing index: index used as a key in write context */

            public void visitPhpArrayCreationExpression(ArrayCreationExpression expression) {
                /* requires PHP7 */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (phpVersion.compareTo(PhpLanguageLevel.PHP700) < 0) {
                    return;
                }

                /* step 1: collect indexes and verify array structure */
                boolean isTarget                  = true;
                final List<PhpPsiElement> indexes = new ArrayList<>();
                for (PsiElement pairCandidate : expression.getChildren()) {
                    /* inspect only associative arrays */
                    if (!(pairCandidate instanceof ArrayHashElement)) {
                        isTarget = false;
                        break;
                    }

                    /* ensure key is available */
                    final ArrayHashElement pair = (ArrayHashElement) pairCandidate;
                    final PhpPsiElement key     = null == pair.getKey() ? null : pair.getKey().getFirstPsiChild();
                    if (null == key) {
                        isTarget = false;
                        break;
                    }

                    if (key instanceof StringLiteralExpression || key instanceof PhpExpressionImpl) {
                        /* no injections expected */
                        if (null != key.getFirstPsiChild()) {
                            isTarget = false;
                            break;
                        }

                        indexes.add(key);
                        continue;
                    }

                    /* key is not as expected, stop processing */
                    isTarget = false;
                    break;
                }
                if (!isTarget) {
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
                    } else {
                        numericIndex = index.getText().replaceAll("\\s+", "");
                    }

                    /* try converting into integer */
                    try {
                        integerIndex = Integer.valueOf(numericIndex);
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
                    final String message = "Reordering element by increasing key would enable packed hashtable optimizations here";
                    holder.registerProblem(expression.getFirstChild(), message, ProblemHighlightType.WEAK_WARNING);
                }
                if (hasIncreasingIndexes && hasStringIndexes) {
                    final String message = "Using integer keys would enable packed hashtable optimizations here";
                    holder.registerProblem(expression.getFirstChild(), message, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}
