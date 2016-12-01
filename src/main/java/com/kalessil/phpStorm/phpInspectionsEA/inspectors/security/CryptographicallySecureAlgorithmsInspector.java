package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class CryptographicallySecureAlgorithmsInspector extends BasePhpInspection {

    final private static Map<String, String> constants = new HashMap<>();
    static {
        constants.put("MCRYPT_RIJNDAEL_192", "mcrypt's MCRYPT_RIJNDAEL_192 is not AES compliant, MCRYPT_RIJNDAEL_128 should be used instead");
        constants.put("MCRYPT_RIJNDAEL_256", "mcrypt's MCRYPT_RIJNDAEL_256 is not AES compliant, MCRYPT_RIJNDAEL_128 + 256-bit key should be used instead");
    }

    @NotNull
    public String getShortName() {
        return "CryptographicallySecureAlgorithm";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpConstantReference(ConstantReference reference) {
                final String name = reference.getName();
                if (!StringUtil.isEmpty(name) && constants.containsKey(name)) {
                    holder.registerProblem(reference, constants.get(name), ProblemHighlightType.GENERIC_ERROR);
                }
            }
        };
    }
}
