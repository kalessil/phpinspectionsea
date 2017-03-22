package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class UsageOfSilenceOperatorInspector extends BasePhpInspection {
    private static final String message = "Usage of a silence operator.";

    private static final List<String> FUNCTIONS = Arrays.asList(
            "\\unlink", "\\mkdir"
    );

    @NotNull
    public String getShortName() {
        return "UsageOfSilenceOperatorInspection";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            public void visitPhpUnaryExpression(UnaryExpression expr) {
                PsiElement operation = expr.getOperation();
                if (!PhpPsiUtil.isOfType(operation, PhpTokenTypes.opSILENCE)) {
                    return;
                }


                PsiElement lastElement = expr.getLastChild();
                if (lastElement instanceof FunctionReference) {
                    String functionName = ((FunctionReference) lastElement).getFQN();
                    if (FUNCTIONS.contains(functionName)) {
                        return;
                    }
                }

                holder.registerProblem(operation, message);
            }
        };
    }

}
