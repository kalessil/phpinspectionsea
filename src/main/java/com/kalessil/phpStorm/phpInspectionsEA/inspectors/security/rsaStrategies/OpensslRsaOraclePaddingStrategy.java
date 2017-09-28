package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.rsaStrategies;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class OpensslRsaOraclePaddingStrategy {
    private static final String message = "This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.";

    private static final Set<String> functions = new HashSet<>();
    static {
        /* openssl_private_encrypt and openssl_public_decrypt are out of the scope */
        functions.add("openssl_public_encrypt");
        functions.add("openssl_private_decrypt");
    }

    static public void apply(@NotNull ProblemsHolder holder, @NotNull FunctionReference reference) {
        final PsiElement[] arguments = reference.getParameters();
        if (arguments.length == 3 && isTargetCall(reference)) {
            holder.registerProblem(reference, message);
        } else if (arguments.length == 4 && isTargetCall(reference)) {
            final Set<PsiElement> modeVariants = PossibleValuesDiscoveryUtil.discover(arguments[3]);
            if (!modeVariants.isEmpty()) {
                for (final PsiElement variant : modeVariants) {
                    if (variant instanceof ConstantReference) {
                        final String constantName = ((ConstantReference) variant).getName();
                        if (constantName != null && constantName.equals("OPENSSL_PKCS1_PADDING")) {
                            holder.registerProblem(reference, message);
                            break;
                        }
                    }
                }
                modeVariants.clear();
            }
        }
    }

    static private boolean isTargetCall(@NotNull FunctionReference reference) {
        boolean result = false;
        if (reference.getNameNode() == null) {
            final PsiElement name = reference.getFirstPsiChild();
            if (name != null) {
                final Set<PsiElement> nameVariants = PossibleValuesDiscoveryUtil.discover(name);
                if (!nameVariants.isEmpty()) {
                    for (final PsiElement variant : nameVariants) {
                        if (variant instanceof StringLiteralExpression) {
                            final String content = ((StringLiteralExpression) variant).getContents();
                            if (functions.contains(StringUtils.strip(content, "\\"))) {
                                result = true;
                                break;
                            }
                        }
                    }
                    nameVariants.clear();
                }
            }
        } else {
            result = functions.contains(reference.getName());
        }
        return result;
    }
}
