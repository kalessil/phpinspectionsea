package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;


import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Funivan <alotofall@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UsageOfSilenceOperatorInspector extends BasePhpInspection {
    private static final String message = "Usage of a silence operator.";

    private static final List<String> FUNCTIONS = Arrays.asList(
            "\\unlink", "\\mkdir", "\\trigger_error"
    );

    @NotNull
    public String getShortName() {
        return "UsageOfSilenceOperatorInspection";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            public void visitPhpUnaryExpression(UnaryExpression expr) {
                final PsiElement operation = expr.getOperation();
                if (!PhpPsiUtil.isOfType(operation, PhpTokenTypes.opSILENCE)) {
                    return;
                }


                final PsiElement lastElement = expr.getValue();
                if (OpenapiTypesUtil.isFunctionReference(lastElement)) {
                    final String functionName = ((FunctionReference) lastElement).getFQN();
                    if (FUNCTIONS.contains(functionName)) {
                        return;
                    }
                }

                holder.registerProblem(operation, message, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }

}
