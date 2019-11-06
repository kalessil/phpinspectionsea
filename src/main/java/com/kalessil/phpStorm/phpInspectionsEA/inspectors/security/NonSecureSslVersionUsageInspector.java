package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NonSecureSslVersionUsageInspector extends PhpInspection {
    private static final String messageSsl = "This SSL version is a weak one, please consider using newer version instead.";
    private static final String messageTls = "This TLS version is a weak one, please consider using newer version instead.";

    private static final Set<String> sslConstants = new HashSet<>();
    private static final Set<String> tlsConstants = new HashSet<>();
    static {
        /* -> STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT */
        sslConstants.add("STREAM_CRYPTO_METHOD_TLSv1_0_CLIENT");
        sslConstants.add("STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT");
        /* -> STREAM_CRYPTO_PROTO_TLSv1_2 */
        sslConstants.add("STREAM_CRYPTO_PROTO_TLSv1_0");
        sslConstants.add("STREAM_CRYPTO_PROTO_TLSv1_1");
        /* -> CURL_SSLVERSION_MAX_TLSv1_3 */
        sslConstants.add("CURL_SSLVERSION_MAX_TLSv1_0");
        sslConstants.add("CURL_SSLVERSION_MAX_TLSv1_1");
        sslConstants.add("CURL_SSLVERSION_MAX_TLSv1_2");

        /* -> STREAM_CRYPTO_METHOD_SSLv3_CLIENT */
        tlsConstants.add("STREAM_CRYPTO_METHOD_SSLv2_CLIENT");
        tlsConstants.add("STREAM_CRYPTO_METHOD_SSLv23_CLIENT");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "NonSecureSslVersionUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Insecure SSL/TLS version usage";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpConstantReference(@NotNull ConstantReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_SECURITY)) { return; }

                final String constantName = reference.getName();
                if (constantName != null && ! constantName.isEmpty()) {
                    if (sslConstants.contains(constantName)) {
                        holder.registerProblem(reference, messageSsl);
                    } else if (tlsConstants.contains(constantName)) {
                        holder.registerProblem(reference, messageTls);
                    }
                }
            }
        };
    }
}
