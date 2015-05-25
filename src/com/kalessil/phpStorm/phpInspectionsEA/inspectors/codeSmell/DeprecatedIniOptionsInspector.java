package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeprecatedIniOptionsInspector extends BasePhpInspection {
    private static final List<String> INI_FUNCS = Arrays.asList(
            "\\ini_set", "\\ini_get", "\\ini_alter", "\\ini_restore"
    );
    private static final Map<String, String> INI_NAMES = new HashMap<String, String>();
    static {
        INI_NAMES.put("iconv.input_encoding", "'iconv.input_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        INI_NAMES.put("iconv.output_encoding", "'iconv.output_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        INI_NAMES.put("iconv.internal_encoding", "'iconv.internal_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        INI_NAMES.put("mbstring.http_input", "'mbstring.http_input' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        INI_NAMES.put("mbstring.http_output", "'mbstring.http_output' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");
        INI_NAMES.put("mbstring.internal_encoding", "'mbstring.internal_encoding' is a deprecated option since PHP 5.6.0. Use 'default_charset' instead.");

        INI_NAMES.put("xsl.security_prefs", "'xsl.security_prefs' is a deprecated option since PHP 5.4.0. Use XsltProcessor->setSecurityPrefs() instead.");

        INI_NAMES.put("allow_call_time_pass_reference", "'allow_call_time_pass_reference' is a discontinued option since PHP 5.4.0.");
        INI_NAMES.put("highlight.bg", "'highlight.bg' is a discontinued option since PHP 5.4.0.");
        INI_NAMES.put("zend.ze1_compatibility_mode", "'zend.ze1_compatibility_mode' is a discontinued option since PHP 5.4.0.");
        INI_NAMES.put("session.bug_compat_42", "'session.bug_compat_42' is a discontinued option since PHP 5.4.0.");
        INI_NAMES.put("session.bug_compat_warn", "'session.bug_compat_warn' is a discontinued option since PHP 5.4.0.");
        INI_NAMES.put("y2k_compliance", "'y2k_compliance' is a discontinued option since PHP 5.4.0.");

        INI_NAMES.put("define_syslog_variables", "'define_syslog_variables' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_NAMES.put("magic_quotes_gpc", "'magic_quotes_gpc' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_NAMES.put("magic_quotes_runtime", "'magic_quotes_runtime' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_NAMES.put("magic_quotes_sybase", "'magic_quotes_sybase' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_NAMES.put("register_globals", "'register_globals' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_NAMES.put("register_long_arrays", "'register_long_arrays' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_NAMES.put("safe_mode", "'safe_mode' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_NAMES.put("safe_mode_gid", "'safe_mode_gid' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_NAMES.put("safe_mode_include_dir", "'safe_mode_include_dir' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_NAMES.put("safe_mode_exec_dir", "'safe_mode_exec_dir' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_NAMES.put("safe_mode_allowed_env_vars", "'safe_mode_allowed_env_vars' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
        INI_NAMES.put("safe_mode_protected_env_vars", "'safe_mode_protected_env_vars' is a deprecated option since PHP 5.3.0 (removed in PHP 5.4.0).");
    }

    @NotNull
    public String getShortName() {
        return "DeprecatedIniOptionsInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(final FunctionReference reference) {
                if (!INI_FUNCS.contains(reference.getFQN())) {
                    return;
                }

                ParameterList parameterList = reference.getParameterList();
                if (parameterList == null) {
                    return;
                }
                PsiElement[] parameters = parameterList.getParameters();
                if ((parameters.length == 0) || !(parameters[0] instanceof StringLiteralExpression)) {
                    return;
                }


                final String value = ((StringLiteralExpression) parameters[0]).getContents();
                if (!INI_NAMES.containsKey(value)) {
                    return;
                }

                holder.registerProblem(parameters[0], INI_NAMES.get(value), ProblemHighlightType.LIKE_DEPRECATED);
            }
        };
    }
}
