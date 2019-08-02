package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ConcatenationExpression;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
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

public class MissingDirectorySeparatorInspector extends PhpInspection {
    private static final String message = "Looks like a directory separator is missing here.";

    final private static Set<String> targetFunctions = new HashSet<>();
    static {
        targetFunctions.add("getcwd");
        targetFunctions.add("dirname");
        targetFunctions.add("realpath");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "MissingDirectorySeparatorInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpConstantReference(@NotNull ConstantReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String constantName = reference.getName();
                if (constantName != null && constantName.equals("__DIR__")) {
                    this.analyze(reference);
                }
            }

            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && targetFunctions.contains(functionName)) {
                    this.analyze(reference);
                }
            }

            private void analyze(@NotNull PsiElement expression) {
                final PsiElement parent = expression.getParent();
                if (parent instanceof ConcatenationExpression) {
                    final PsiElement grandParent = parent.getParent();
                    /* identify the string literal candidate */
                    final PsiElement candidate;
                    final ConcatenationExpression concatenation = (ConcatenationExpression) parent;
                    if (concatenation.getLeftOperand() == expression) {
                        candidate = concatenation.getRightOperand();
                    } else if (grandParent instanceof ConcatenationExpression) {
                        candidate = ((ConcatenationExpression) grandParent).getRightOperand();
                    } else {
                        candidate = null;
                    }
                    /* inspect the candidate */
                    if (candidate != null) {
                        final StringLiteralExpression target = ExpressionSemanticUtil.resolveAsStringLiteral(candidate);
                        if (target != null) {
                            final String content = target.getContents();
                            if (!content.startsWith("/") && !content.startsWith("\\") && content.matches("^\\w.*$")) {
                                holder.registerProblem(candidate, message);
                            }
                        }
                    }
                }
            }
        };
    }
}
