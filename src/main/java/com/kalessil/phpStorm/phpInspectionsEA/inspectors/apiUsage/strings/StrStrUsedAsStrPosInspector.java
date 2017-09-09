package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class StrStrUsedAsStrPosInspector extends BasePhpInspection {
    private static final String messagePattern = "'%e%' should be used instead (saves memory).";

    @NotNull
    public String getShortName() {
        return "StrStrUsedAsStrPosInspection";
    }

    private static final HashMap<String, String> mapping = new HashMap<>();
    static {
        mapping.put("strstr",  "strpos");
        mapping.put("stristr", "stripos");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                /* check if it's the target function */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (params.length < 2 || functionName == null || !mapping.containsKey(functionName)) {
                    return;
                }

                /* checks implicit boolean comparison pattern */
                if (reference.getParent() instanceof BinaryExpression) {
                    final BinaryExpression parent    = (BinaryExpression) reference.getParent();
                    final IElementType operationType = parent.getOperationType();
                    if (PhpTokenTypes.tsCOMPARE_EQUALITY_OPS.contains(operationType)) {
                        /* get second operand */
                        PsiElement secondOperand = parent.getLeftOperand();
                        if (secondOperand == reference) {
                            secondOperand = parent.getRightOperand();
                        }

                        /* verify if operand is a boolean and report an issue */
                        final PsiElement operationNode = parent.getOperation();
                        if (operationNode != null && PhpLanguageUtil.isBoolean(secondOperand)) {
                            final String operator    = operationNode.getText();
                            final String replacement = "false %o% %f%(%s%, %p%)"
                                .replace("%p%", params[1].getText())
                                .replace("%s%", params[0].getText())
                                .replace("%f%", mapping.get(functionName))
                                .replace("%o%", operator.length() == 2 ? operator + "=": operator);
                            final String message     = messagePattern.replace("%e%", replacement);
                            holder.registerProblem(parent, message, new UseStrposFix(replacement));

                            return;
                        }
                    }
                }

                /* checks NON-implicit boolean comparison patternS */
                if (ExpressionSemanticUtil.isUsedAsLogicalOperand(reference)) {
                    final PsiElement parent = reference.getParent();
                    final PsiElement target = parent instanceof UnaryExpression ? parent : reference;

                    final String replacement = "false %o% %f%(%s%, %p%)"
                        .replace("%p%", params[1].getText())
                        .replace("%s%", params[0].getText())
                        .replace("%f%", mapping.get(functionName))
                        .replace("%o%", reference.getParent() instanceof UnaryExpression ? "===": "!==");
                    final String message     = messagePattern.replace("%e%", replacement);
                    holder.registerProblem(target, message, new UseStrposFix(replacement));

                    //return;
                }
            }
        };
    }

    private class UseStrposFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use str[i]pos() instead";
        }

        UseStrposFix(@NotNull String expression) {
            super(expression);
        }
    }
}
