package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.rsaStrategies;

import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import org.jetbrains.annotations.NotNull;

final public class McryptRsaOraclePaddingStrategy {
    private static final String message = "This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.";

    static public boolean apply(@NotNull ProblemsHolder holder, @NotNull FunctionReference reference) {
        return true;
    }
}
