package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class EncryptionInitializationVectorRandomnessInspector extends BasePhpInspection {
    private static final String messagePattern = "%f%() should be used for IV, but found: %e%.";

    private static final Set<String> secureFunctions = new HashSet<>();
    static {
        secureFunctions.add("random_bytes");
        secureFunctions.add("openssl_random_pseudo_bytes");
        secureFunctions.add("mcrypt_create_iv");
    }

    @NotNull
    public String getShortName() {
        return "EncryptionInitializationVectorRandomnessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                /* variable functions are not supported, as we are checking 2 different extensions functions */
                if (functionName != null && (functionName.equals("openssl_encrypt") || functionName.equals("mcrypt_encrypt"))) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length != 5 || arguments[4] == null || arguments[4].getText().isEmpty()) {
                        return;
                    }

                    /* discover and inspect possible values */
                    final Set<PsiElement> values = PossibleValuesDiscoveryUtil.discover(arguments[4]);
                    if (values.size() > 0) {
                        final List<String> reporting = new ArrayList<>();

                        /* check all possible values */
                        for (final PsiElement source : values) {
                            if (OpenapiTypesUtil.isFunctionReference(source)) {
                                final String sourceName = ((FunctionReference) source).getName();
                                if (null != sourceName && secureFunctions.contains(sourceName)) {
                                    continue;
                                }
                            }
                            reporting.add(source.getText());
                        }
                        values.clear();

                        /* got something for reporting */
                        if (reporting.size() > 0) {
                            /* sort reporting list to produce testable results */
                            Collections.sort(reporting);

                            /* report now */
                            final String ivFunction = functionName.startsWith("openssl_") ? "openssl_random_pseudo_bytes" : "mcrypt_create_iv";
                            final String message    = messagePattern
                                    .replace("%e%", String.join(", ", reporting))
                                    .replace("%f%", ivFunction);
                            holder.registerProblem(arguments[4], message, ProblemHighlightType.GENERIC_ERROR);

                            reporting.clear();
                        }
                    }
                }
            }
        };
    }
}
