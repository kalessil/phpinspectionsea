package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
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

public class CurlSslServerSpoofingInspector extends LocalInspectionTool {
    private static final String messageVerifyHost = "Exposes a connection to MITM attacks. Use 2 (default) to stay safe.";
    private static final String messageVerifyPeer = "Exposes a connection to MITM attacks. Use true (default) to stay safe.";

    @NotNull
    public String getShortName() {
        return "CurlSslServerSpoofingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpConstantReference(@NotNull ConstantReference reference) {
                final String constantName = reference.getName();
                if (constantName == null || !constantName.startsWith("CURLOPT_")) {
                    return;
                }

                /* get 2nd parent level: ArrayHashElement, FunctionReference */
                PsiElement parent = reference.getParent();
                parent = null == parent ? null : parent.getParent();
                if (null == parent) {
                    return;
                }

                if (constantName.equals("CURLOPT_SSL_VERIFYHOST") || constantName.equals("CURLOPT_SSL_VERIFYPEER")) {
                    checkConstantUsage(parent, constantName, reference);
                }
            }

            private void checkConstantUsage(
                    @NotNull PsiElement parent,
                    @NotNull String constantName, @NotNull ConstantReference constant
            ) {
                if (parent instanceof FunctionReference) {
                    final FunctionReference call = (FunctionReference) parent;
                    final PsiElement[] params    = call.getParameters();
                    final String functionName    = call.getName();
                    if (
                        3 != params.length || null == params[2] ||
                        functionName == null || !functionName.equals("curl_setopt")
                    ) {
                        return;
                    }

                    if (constantName.equals("CURLOPT_SSL_VERIFYHOST")) {
                        if (isHostVerifyDisabled(params[2])) {
                            holder.registerProblem(parent, messageVerifyHost, ProblemHighlightType.GENERIC_ERROR);
                        }
                        return;
                    }
                    if (constantName.equals("CURLOPT_SSL_VERIFYPEER")) {
                        if (isPeerVerifyDisabled(params[2])) {
                            holder.registerProblem(parent, messageVerifyPeer, ProblemHighlightType.GENERIC_ERROR);
                        }
                        return;
                    }

                    return;
                }

                if (parent instanceof ArrayHashElement && constant == ((ArrayHashElement) parent).getKey()) {
                    final PsiElement value = ((ArrayHashElement) parent).getValue();
                    if (null == value) {
                        return;
                    }

                    if (constantName.equals("CURLOPT_SSL_VERIFYHOST")) {
                        if (isHostVerifyDisabled(value)) {
                            holder.registerProblem(parent, messageVerifyHost, ProblemHighlightType.GENERIC_ERROR);
                        }
                        return;
                    }
                    if (constantName.equals("CURLOPT_SSL_VERIFYPEER")) {
                        if (isPeerVerifyDisabled(value)) {
                            holder.registerProblem(parent, messageVerifyPeer, ProblemHighlightType.GENERIC_ERROR);
                        }
                        // return;
                    }

                    // return;
                }
            }

            private boolean isHostVerifyDisabled(@NotNull PsiElement value) {
                boolean result = false;

                final Set<PsiElement> discovered = PossibleValuesDiscoveryUtil.discover(value);
                if (discovered.size() > 0) {
                    int countDisables = 0;
                    int countEnables  = 0;

                    for (PsiElement possibleValue : discovered) {
                        if (possibleValue instanceof StringLiteralExpression) {
                            boolean disabled = !((StringLiteralExpression) possibleValue).getContents().equals("2");
                            int dummy        = disabled ? ++countDisables : ++countEnables;
                            continue;
                        }
                        if (possibleValue instanceof ConstantReference) {
                            ++countDisables;
                            continue;
                        }
                        if (1 == possibleValue.getTextLength()) {
                            boolean disabled = !possibleValue.getText().equals("2");
                            int dummy        = disabled ? ++countDisables : ++countEnables;
                            //continue;
                        }

                        /* other expressions are not supported currently */
                    }
                    discovered.clear();

                    result = countDisables > 0 && 0 == countEnables;
                }

                return result;
            }

            private boolean isPeerVerifyDisabled(@NotNull PsiElement value) {
                boolean result = false;

                final Set<PsiElement> discovered = PossibleValuesDiscoveryUtil.discover(value);
                if (!discovered.isEmpty()) {
                    int countDisables = 0;
                    int countEnables  = 0;

                    for (final PsiElement possibleValue : discovered) {
                        if (possibleValue instanceof StringLiteralExpression) {
                            boolean disabled = !((StringLiteralExpression) possibleValue).getContents().equals("1");
                            int dummy        = disabled ? ++countDisables : ++countEnables;
                            continue;
                        }
                        if (possibleValue instanceof ConstantReference) {
                            boolean disabled = !PhpLanguageUtil.isTrue(possibleValue);
                            int dummy        = disabled ? ++countDisables : ++countEnables;
                            continue;
                        }
                        if (1 == possibleValue.getTextLength()) {
                            boolean disabled = !possibleValue.getText().equals("1");
                            int dummy        = disabled ? ++countDisables : ++countEnables;
                            // continue;
                        }

                        /* other expressions are not supported currently */
                    }
                    discovered.clear();

                    result = countDisables > 0 && 0 == countEnables;
                }

                return result;
            }
        };
    }
}
