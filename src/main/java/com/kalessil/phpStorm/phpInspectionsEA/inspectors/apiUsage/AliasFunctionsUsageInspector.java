package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
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
    private static final String messagePattern = "'%a%(...)' is an alias function. Use '%f%(...)' instead.";

    @NotNull
    public String getShortName() {
        return "AliasFunctionsUsageInspection";
    }

    private static final Map<String, String> mapping = new HashMap<>();
    static {
        mapping.put("is_double",              "is_float");
        mapping.put("is_integer",             "is_int");
        mapping.put("is_long",                "is_int");
        mapping.put("is_real",                "is_float");
        mapping.put("sizeof",                 "count");
        mapping.put("doubleval",              "floatval");
        mapping.put("fputs",                  "fwrite");
        mapping.put("join",                   "implode");
        mapping.put("key_exists",             "array_key_exists");
        mapping.put("chop",                   "rtrim");
        mapping.put("ini_alter",              "ini_set");
        mapping.put("is_writeable",           "is_writable");
        mapping.put("magic_quotes_runtime",   "set_magic_quotes_runtime");
        mapping.put("pos",                    "current");
        mapping.put("show_source",            "highlight_file");
        mapping.put("strchr",                 "strstr");
        mapping.put("set_file_buffer",        "stream_set_write_buffer");
        mapping.put("session_commit",         "session_write_close");
        mapping.put("mysqli_escape_string",   "mysqli_real_escape_string");
        mapping.put("ocifreecursor",          "oci_free_statement");
        mapping.put("recode",                 "recode_string");
        mapping.put("imap_listmailbox",       "imap_list");
        mapping.put("socket_getopt",          "socket_get_option");
        mapping.put("socket_setopt",          "socket_set_option");
        mapping.put("openssl_get_privatekey", "openssl_pkey_get_private");
        mapping.put("posix_errno",            "posix_get_last_error");
        mapping.put("ldap_close",             "ldap_unbind");
        mapping.put("pcntl_errno",            "pcntl_get_last_error");
        mapping.put("ftp_quit",               "ftp_close");
        mapping.put("odbc_do",                "odbc_exec");

        /* also aliases, but original naming is so ugly, that I'm not going to promote it */
        //mapping.put("close", "closedir");
        //mapping.put("rewind", "rewinddir");
        //mapping.put("odbc_field_precision", "odbc_field_len");
        //mapping.put("mysqli_execute", "mysqli_stmt_execute");
        //mapping.put("imap_header", "imap_headerinfo");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (null != functionName && mapping.containsKey(functionName)) {
                    /* avoid complaining to imported functions */
                    PsiElement function = OpenapiResolveUtil.resolveReference(reference);
                    if (function == null) {
                        /* handle multiply resolved functions - we need one unique FQN */
                        final Map<String, Function> resolvedFunctions = new HashMap<>();
                        for (final ResolveResult resolve : reference.multiResolve(true)) {
                            final PsiElement resolved = resolve.getElement();
                            if (resolved instanceof Function) {
                                resolvedFunctions.put(((Function) resolved).getFQN(), (Function) resolved);
                            }
                        }
                        if (1 == resolvedFunctions.size()) {
                            function = resolvedFunctions.values().iterator().next();
                        }
                        resolvedFunctions.clear();
                    }
                    if (function instanceof Function && ((Function) function).getFQN().equals('\\' + functionName)) {
                        final String suggestedName = mapping.get(functionName);
                        final String message       = messagePattern
                                .replace("%a%", functionName)
                                .replace("%f%", suggestedName);
                        holder.registerProblem(reference, message, ProblemHighlightType.LIKE_DEPRECATED, new TheLocalFix(suggestedName));
                    }
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private String suggestedName;

        TheLocalFix(@NotNull String suggestedName) {
            super();
            this.suggestedName = suggestedName;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use origin function";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference) {
                ((FunctionReference) expression).handleElementRename(this.suggestedName);
            }
        }
    }
}
