package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class DeprecatedIniOptionsInspector extends BasePhpInspection {
    private static final List<String> targetFunctions = new ArrayList<>();
    static {
        targetFunctions.add("ini_set");
        targetFunctions.add("ini_get");
        targetFunctions.add("ini_alter");
        targetFunctions.add("ini_restore");
    }

    private static final Map<String, String> targetOptions = new HashMap<>();
    static {
        // -> http://php.net/manual/en/migration72.incompatible.php#migration72.incompatible.sqlsafe_mode-ini-setting
        // -> http://php.net/manual/en/migration72.deprecated.php#migration72.deprecated.track_errors-and-php_errormsg
        // -> http://php.net/manual/en/migration72.deprecated.php#migration72.deprecated.mbstringfunc_overload-ini-setting
        // -> http://php.net/manual/en/migration71.incompatible.php#migration71.incompatible.removed-ini-directives
        // -> http://php.net/manual/en/migration70.incompatible.php#migration70.incompatible.removed-ini-directives
        // -> http://php.net/manual/en/migration56.deprecated.php#migration56.deprecated.iconv-mbstring-encoding
        // -> http://php.net/manual/en/migration54.ini.php#migration54.ini

        targetOptions.put("asp_tags", "'asp_tags' is a deprecated option since PHP 7.0.0.");
        targetOptions.put("always_populate_raw_post_data", "'always_populate_raw_post_data' is a deprecated option since PHP 7.0.0.");
        targetOptions.put("iconv.input_encoding", "'iconv.input_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        targetOptions.put("iconv.output_encoding", "'iconv.output_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        targetOptions.put("iconv.internal_encoding", "'iconv.internal_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        targetOptions.put("mbstring.http_input", "'mbstring.http_input' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        targetOptions.put("mbstring.http_output", "'mbstring.http_output' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        targetOptions.put("mbstring.internal_encoding", "'mbstring.internal_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        targetOptions.put("xsl.security_prefs", "'xsl.security_prefs' is a deprecated option since PHP 5.4.0 (removed in PHP 7.0.0). Use XsltProcessor->setSecurityPrefs() instead.");
        targetOptions.put("allow_call_time_pass_reference", "'allow_call_time_pass_reference' is a deprecated option since PHP 5.4.0.");
        targetOptions.put("highlight.bg", "'highlight.bg' is a deprecated option since PHP 5.4.0.");
        targetOptions.put("zend.ze1_compatibility_mode", "'zend.ze1_compatibility_mode' is a deprecated option since PHP 5.4.0.");
        targetOptions.put("session.bug_compat_42", "'session.bug_compat_42' is a deprecated option since PHP 5.4.0.");
        targetOptions.put("session.bug_compat_warn", "'session.bug_compat_warn' is a deprecated option since PHP 5.4.0.");
        targetOptions.put("y2k_compliance", "'y2k_compliance' is a deprecated option since PHP 5.4.0.");
        targetOptions.put("define_syslog_variables", "'define_syslog_variables' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        targetOptions.put("magic_quotes_gpc", "'magic_quotes_gpc' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        targetOptions.put("magic_quotes_runtime", "'magic_quotes_runtime' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        targetOptions.put("magic_quotes_sybase", "'magic_quotes_sybase' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        targetOptions.put("register_globals", "'register_globals' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        targetOptions.put("register_long_arrays", "'register_long_arrays' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        targetOptions.put("safe_mode", "'safe_mode' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        targetOptions.put("safe_mode_gid", "'safe_mode_gid' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        targetOptions.put("safe_mode_include_dir", "'safe_mode_include_dir' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        targetOptions.put("safe_mode_exec_dir", "'safe_mode_exec_dir' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        targetOptions.put("safe_mode_allowed_env_vars", "'safe_mode_allowed_env_vars' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        targetOptions.put("safe_mode_protected_env_vars", "'safe_mode_protected_env_vars' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
    }

    @NotNull
    public String getShortName() {
        return "DeprecatedIniOptionsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull final FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && targetFunctions.contains(functionName)) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > 0 && arguments[0] instanceof StringLiteralExpression) {
                        final String option = ((StringLiteralExpression) arguments[0]).getContents();
                        if (targetOptions.containsKey(option)) {
                            holder.registerProblem(arguments[0], targetOptions.get(option), ProblemHighlightType.LIKE_DEPRECATED);
                        }
                    }
                }
            }
        };
    }
}
