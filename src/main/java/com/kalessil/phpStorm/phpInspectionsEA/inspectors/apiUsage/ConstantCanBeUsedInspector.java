package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

public class ConstantCanBeUsedInspector extends BasePhpInspection {
    private static final String useConstantPattern           = "%s constant should be used instead.";
    private static final String usePhpVersionConstantPattern = "Consider using '%s' instead.";
    private static final String useOsFamilyConstantPattern   = "Consider using 'PHP_OS_FAMILY' instead.";

    static private final Map<String, String> functionsToConstantMapping = new HashMap<>();
    static private final Map<String, String> operators                  = new HashMap<>();
    static private final Set<String> functionsForOsFamily               = new HashSet<>();
    private static final Set<String> caseManipulationFunctions          = new HashSet<>();
    static {
        functionsToConstantMapping.put("phpversion",    "PHP_VERSION");
        functionsToConstantMapping.put("php_sapi_name", "PHP_SAPI");
        functionsToConstantMapping.put("get_class",     "__CLASS__");
        functionsToConstantMapping.put("pi",            "M_PI");

        functionsForOsFamily.add("strpos");
        functionsForOsFamily.add("stripos");
        functionsForOsFamily.add("mb_strpos");
        functionsForOsFamily.add("mb_stripos");
        functionsForOsFamily.add("strncasecmp");
        functionsForOsFamily.add("strncmp");
        functionsForOsFamily.add("substr");
        functionsForOsFamily.add("mb_substr");

        caseManipulationFunctions.add("strtolower");
        caseManipulationFunctions.add("mb_strtolower");
        caseManipulationFunctions.add("strtoupper");
        caseManipulationFunctions.add("mb_strtoupper");

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

    @NotNull
    @Override
    public String getDisplayName() {
        return "A constant can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && !(reference.getParent() instanceof PhpUse)) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (functionsToConstantMapping.containsKey(functionName)) {
                        boolean constantApplicable = arguments.length == 0;
                        if (constantApplicable) {
                            final String constant = functionsToConstantMapping.get(functionName);
                            holder.registerProblem(
                                    reference,
                                    String.format(MessagesPresentationUtil.prefixWithEa(useConstantPattern), constant),
                                    new UseConstantFix(constant)
                            );
                        }
                    } else if (arguments.length == 3 && functionName.equals("version_compare")) {
                        if (arguments[0] instanceof ConstantReference && arguments[1] instanceof StringLiteralExpression) {
                            final String constant = ((ConstantReference) arguments[0]).getName();
                            if (constant != null && constant.equals("PHP_VERSION")) {
                                final String version = ((StringLiteralExpression) arguments[1]).getContents();
                                if (!version.isEmpty() && arguments[2] instanceof StringLiteralExpression) {
                                    final String operator = ((StringLiteralExpression) arguments[2]).getContents();
                                    if (operators.containsKey(operator)) {
                                        final Matcher versionMatcher = versionRegex.matcher(version);
                                        if (versionMatcher.find()) {
                                            final String minor       = versionMatcher.group(3) == null ? "0" : versionMatcher.group(3);
                                            final String patch       = versionMatcher.group(5) == null ? "0" : versionMatcher.group(5);
                                            final String replacement = String.format("PHP_VERSION_ID %s %s%s%s",
                                                operators.get(operator),
                                                versionMatcher.group(1),
                                                minor.length() == 1 ? '0' + minor : minor,
                                                patch.length() == 1 ? '0' + patch : patch
                                            );
                                            holder.registerProblem(
                                                    reference,
                                                    String.format(MessagesPresentationUtil.prefixWithEa(usePhpVersionConstantPattern), replacement),
                                                    new UseConstantFix(replacement)
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void visitPhpConstantReference(@NotNull ConstantReference reference) {
                if (PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP720)) {
                    final String name = reference.getName();
                    if (name != null && name.equals("PHP_OS")) {
                        final PsiElement parent = reference.getParent();
                        PsiElement context      = parent instanceof ParameterList ? parent.getParent() : parent;
                        if (OpenapiTypesUtil.isFunctionReference(context)) {
                            final FunctionReference call = (FunctionReference) context;
                            final String functionName    = call.getName();
                            if (functionName != null && functionsForOsFamily.contains(functionName)) {
                                /* substring call needs context re-specification */
                                if (functionName.equals("substr") || functionName.equals("mb_substr")) {
                                    final PsiElement substringParent  = call.getParent();
                                    final PsiElement substringContext = substringParent instanceof ParameterList ? substringParent.getParent() : substringParent;
                                    if (OpenapiTypesUtil.isFunctionReference(substringContext)) {
                                        final String outerFunctionName = ((FunctionReference) substringContext).getName();
                                        if (outerFunctionName != null && caseManipulationFunctions.contains(outerFunctionName)) {
                                            context = substringContext;
                                        }
                                    }
                                }
                                /* now we have clear context, where we do expect comparison to false, number or string */
                                final PsiElement binaryCandidate = context.getParent();
                                if (binaryCandidate instanceof BinaryExpression) {
                                    final BinaryExpression binary = (BinaryExpression) binaryCandidate;
                                    if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(binary.getOperationType())) {
                                        final PsiElement value = OpenapiElementsUtil.getSecondOperand(binary, context);
                                        if (value != null) {
                                            final boolean suggest;
                                            if (functionName.equals("substr") || functionName.equals("mb_substr")) {
                                                suggest = value instanceof StringLiteralExpression;
                                            } else {
                                                suggest = OpenapiTypesUtil.isNumber(value) || PhpLanguageUtil.isFalse(value);
                                            }
                                            if (suggest) {
                                                holder.registerProblem(
                                                        context,
                                                        MessagesPresentationUtil.prefixWithEa(useOsFamilyConstantPattern)
                                                );
                                            }
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
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseConstantFix(@NotNull String expression) {
            super(expression);
        }
    }
}