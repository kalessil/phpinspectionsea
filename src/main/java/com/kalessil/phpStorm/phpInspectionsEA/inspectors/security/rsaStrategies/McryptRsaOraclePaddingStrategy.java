package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.rsaStrategies;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class McryptRsaOraclePaddingStrategy {
    private static final String message = "This call is vulnerable to oracle padding attacks (see our documentation on GitHub for options).";

    static public boolean apply(@NotNull ProblemsHolder holder, @NotNull FunctionReference reference) {
        boolean result = false;
        if (isTargetCall(reference)) {
            final Set<PsiElement> modeVariants = PossibleValuesDiscoveryUtil.discover(reference.getParameters()[3]);
            if (!modeVariants.isEmpty()) {
                for (final PsiElement variant : modeVariants) {
                    if (variant instanceof ConstantReference) {
                        final String constantName = ((ConstantReference) variant).getName();
                        if (constantName != null && constantName.equals("MCRYPT_MODE_CBC")) {
                            holder.registerProblem(reference, message);
                            result = true;
                            break;
                        }
                    }
                }
                modeVariants.clear();
            }
        }
        return result;
    }

    static private boolean isTargetCall(@NotNull FunctionReference reference) {
        boolean result = false;
        final PsiElement[] arguments = reference.getParameters();
        final String functionName    = reference.getName();
        if (arguments.length >= 4 && functionName != null && functionName.equals("mcrypt_encrypt")) {
            if (arguments[0] instanceof ConstantReference) {
                final String constantName = ((ConstantReference) arguments[0]).getName();
                result = constantName != null && constantName.equals("MCRYPT_RIJNDAEL_128");
            }
        }
        return result;
    }
}
