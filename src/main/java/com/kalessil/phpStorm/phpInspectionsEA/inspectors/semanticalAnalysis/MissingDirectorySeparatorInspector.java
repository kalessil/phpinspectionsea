package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class MissingDirectorySeparatorInspector extends BasePhpInspection {
    private static final String message = "Looks like a directory separator is missing here.";

    @NotNull
    public String getShortName() {
        return "MissingDirectorySeparatorInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpConstantReference(@NotNull ConstantReference reference) {
                final String constantName = reference.getName();
                if (constantName != null && constantName.equals("__DIR__")) {
                    this.analyze(reference);
                }
            }

            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("dirname")) {
                    this.analyze(reference);
                }
            }

            private void analyze(@NotNull PsiElement expression) {
                final PsiElement parent = expression.getParent();
                if (parent instanceof ConcatenationExpression) {
                    final PsiElement second = OpenapiElementsUtil.getSecondOperand((BinaryExpression) parent, expression);
                    if (second != null) {
                        final PsiElement target = ExpressionSemanticUtil.resolveAsStringLiteral(second);
                        if (target != null) {
                            final String content = ((StringLiteralExpression) target).getContents();
                            if (!content.startsWith("/") && !content.startsWith("\\")) {
                                holder.registerProblem(second, message);
                            }
                        }
                    }
                }
            }
        };
    }
}
