package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.apache.commons.lang.StringUtils;
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
    static private final Map<String, String> operators = new HashMap<>();
    static {
        functions.put("phpversion",    "PHP_VERSION");
        functions.put("php_sapi_name", "PHP_SAPI");
        functions.put("get_class",     "__CLASS__");
        functions.put("pi",            "M_PI");
        functions.put("php_uname",     "PHP_OS");

        operators.put("<",  "<");
        operators.put("lt", "<");
        operators.put("<=", "<=");
        operators.put("le", "<=");
        operators.put(">",  ">");
        operators.put("gt", ">");
        operators.put(">=", ">=");
        operators.put("ge", ">=");
        operators.put("==", "===");
        operators.put("=",  "===");
        operators.put("eq", "===");
        operators.put("!=", "!==");
        operators.put("<>", "!==");
        operators.put("ne", "!==");
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
                if (functionName != null) {
                    if (functions.containsKey(functionName)) {
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
                            final String message = messagePattern.replace("%c%", constant);
                            holder.registerProblem(reference, message, new UseConstantFix(constant));
                        }
                    } else if (arguments.length == 3 && functionName.equals("version_compare")) {
                        if (arguments[0] instanceof ConstantReference && arguments[1] instanceof StringLiteralExpression) {
                            final String constant = ((ConstantReference) arguments[0]).getName();
                            final String version  = ((StringLiteralExpression) arguments[1]).getContents();
                            if (constant != null && constant.equals("PHP_VERSION") && !StringUtils.isEmpty(version)) {
                                if (arguments[2] instanceof StringLiteralExpression) {
                                    final String operator = ((StringLiteralExpression) arguments[2]).getContents();
                                    if (operators.containsKey(operator)) {
                                        /*
                                        PHP_VERSION_ID
                                        Version string:            %d(.%d)?(.%d{1,2})?(-.+)?
                                        5.2, 5.2.7, 5.2.7-extra -> $10$20$3
                                        */
                                    }
                                }
                            }
                        }
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