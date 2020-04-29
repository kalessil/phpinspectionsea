package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
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
        /* notes for future me, to not check things twice */
        // OPENSSL_ALGO_SHA1, OPENSSL_ALGO_MD5, OPENSSL_ALGO_MD4, OPENSSL_ALGO_MD2 -> has no Blowfish replacement

        /* known bugs */
        constants.put("MCRYPT_RIJNDAEL_192",    "mcrypt's MCRYPT_RIJNDAEL_192 is not AES compliant, MCRYPT_RIJNDAEL_128 should be used instead.");
        constants.put("MCRYPT_RIJNDAEL_256",    "mcrypt's MCRYPT_RIJNDAEL_256 is not AES compliant, MCRYPT_RIJNDAEL_128 + 256-bit key should be used instead.");
        /* weak algorithms, mcrypt constants */
        constants.put("MCRYPT_3DES",            "3DES has known vulnerabilities, consider using MCRYPT_RIJNDAEL_128 instead.");
        constants.put("MCRYPT_TRIPLEDES",       "3DES has known vulnerabilities, consider using MCRYPT_RIJNDAEL_128 instead.");
        constants.put("MCRYPT_DES_COMPAT",      "DES has known vulnerabilities, consider using MCRYPT_RIJNDAEL_128 instead.");
        constants.put("MCRYPT_DES",             "DES has known vulnerabilities, consider using MCRYPT_RIJNDAEL_128 instead.");
        constants.put("MCRYPT_RC2",             "RC2 has known vulnerabilities, consider using MCRYPT_RIJNDAEL_128 instead.");
        constants.put("MCRYPT_RC4",             "RC4 has known vulnerabilities, consider using MCRYPT_RIJNDAEL_128 instead.");
        constants.put("MCRYPT_ARCFOUR",         "RC4 has known vulnerabilities, consider using MCRYPT_RIJNDAEL_128 instead.");
        /* weak algorithms, openssl constants */
        constants.put("OPENSSL_CIPHER_3DES",    "3DES has known vulnerabilities, consider using AES-128-* instead.");
        constants.put("OPENSSL_CIPHER_DES",     "DES has known vulnerabilities, consider using AES-128-* instead.");
        constants.put("OPENSSL_CIPHER_RC2_40",  "RC2 has known vulnerabilities, consider using AES-128-* instead.");
        constants.put("OPENSSL_CIPHER_RC2_64",  "RC2 has known vulnerabilities, consider using AES-128-* instead.");
        /* weak algorithms, crypt constants */
        constants.put("CRYPT_MD5",              "MD5 has known vulnerabilities, consider using CRYPT_BLOWFISH instead.");
        constants.put("CRYPT_STD_DES",          "DES has known vulnerabilities, consider using CRYPT_BLOWFISH instead.");

        /*
            Functions:
                md5|sha1|crc32 => use crypt(.., CRYPT_BLOWFISH);
            Strings (possibly only resolved as string literals):
                '(tripledes)|(des3)|(des(-(ede|ede3))?(-(cbc|cfb|ecb|cfb1|cfb8|ofb))?)'
                'sha[01]?'
                'md[245]'
                'rc2(-(40|64))?(-(cbc|cfb|ecb|ofb))?'
                '(arcfour)|(rc4(-40)?)'
         */
    }

    @NotNull
    @Override
    public String getShortName() {
        return "CryptographicallySecureAlgorithmsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Cryptographically secure algorithms";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpConstantReference(@NotNull ConstantReference reference) {
                final String constantName = reference.getName();
                if (constantName != null && constants.containsKey(constantName) && !this.isTestContext(reference)) {
                    holder.registerProblem(
                            reference,
                            MessagesPresentationUtil.prefixWithEa(constants.get(constantName)),
                            ProblemHighlightType.GENERIC_ERROR
                    );
                }
            }
        };
    }
}
