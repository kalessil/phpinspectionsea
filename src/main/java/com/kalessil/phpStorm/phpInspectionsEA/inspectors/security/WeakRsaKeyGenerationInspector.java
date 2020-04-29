package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
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

public class WeakRsaKeyGenerationInspector extends LocalInspectionTool {
    private static final String messageLengthIsDefault      = "The generated RSA key length is insufficient (system defaults are 1024 or 2048 bits), using 4096 bits is recommended.";
    private static final String messageLengthIsNoSufficient = "The generated RSA key length is insufficient, using 4096 bits is recommended.";

    private static final Map<String, Integer> targetFunctions = new HashMap<>();
    static {
        targetFunctions.put("openssl_pkey_new", 1);
        targetFunctions.put("openssl_csr_new",  3);
    }

    @NotNull
    @Override
    public String getShortName() {
        return "WeakRsaKeyGenerationInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Insufficient RSA key length";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_SECURITY)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && targetFunctions.containsKey(functionName)) {
                    final PsiElement[] arguments    = reference.getParameters();
                    final int configurationPosition = targetFunctions.get(functionName);
                    if (arguments.length >= configurationPosition) {
                        for (final PsiElement variant : PossibleValuesDiscoveryUtil.discover(arguments[configurationPosition - 1])) {
                            if (variant instanceof ArrayCreationExpression) {
                                /* extract settings */
                                PhpPsiElement keyLength = null;
                                PhpPsiElement keyType   = null;
                                for (final ArrayHashElement pair : ((ArrayCreationExpression) variant).getHashElements()) {
                                    final PsiElement setting = pair.getKey();
                                    if (setting instanceof StringLiteralExpression) {
                                        final String settingName = ((StringLiteralExpression) setting).getContents();
                                        if (settingName.equals("private_key_bits")) {
                                            keyLength = pair.getValue();
                                        } else if (settingName.equals("private_key_type")) {
                                            keyType = pair.getValue();
                                        }
                                    }
                                }
                                /* understand settings */
                                final boolean isTargetKeyType = keyType == null || "OPENSSL_KEYTYPE_RSA".equals(keyType.getName());
                                if (isTargetKeyType) {
                                    if (keyLength == null) {
                                        holder.registerProblem(
                                                reference,
                                                MessagesPresentationUtil.prefixWithEa(messageLengthIsDefault)
                                        );
                                        break;
                                    }
                                    if (OpenapiTypesUtil.isNumber(keyLength)) {
                                        boolean isTarget;
                                        try {
                                            isTarget = Integer.parseInt(keyLength.getText()) <= 2048;
                                        } catch (final NumberFormatException wrongFormat) {
                                            isTarget = false;
                                        }
                                        if (isTarget) {
                                            holder.registerProblem(
                                                    reference,
                                                    MessagesPresentationUtil.prefixWithEa(messageLengthIsNoSufficient)
                                            );
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        holder.registerProblem(
                                reference,
                                MessagesPresentationUtil.prefixWithEa(messageLengthIsDefault)
                        );
                    }
                }
            }
        };
    }
}
