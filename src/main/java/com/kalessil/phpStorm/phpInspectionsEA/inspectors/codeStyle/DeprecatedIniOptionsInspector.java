package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
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

    private static final Map<String, PhpLanguageLevel> deprecations = new HashMap<>();
    private static final Map<String, PhpLanguageLevel> removals     = new HashMap<>();
    private static final Map<String, String> alternatives           = new HashMap<>();
    static {
        /* http://php.net/manual/en/network.configuration.php */
        deprecations.put("define_syslog_variables", PhpLanguageLevel.PHP530);
        removals.put("define_syslog_variables",     PhpLanguageLevel.PHP540);

        /* http://php.net/manual/en/info.configuration.php */
        deprecations.put("magic_quotes_gpc",     PhpLanguageLevel.PHP530);
        deprecations.put("magic_quotes_runtime", PhpLanguageLevel.PHP530);
        removals.put("magic_quotes_gpc",         PhpLanguageLevel.PHP540);
        removals.put("magic_quotes_runtime",     PhpLanguageLevel.PHP540);

        /* http://php.net/manual/en/misc.configuration.php */
        removals.put("highlight.bg", PhpLanguageLevel.PHP540);

        /* http://php.net/manual/en/xsl.configuration.php */
        deprecations.put("xsl.security_prefs", PhpLanguageLevel.PHP540);
        removals.put("xsl.security_prefs",     PhpLanguageLevel.PHP700);
        alternatives.put("xsl.security_prefs", "XsltProcessor->setSecurityPrefs()");

        /* http://php.net/manual/en/ini.sect.safe-mode.php */
        deprecations.put("safe_mode",                    PhpLanguageLevel.PHP530);
        deprecations.put("safe_mode_gid",                PhpLanguageLevel.PHP530);
        deprecations.put("safe_mode_include_dir",        PhpLanguageLevel.PHP530);
        deprecations.put("safe_mode_exec_dir",           PhpLanguageLevel.PHP530);
        deprecations.put("safe_mode_allowed_env_vars",   PhpLanguageLevel.PHP530);
        deprecations.put("safe_mode_protected_env_vars", PhpLanguageLevel.PHP530);
        removals.put("safe_mode",                        PhpLanguageLevel.PHP540);
        removals.put("safe_mode_gid",                    PhpLanguageLevel.PHP540);
        removals.put("safe_mode_include_dir",            PhpLanguageLevel.PHP540);
        removals.put("safe_mode_exec_dir",               PhpLanguageLevel.PHP540);
        removals.put("safe_mode_allowed_env_vars",       PhpLanguageLevel.PHP540);
        removals.put("safe_mode_protected_env_vars",     PhpLanguageLevel.PHP540);

        /* http://php.net/manual/en/ini.core.php */
        deprecations.put("allow_call_time_pass_reference", PhpLanguageLevel.PHP530);
        deprecations.put("register_globals",               PhpLanguageLevel.PHP530);
        deprecations.put("register_long_arrays",           PhpLanguageLevel.PHP530);
        deprecations.put("always_populate_raw_post_data",  PhpLanguageLevel.PHP560);
        removals.put("sql.safe_mode",                      PhpLanguageLevel.PHP720);
        removals.put("always_populate_raw_post_data",      PhpLanguageLevel.PHP700);
        removals.put("asp_tags",                           PhpLanguageLevel.PHP700);
        removals.put("allow_call_time_pass_reference",     PhpLanguageLevel.PHP540);
        removals.put("register_globals",                   PhpLanguageLevel.PHP540);
        removals.put("register_long_arrays",               PhpLanguageLevel.PHP540);
        removals.put("y2k_compliance",                     PhpLanguageLevel.PHP540);
        removals.put("zend.ze1_compatibility_mode",        PhpLanguageLevel.PHP530);

        /* http://php.net/manual/en/session.configuration.php */
        removals.put("session.hash_function",           PhpLanguageLevel.PHP710);
        removals.put("session.hash_bits_per_character", PhpLanguageLevel.PHP710);
        removals.put("session.entropy_file",            PhpLanguageLevel.PHP710);
        removals.put("session.entropy_length",          PhpLanguageLevel.PHP710);
        removals.put("session.bug_compat_42",           PhpLanguageLevel.PHP540);
        removals.put("session.bug_compat_warn",         PhpLanguageLevel.PHP540);

        /* http://php.net/manual/en/iconv.configuration.php */
        deprecations.put("iconv.input_encoding",    PhpLanguageLevel.PHP540);
        deprecations.put("iconv.output_encoding",   PhpLanguageLevel.PHP540);
        deprecations.put("iconv.internal_encoding", PhpLanguageLevel.PHP540);
        alternatives.put("iconv.input_encoding",    "default_charset");
        alternatives.put("iconv.output_encoding",   "default_charset");
        alternatives.put("iconv.internal_encoding", "default_charset");

        /* http://php.net/manual/en/mbstring.configuration.php */
        deprecations.put("mbstring.func_overload",     PhpLanguageLevel.PHP720);
        deprecations.put("mbstring.http_input",        PhpLanguageLevel.PHP560);
        deprecations.put("mbstring.http_output",       PhpLanguageLevel.PHP560);
        deprecations.put("mbstring.internal_encoding", PhpLanguageLevel.PHP560);
        removals.put("mbstring.script_encoding",       PhpLanguageLevel.PHP540);
        alternatives.put("mbstring.func_overload",     "default_charset");
        alternatives.put("mbstring.http_input",        "default_charset");
        alternatives.put("mbstring.http_output",       "default_charset");
        alternatives.put("mbstring.script_encoding",   "zend.script_encoding");

        /* http://php.net/manual/en/sybase.configuration.php */
        deprecations.put("magic_quotes_sybase", PhpLanguageLevel.PHP530);
        removals.put("magic_quotes_sybase",     PhpLanguageLevel.PHP540);
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
