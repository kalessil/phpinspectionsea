package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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
    private static final String messagePattern = "'%s' should be used instead (saves memory).";

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
                final String functionName = reference.getName();
                if (functionName != null && mapping.containsKey(functionName)) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length >= 2) {
                        /* checks implicit boolean comparison pattern */
                        final PsiElement parent = reference.getParent();
                        if (parent instanceof BinaryExpression) {
                            final BinaryExpression binary = (BinaryExpression) parent;
                            if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(binary.getOperationType())) {
                                final PsiElement secondOperand = OpenapiElementsUtil.getSecondOperand(binary, reference);
                                if (PhpLanguageUtil.isFalse(secondOperand)) {
                                    final PsiElement operationNode = binary.getOperation();
                                    if (operationNode != null) {
                                        final String operation   = operationNode.getText();
                                        final String replacement = "false %o% %f%(%s%, %p%)"
                                                .replace("%p%", arguments[1].getText())
                                                .replace("%s%", arguments[0].getText())
                                                .replace("%f%", mapping.get(functionName))
                                                .replace("%o%", operation.length() == 2 ? operation + '=' : operation);
                                        holder.registerProblem(
                                                binary,
                                                String.format(messagePattern, replacement),
                                                new UseStrposFix(replacement)
                                        );
                                        return;
                                    }
                                }
                            }
                        }
                        /* checks non-implicit boolean comparison patternS */
                        if (ExpressionSemanticUtil.isUsedAsLogicalOperand(reference)) {
                            final String replacement = "false %o% %f%(%s%, %p%)"
                                .replace("%p%", arguments[1].getText())
                                .replace("%s%", arguments[0].getText())
                                .replace("%f%", mapping.get(functionName))
                                .replace("%o%", parent instanceof UnaryExpression ? "===": "!==");
                            holder.registerProblem(
                                    parent instanceof UnaryExpression ? parent : reference,
                                    String.format(messagePattern, replacement),
                                    new UseStrposFix(replacement)
                            );
                        }
                    }
                }
            }
        };
    }

    private static final class UseStrposFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use str[i]pos() instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseStrposFix(@NotNull String expression) {
            super(expression);
        }
    }
}
