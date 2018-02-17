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

    private static final Map<String, String> deprecations = new HashMap<>();
    private static final Map<String, String> removals     = new HashMap<>();
    static {

        /* TODO: http://php.net/manual/en/network.configuration.php */
        /* TODO: http://php.net/manual/en/info.configuration.php */
        /* TODO: http://php.net/manual/en/misc.configuration.php */
        deprecations.put("xsl.security_prefs", "'xsl.security_prefs' is a deprecated option since PHP 5.4.0 (removed in PHP 7.0.0). Use XsltProcessor->setSecurityPrefs() instead.");
        deprecations.put("highlight.bg", "'highlight.bg' is a deprecated option since PHP 5.4.0.");
        deprecations.put("define_syslog_variables", "'define_syslog_variables' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        deprecations.put("magic_quotes_gpc", "'magic_quotes_gpc' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        deprecations.put("magic_quotes_runtime", "'magic_quotes_runtime' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        /* TODO: http://php.net/manual/en/sybase.configuration.php */
        deprecations.put("magic_quotes_sybase", "'magic_quotes_sybase' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        /* TODO: http://php.net/manual/en/ini.sect.safe-mode.php */
        deprecations.put("safe_mode", "'safe_mode' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        deprecations.put("safe_mode_gid", "'safe_mode_gid' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        deprecations.put("safe_mode_include_dir", "'safe_mode_include_dir' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        deprecations.put("safe_mode_exec_dir", "'safe_mode_exec_dir' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        deprecations.put("safe_mode_allowed_env_vars", "'safe_mode_allowed_env_vars' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        deprecations.put("safe_mode_protected_env_vars", "'safe_mode_protected_env_vars' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");

        /* http://php.net/manual/en/ini.core.php */
        deprecations.put("allow_call_time_pass_reference", "'allow_call_time_pass_reference' is a deprecated option since PHP 5.3.0.");
        deprecations.put("register_globals",               "'register_globals' is a deprecated option since PHP 5.3.0.");
        deprecations.put("register_long_arrays",           "'register_long_arrays' is a deprecated option since PHP 5.3.0.");
        deprecations.put("always_populate_raw_post_data",  "'always_populate_raw_post_data' is a deprecated option since PHP 5.6.0.");

        removals.put("sql.safe_mode",                  "'sql.safe_mode' was removed in PHP 7.2.0.");
        removals.put("always_populate_raw_post_data",  "'always_populate_raw_post_data' was removed in PHP 7.0.0.");
        removals.put("asp_tags",                       "'asp_tags' was removed in PHP 7.0.0.");
        removals.put("allow_call_time_pass_reference", "'allow_call_time_pass_reference' was removed in PHP PHP 5.4.0.");
        removals.put("register_globals",               "'register_globals' was removed in PHP 5.4.0.");
        removals.put("register_long_arrays",           "'register_long_arrays' was removed in PHP 5.4.0.");
        removals.put("y2k_compliance",                 "'y2k_compliance' was removed in PHP 5.4.0.");
        removals.put("zend.ze1_compatibility_mode",    "'zend.ze1_compatibility_mode' was removed in PHP 5.3.0.");

        /* http://php.net/manual/en/session.configuration.php */
        removals.put("session.hash_function",           "'session.hash_function' was removed in PHP 7.1.0.");
        removals.put("session.hash_bits_per_character", "'session.hash_bits_per_character' was removed in PHP 7.1.0.");
        removals.put("session.entropy_file",            "'session.entropy_file' was removed in PHP 7.1.0.");
        removals.put("session.entropy_length",          "'session.entropy_length' was removed in PHP 7.1.0.");
        removals.put("session.bug_compat_42",           "'session.bug_compat_42' was removed in PHP 5.4.0.");
        removals.put("session.bug_compat_warn",         "'session.bug_compat_warn' was removed in PHP 5.4.0.");

        /* http://php.net/manual/en/iconv.configuration.php */
        deprecations.put("iconv.input_encoding",    "'iconv.input_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        deprecations.put("iconv.output_encoding",   "'iconv.output_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        deprecations.put("iconv.internal_encoding", "'iconv.internal_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");

        /* http://php.net/manual/en/mbstring.configuration.php */
        deprecations.put("mbstring.func_overload",     "'mbstring.func_overload' is a deprecated option since PHP 7.2.0.");
        deprecations.put("mbstring.http_input",        "'mbstring.http_input' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        deprecations.put("mbstring.http_output",       "'mbstring.http_output' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        deprecations.put("mbstring.internal_encoding", "'mbstring.internal_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");

        removals.put("mbstring.script_encoding", "'mbstring.script_encoding' was removed in PHP 5.4.0. Use 'zend.script_encoding' instead.");
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
                        if (removals.containsKey(option)) {
                            holder.registerProblem(arguments[0], removals.get(option));
                        } else if (deprecations.containsKey(option)) {
                            holder.registerProblem(arguments[0], deprecations.get(option), ProblemHighlightType.LIKE_DEPRECATED);
                        }
                    }
                }
            }
        };
    }
}
