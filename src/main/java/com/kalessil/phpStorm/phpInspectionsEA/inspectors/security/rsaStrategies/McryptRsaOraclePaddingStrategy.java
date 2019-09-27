package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.rsaStrategies;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
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
                /* MCRYPT_MODE_CBC === 'cbc' */
                result = modeVariants.stream().filter(variant -> variant instanceof StringLiteralExpression).anyMatch(variant -> ((StringLiteralExpression) variant).getContents().equals("cbc"));
                if (result) {
                    holder.registerProblem(reference, message);
                }
                modeVariants.clear();
            }
        }
        return result;
    }

    static private boolean isTargetCall(@NotNull FunctionReference reference) {
        boolean result            = false;
        final String functionName = reference.getName();
        if (functionName != null && functionName.equals("mcrypt_encrypt")) {
            final PsiElement[] arguments = reference.getParameters();
            if (arguments.length >= 4) {
                result = arguments[0] instanceof ConstantReference && "MCRYPT_RIJNDAEL_128".equals(((ConstantReference) arguments[0]).getName());
            }
        }
        return result;
    }
}
