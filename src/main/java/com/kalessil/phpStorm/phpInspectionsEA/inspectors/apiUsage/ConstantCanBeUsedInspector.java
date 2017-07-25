package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ConstantCanBeUsedInspector extends BasePhpInspection {
    private static final String messagePattern = "%c% constant should be used instead.";

    static private final Map<String, String> functions = new HashMap<>();
    static {
        functions.put("phpversion",    "PHP_VERSION");
        functions.put("php_sapi_name", "PHP_SAPI");
        functions.put("get_class",     "__CLASS__");
        functions.put("pi",            "M_PI");
        functions.put("php_uname",     "PHP_OS");
    }

    @NotNull
    public String getShortName() {
        return "ConstantCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName    = reference.getName();
                final PsiElement[] arguments = reference.getParameters();
                if (functionName != null && functions.containsKey(functionName)) {
                    /* classification part */
                    boolean canUseConstant = false;
                    if (arguments.length == 0) {
                        canUseConstant = true;
                    } else if (arguments.length == 1 && functionName.equals("php_uname")) {
                        canUseConstant =
                                arguments[0] instanceof StringLiteralExpression &&
                                ((StringLiteralExpression) arguments[0]).getContents().equals("s");
                    }
                    /* reporting part */
                    if (canUseConstant) {
                        final String constant = functions.get(functionName);
                        final String message  = messagePattern.replace("%c%", constant);
                        holder.registerProblem(reference, message, new UseConstantFix(constant));
                    }
                }
            }
        };
    }

    private class UseConstantFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use the constant instead";
        }

        UseConstantFix(@NotNull String expression) {
            super(expression);
        }
    }
}