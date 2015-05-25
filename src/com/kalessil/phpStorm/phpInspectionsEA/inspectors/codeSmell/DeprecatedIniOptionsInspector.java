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
    private static final String strProblemDescription = "'%a%' is a deprecated option.";
    private static final String strProblemDescriptionRecommend = "'%a%' is a deprecated option. Use %b% instead.";
    private static final List<String> INI_FUNCS = Arrays.asList(
            "\\ini_set", "\\ini_get", "\\ini_alter", "\\ini_restore"
    );
    private static final Map<String, String> INI_NAMES = new HashMap<String, String>();
    static {
        INI_NAMES.put("iconv.input_encoding", "'default_charset'");
        INI_NAMES.put("iconv.output_encoding", "'default_charset'");
        INI_NAMES.put("iconv.internal_encoding", "'default_charset'");
        INI_NAMES.put("mbstring.http_input", "'default_charset'");
        INI_NAMES.put("mbstring.http_output", "'default_charset'");
        INI_NAMES.put("mbstring.internal_encoding", "'default_charset'");
        INI_NAMES.put("xsl.security_prefs", "XsltProcessor->setSecurityPrefs()");
        INI_NAMES.put("allow_call_time_pass_reference", null);
        INI_NAMES.put("define_syslog_variables", null);
        INI_NAMES.put("highlight.bg", null);
        INI_NAMES.put("magic_quotes_gpc", null);
        INI_NAMES.put("magic_quotes_runtime", null);
        INI_NAMES.put("magic_quotes_sybase", null);
        INI_NAMES.put("register_globals", null);
        INI_NAMES.put("register_long_arrays", null);
        INI_NAMES.put("safe_mode", null);
        INI_NAMES.put("safe_mode_gid", null);
        INI_NAMES.put("safe_mode_include_dir", null);
        INI_NAMES.put("safe_mode_exec_dir", null);
        INI_NAMES.put("safe_mode_allowed_env_vars", null);
        INI_NAMES.put("safe_mode_protected_env_vars", null);
        INI_NAMES.put("zend.ze1_compatibility_mode", null);
        INI_NAMES.put("session.bug_compat_42", null);
        INI_NAMES.put("session.bug_compat_warn", null);
        INI_NAMES.put("y2k_compliance", null);
    }

    @NotNull
    public String getShortName() {
        return "DeprecatedIniOptionsInspector";
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

                final String replace = INI_NAMES.get(value);
                final String strMessage = replace == null ?
                        strProblemDescription.replace("%a%", value) :
                        strProblemDescriptionRecommend.replace("%a%", value).replace("%b%", replace);
                holder.registerProblem(parameters[0], strMessage, ProblemHighlightType.LIKE_DEPRECATED);
            }
        };
    }
}
