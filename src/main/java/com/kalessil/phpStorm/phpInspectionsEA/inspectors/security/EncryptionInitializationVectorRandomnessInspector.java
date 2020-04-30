package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class EncryptionInitializationVectorRandomnessInspector extends BasePhpInspection {
    private static final String messagePattern = "%s() should be used for IV, but found: %s.";

    @NotNull
    private static final HashSet<String> secureFunctions = new HashSet<>();
    static {
        secureFunctions.add("random_bytes");
        secureFunctions.add("openssl_random_pseudo_bytes");
        secureFunctions.add("mcrypt_create_iv");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "EncryptionInitializationVectorRandomnessInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Encryption initialization vector randomness";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                /* variable functions are not supported, as we are checking 2 different extensions functions */
                if (functionName != null && (functionName.equals("openssl_encrypt") || functionName.equals("mcrypt_encrypt"))) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length != 5 || arguments[4] == null || arguments[4].getText().isEmpty()) {
                        return;
                    }

                    /* discover and inspect possible values */
                    final Set<PsiElement> values = PossibleValuesDiscoveryUtil.discover(arguments[4]);
                    if (! values.isEmpty()) {
                        /* check all possible values */
                        final List<String> reporting = new ArrayList<>();
                        for (final PsiElement source : values) {
                            if (OpenapiTypesUtil.isFunctionReference(source)) {
                                final String sourceName = ((FunctionReference) source).getName();
                                if (sourceName != null && secureFunctions.contains(sourceName)) {
                                    continue;
                                }
                            }
                            reporting.add(source.getText());
                        }

                        if (!reporting.isEmpty() && !this.isAggregatedGeneration(values)) {
                            /* sort reporting list to produce testable results */
                            Collections.sort(reporting);

                            final String ivFunction = functionName.startsWith("openssl_") ? "openssl_random_pseudo_bytes" : "mcrypt_create_iv";
                            holder.registerProblem(
                                    arguments[4],
                                    MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, ivFunction, String.join(", ", reporting))),
                                    ProblemHighlightType.GENERIC_ERROR
                            );
                        }
                        reporting.clear();
                    }
                    values.clear();
                }
            }

            private boolean isAggregatedGeneration(@NotNull Set<PsiElement> candidates) {
                if (candidates.size() == 1) {
                    final PsiElement candidate = candidates.iterator().next();
                    if (candidate instanceof FunctionReference) {
                        final PsiElement resolved = OpenapiResolveUtil.resolveReference((PsiReference) candidate);
                        if (resolved instanceof Function) {
                            final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(resolved);
                            if (body != null) {
                                return PsiTreeUtil.findChildrenOfType(body, FunctionReference.class)
                                                  .stream().anyMatch(c -> secureFunctions.contains(c.getName()));
                            }
                        }
                    }
                }
                return false;
            }
        };
    }
}
