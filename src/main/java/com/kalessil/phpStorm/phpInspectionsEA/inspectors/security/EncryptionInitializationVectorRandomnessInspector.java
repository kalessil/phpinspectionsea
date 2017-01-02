package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

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

    @NotNull
    private static final HashSet<String> secureFunctions = new HashSet<>();
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
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* verify general requirements to the call */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (5 != params.length || null == params[4] || 0 == params[4].getTextLength() || StringUtil.isEmpty(functionName)) {
                    return;
                }

                /* variable functions are not supported, as we are checking 2 different extensions functions */
                if (functionName.equals("openssl_encrypt") || functionName.equals("mcrypt_encrypt")) {
                    /* discover and inspect possible values */
                    final HashSet<PsiElement> processed = new HashSet<>();
                    final HashSet<PsiElement> values    = PossibleValuesDiscoveryUtil.discover(params[4], processed);
                    if (values.size() > 0) {
                        List<String> reporting = new LinkedList<>();

                        /* check all possible values */
                        for (PsiElement source : values) {
                            if (source instanceof FunctionReferenceImpl) {
                                final String sourceName = ((FunctionReferenceImpl) source).getName();
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
                            holder.registerProblem(params[4], message, ProblemHighlightType.GENERIC_ERROR);

                            reporting.clear();
                        }
                    }
                    processed.clear();
                }
            }
        };
    }
}
