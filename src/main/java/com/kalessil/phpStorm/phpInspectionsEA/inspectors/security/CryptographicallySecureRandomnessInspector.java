package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
/*
 - Cryptographically secure random:
     - openssl_random_pseudo_bytes()
         - use random_bytes() PHP7
         - report missing 2nd argument;
         - report if argument is not verified;
     - mcrypt_create_iv()
         - use random_bytes() PHP7
         - report if MCRYPT_DEV_RANDOM (strong) MCRYPT_DEV_RANDOM (secure) not provided as 2nd argument
         - report using MCRYPT_DEV_RANDOM and side-effects;
 */

public class CryptographicallySecureRandomnessInspector extends BasePhpInspection {
    private static final String messageUseRandomBytes                    = "..."; // weak warning
    private static final String messageVerifyBytes                       = "..."; // error
    private static final String messageOpenssl2ndArgumentNotDefined      = "..."; // error
    private static final String messageOpenssl2ndArgumentNotVerified     = "..."; // error
    private static final String messageMcrypt2ndArgumentNotDefined       = "..."; // error
    private static final String messageMcrypt2ndArgumentStrongNotSecure  = "..."; // error

    @NotNull
    public String getShortName() {
        return "CryptographicallySecureRandomnessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                // holder.registerProblem(reference, messageUseRandomBytes, ProblemHighlightType.GENERIC_ERROR);
            }
        };
    }
}
