package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.deprecations;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
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

public class AliasFunctionsUsageInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s(...)' is an alias function, consider using '%s(...)' instead.";

    @NotNull
    public String getShortName() {
        return "AliasFunctionsUsageInspection";
    }

    private static final Map<String, String> relevantAliases   = new HashMap<>();
    private static final Map<String, String> deprecatedAliases = new HashMap<>();
    static {
        /* See also: http://php.net/manual/en/aliases.php */
        relevantAliases.put("close",                  "closedir");
        relevantAliases.put("is_double",              "is_float");
        relevantAliases.put("is_integer",             "is_int");
        relevantAliases.put("is_long",                "is_int");
        relevantAliases.put("is_real",                "is_float");
        relevantAliases.put("sizeof",                 "count");
        relevantAliases.put("doubleval",              "floatval");
        relevantAliases.put("fputs",                  "fwrite");
        relevantAliases.put("join",                   "implode");
        relevantAliases.put("key_exists",             "array_key_exists");
        relevantAliases.put("chop",                   "rtrim");
        relevantAliases.put("ini_alter",              "ini_set");
        relevantAliases.put("is_writeable",           "is_writable");
        relevantAliases.put("pos",                    "current");
        relevantAliases.put("show_source",            "highlight_file");
        relevantAliases.put("strchr",                 "strstr");
        relevantAliases.put("set_file_buffer",        "stream_set_write_buffer");
        relevantAliases.put("session_commit",         "session_write_close");
        relevantAliases.put("socket_getopt",          "socket_get_option");
        relevantAliases.put("socket_setopt",          "socket_set_option");
        relevantAliases.put("openssl_get_privatekey", "openssl_pkey_get_private");
        relevantAliases.put("posix_errno",            "posix_get_last_error");
        relevantAliases.put("ldap_close",             "ldap_unbind");
        relevantAliases.put("pcntl_errno",            "pcntl_get_last_error");
        relevantAliases.put("ftp_quit",               "ftp_close");
        relevantAliases.put("socket_set_blocking",    "stream_set_blocking");
        relevantAliases.put("stream_register_wrapper","stream_wrapper_register");
        relevantAliases.put("socket_set_timeout",     "stream_set_timeout");
        relevantAliases.put("socket_get_status",      "stream_get_meta_data");
        relevantAliases.put("diskfreespace",          "disk_free_space");
        relevantAliases.put("odbc_do",                "odbc_exec");
        relevantAliases.put("odbc_field_precision",   "odbc_field_len");
        relevantAliases.put("recode",                 "recode_string");
        relevantAliases.put("mysqli_escape_string",   "mysqli_real_escape_string");
        relevantAliases.put("mysqli_execute",         "mysqli_stmt_execute");
        /* aliases covered by other inspections: rand -> mt_rand, srand -> mt_srand */

        /* aliases affected by backward-incompatible changes */
        deprecatedAliases.put("mysqli_bind_param",      "This alias has been DEPRECATED as of PHP 5.3.0 and REMOVED as of PHP 5.4.0.");
        deprecatedAliases.put("mysqli_bind_result",     "This alias has been DEPRECATED as of PHP 5.3.0 and REMOVED as of PHP 5.4.0.");
        deprecatedAliases.put("mysqli_client_encoding", "This alias has been DEPRECATED as of PHP 5.3.0 and REMOVED as of PHP 5.4.0.");
        deprecatedAliases.put("mysqli_fetch",           "This alias has been DEPRECATED as of PHP 5.3.0 and REMOVED as of PHP 5.4.0.");
        deprecatedAliases.put("mysqli_param_count",     "This alias has been DEPRECATED as of PHP 5.3.0 and REMOVED as of PHP 5.4.0.");
        deprecatedAliases.put("mysqli_get_metadata",    "This alias has been DEPRECATED as of PHP 5.3.0 and REMOVED as of PHP 5.4.0.");
        deprecatedAliases.put("mysqli_send_long_data",  "This alias has been DEPRECATED as of PHP 5.3.0 and REMOVED as of PHP 5.4.0.");
        deprecatedAliases.put("ocifreecursor",          "This alias has been DEPRECATED as of PHP 5.4.0. Relying on this alias is highly discouraged.");
        deprecatedAliases.put("magic_quotes_runtime",   "This alias has been DEPRECATED as of PHP 5.3.0 and REMOVED as of PHP 7.0.0.");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null) {
                    if (relevantAliases.containsKey(functionName) && this.isFromRootNamespace(reference)) {
                        final PsiElement target = NamedElementUtil.getNameIdentifier(reference);
                        if (target != null) {
                            final String original = relevantAliases.get(functionName);
                            holder.registerProblem(
                                    target,
                                    String.format(messagePattern, functionName, original),
                                    ProblemHighlightType.LIKE_DEPRECATED,
                                    new TheLocalFix(original)
                            );
                        }
                    } else if (deprecatedAliases.containsKey(functionName) && this.isFromRootNamespace(reference)) {
                        final PsiElement target = NamedElementUtil.getNameIdentifier(reference);
                        if (target != null) {
                            holder.registerProblem(target, deprecatedAliases.get(functionName));
                        }
                    }
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use origin function";

        final private String suggestedName;

        TheLocalFix(@NotNull String suggestedName) {
            super();
            this.suggestedName = suggestedName;
        }

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement().getParent();
            if (expression instanceof FunctionReference && !project.isDisposed()) {
                ((FunctionReference) expression).handleElementRename(this.suggestedName);
            }
        }
    }
}
