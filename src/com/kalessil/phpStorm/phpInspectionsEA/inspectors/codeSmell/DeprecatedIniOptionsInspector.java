package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeprecatedIniOptionsInspector extends BasePhpInspection {
    private static final List<String> INI_FUNCTIONS = Arrays.asList(
            "ini_set", "ini_get", "ini_alter", "ini_restore"
    );
    private static final Map<String, String> INI_OPTIONS = new HashMap<String, String>();
    static {
        INI_OPTIONS.put("asp_tags", "'asp_tags' is a deprecated option since PHP 7.0.0.");
        INI_OPTIONS.put("always_populate_raw_post_data", "'always_populate_raw_post_data' is a deprecated option since PHP 7.0.0.");

        INI_OPTIONS.put("iconv.input_encoding", "'iconv.input_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        INI_OPTIONS.put("iconv.output_encoding", "'iconv.output_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        INI_OPTIONS.put("iconv.internal_encoding", "'iconv.internal_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        INI_OPTIONS.put("mbstring.http_input", "'mbstring.http_input' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        INI_OPTIONS.put("mbstring.http_output", "'mbstring.http_output' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        INI_OPTIONS.put("mbstring.internal_encoding", "'mbstring.internal_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");

        INI_OPTIONS.put("xsl.security_prefs", "'xsl.security_prefs' is a deprecated option since PHP 5.4.0 (removed in PHP 7.0.0). Use XsltProcessor->setSecurityPrefs() instead.");

        INI_OPTIONS.put("allow_call_time_pass_reference", "'allow_call_time_pass_reference' is a deprecated option since PHP 5.4.0.");
        INI_OPTIONS.put("highlight.bg", "'highlight.bg' is a deprecated option since PHP 5.4.0.");
        INI_OPTIONS.put("zend.ze1_compatibility_mode", "'zend.ze1_compatibility_mode' is a deprecated option since PHP 5.4.0.");
        INI_OPTIONS.put("session.bug_compat_42", "'session.bug_compat_42' is a deprecated option since PHP 5.4.0.");
        INI_OPTIONS.put("session.bug_compat_warn", "'session.bug_compat_warn' is a deprecated option since PHP 5.4.0.");
        INI_OPTIONS.put("y2k_compliance", "'y2k_compliance' is a deprecated option since PHP 5.4.0.");

        INI_OPTIONS.put("define_syslog_variables", "'define_syslog_variables' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_OPTIONS.put("magic_quotes_gpc", "'magic_quotes_gpc' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_OPTIONS.put("magic_quotes_runtime", "'magic_quotes_runtime' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_OPTIONS.put("magic_quotes_sybase", "'magic_quotes_sybase' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_OPTIONS.put("register_globals", "'register_globals' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_OPTIONS.put("register_long_arrays", "'register_long_arrays' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_OPTIONS.put("safe_mode", "'safe_mode' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_OPTIONS.put("safe_mode_gid", "'safe_mode_gid' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_OPTIONS.put("safe_mode_include_dir", "'safe_mode_include_dir' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_OPTIONS.put("safe_mode_exec_dir", "'safe_mode_exec_dir' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_OPTIONS.put("safe_mode_allowed_env_vars", "'safe_mode_allowed_env_vars' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_OPTIONS.put("safe_mode_protected_env_vars", "'safe_mode_protected_env_vars' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
    }

    @NotNull
    public String getShortName() {
        return "DeprecatedIniOptionsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(final FunctionReference reference) {
                String strFunctionName = reference.getName();
                if (StringUtil.isEmpty(strFunctionName) || !INI_FUNCTIONS.contains(strFunctionName)) {
                    return;
                }

                PsiElement[] parameters = reference.getParameters();
                if (parameters.length == 0 || !(parameters[0] instanceof StringLiteralExpression)) {
                    return;
                }

                final String optionName = ((StringLiteralExpression) parameters[0]).getContents();
                if (StringUtil.isEmpty(optionName) || !INI_OPTIONS.containsKey(optionName)) {
                    return;
                }

                String strError = INI_OPTIONS.get(optionName);
                holder.registerProblem(parameters[0], strError, ProblemHighlightType.LIKE_DEPRECATED);
            }
        };
    }
}
