package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class EncryptionInitializationVectorRandomnessInspector extends BasePhpInspection {
    private static final String message = "Nooooo";

    @NotNull
    public String getShortName() {
        return "EncryptionInitializationVectorRandomnessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* verify general requirements to the call */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (5 != params.length || null == params[4] || StringUtil.isEmpty(functionName)) {
                    return;
                }

                if (functionName.equals("openssl_encrypt") || functionName.equals("mcrypt_encrypt")) {
                    /* discover what can be provided as 5th argument within callable and class property */
                    HashSet<PsiElement> values = PossibleValuesDiscoveryUtil.discover(params[4]);
                    if (values.size() > 0) {
                        for (PsiElement source : values) {
                            // functions: only random_bytes,openssl_random_pseudo_bytes,mcrypt_create_iv are allowed
                            // other options: bad, but we should have weak warning for cases like arrays
                        }

                        values.clear();
                    }
                }
            }
        };
    }
}
