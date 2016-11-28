package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
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
         - report missing 2nd argument;
         - report if argument is not verified;
     - mcrypt_create_iv()
         - report if MCRYPT_DEV_RANDOM (strong) MCRYPT_DEV_RANDOM (secure) not provided as 2nd argument
         - report using MCRYPT_DEV_RANDOM and side-effects;
 */

public class CryptographicallySecureRandomnessInspector extends BasePhpInspection {
    private static final String messageUseRandomBytes                    = "Consider using cryptographically secure random_bytes() instead";
    private static final String messageOpenssl2ndArgumentNotDefined      = "Use 2nd parameter for determining if the algorithm used was cryptographically strong";
    private static final String messageMcrypt2ndArgumentNotDefined       = "Please provide 2nd parameter implicitly as default value has changed between PHP versions";

    private static final String messageVerifyBytes                       = "..."; // error
    private static final String messageOpenssl2ndArgumentNotVerified     = "..."; // error
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
                /* genera and function name requirements */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if ((1 != params.length && 2 != params.length) || StringUtil.isEmpty(functionName)) {
                    return;
                }
                if (!functionName.equals("openssl_random_pseudo_bytes") && !functionName.equals("mcrypt_create_iv")) {
                    return;
                }
                final boolean isOpenSSL = functionName.equals("openssl_random_pseudo_bytes");

                /* Case 1: use random_bytes in PHP7 */
                PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) { // PHP7 and newer
                    holder.registerProblem(reference, messageUseRandomBytes, ProblemHighlightType.WEAK_WARNING);
                }

                /* Case 2: report missing 2nd argument */
                final boolean hasSecondArgument = 2 == params.length;
                if (!hasSecondArgument) {
                    final String message = isOpenSSL ? messageOpenssl2ndArgumentNotDefined : messageMcrypt2ndArgumentNotDefined;
                    holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);
                }

                /* Case 3: unchecked generation result */

                /* Case 4: is 2nd argument verified/strong enough */
            }
        };
    }
}
