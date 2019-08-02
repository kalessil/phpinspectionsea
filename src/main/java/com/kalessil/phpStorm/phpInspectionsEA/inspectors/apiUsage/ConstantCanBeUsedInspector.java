package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ConstantCanBeUsedInspector extends PhpInspection {
    private static final String useConstantPattern           = "%s constant should be used instead.";
    private static final String usePhpVersionConstantPattern = "'%s' should be used instead.";

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

    final static private Pattern versionRegex;
    static {
        /* ^(\d)(\.(\d)(\.(\d+))?)?$ */
        versionRegex = Pattern.compile("^(\\d)(\\.(\\d)(\\.(\\d+))?)?$");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "ConstantCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_LANGUAGE_LEVEL_MIGRATION)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && !(reference.getParent() instanceof PhpUse)) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (functions.containsKey(functionName)) {
                        boolean canUseConstant = arguments.length == 0;
                        /* special handling for "php_uname" */
                        if (functionName.equals("php_uname")) {
                            canUseConstant = false;
                            if (arguments.length == 1 && arguments[0] instanceof StringLiteralExpression) {
                                canUseConstant = ((StringLiteralExpression) arguments[0]).getContents().equals("s");
                            }
                        }
                        if (canUseConstant) {
                            final String constant = functions.get(functionName);
                            final String message  = String.format(useConstantPattern, constant);
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
                                        final Matcher versionMatcher = versionRegex.matcher(version);
                                        if (versionMatcher.find()) {
                                            final String minor       = versionMatcher.group(3) == null ? "0" : versionMatcher.group(3);
                                            final String patch       = versionMatcher.group(5) == null ? "0" : versionMatcher.group(5);
                                            final String replacement = String.format("PHP_VERSION_ID %s %s%s%s",
                                                operators.get(operator),
                                                versionMatcher.group(1),
                                                minor.length() == 1 ? '0' + minor: minor,
                                                patch.length() == 1 ? '0' + patch: patch
                                            );
                                            final String message = String.format(usePhpVersionConstantPattern, replacement);
                                            holder.registerProblem(reference, message, new UseConstantFix(replacement));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseConstantFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use the constant instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseConstantFix(@NotNull String expression) {
            super(expression);
        }
    }
}