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
    @Override
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
                if (constantName != null && constantName.startsWith("CURLOPT_")) {
                    /* get 2nd parent level: ArrayHashElement, FunctionReference */
                    final PsiElement parent  = reference.getParent();
                    final PsiElement context = parent == null ? null : parent.getParent();
                    if (context != null) {
                        final boolean isTarget =
                                constantName.equals("CURLOPT_SSL_VERIFYHOST") ||
                                constantName.equals("CURLOPT_SSL_VERIFYPEER");
                        if (isTarget) {
                            this.checkConstantUsage(context, constantName, reference);
                        }
                    }
                }
            }

            private void checkConstantUsage(
                    @NotNull PsiElement parent,
                    @NotNull String constantName,
                    @NotNull ConstantReference constant
            ) {
                if (parent instanceof FunctionReference) {
                    final FunctionReference call = (FunctionReference) parent;
                    final String functionName    = call.getName();
                    if (functionName != null && functionName.equals("curl_setopt")) {
                        final PsiElement[] params = call.getParameters();
                        if (params.length == 3 && params[2] != null) {
                            if (constantName.equals("CURLOPT_SSL_VERIFYHOST")) {
                                if (this.isHostVerifyDisabled(params[2])) {
                                    holder.registerProblem(parent, messageVerifyHost, ProblemHighlightType.GENERIC_ERROR);
                                }
                            } else if (constantName.equals("CURLOPT_SSL_VERIFYPEER")) {
                                if (this.isPeerVerifyDisabled(params[2])) {
                                    holder.registerProblem(parent, messageVerifyPeer, ProblemHighlightType.GENERIC_ERROR);
                                }
                            }
                        }
                    }
                } else if (parent instanceof ArrayHashElement && constant == ((ArrayHashElement) parent).getKey()) {
                    final PsiElement value = ((ArrayHashElement) parent).getValue();
                    if (value != null) {
                        if (constantName.equals("CURLOPT_SSL_VERIFYHOST")) {
                            if (this.isHostVerifyDisabled(value)) {
                                holder.registerProblem(parent, messageVerifyHost, ProblemHighlightType.GENERIC_ERROR);
                            }
                        }  else if (constantName.equals("CURLOPT_SSL_VERIFYPEER")) {
                            if (this.isPeerVerifyDisabled(value)) {
                                holder.registerProblem(parent, messageVerifyPeer, ProblemHighlightType.GENERIC_ERROR);
                            }
                        }
                    }
                }
            }

            private boolean isHostVerifyDisabled(@NotNull PsiElement value) {
                boolean result = false;

                final Set<PsiElement> discovered = PossibleValuesDiscoveryUtil.discover(value);
                if (!discovered.isEmpty()) {
                    int countDisables = 0;
                    int countEnables  = 0;

                    for (final PsiElement possibleValue : discovered) {
                        if (possibleValue instanceof StringLiteralExpression) {
                            boolean disabled = !((StringLiteralExpression) possibleValue).getContents().equals("2");
                            if (disabled) {
                                ++countDisables;
                            } else {
                                ++countEnables;
                            }
                        } else if (possibleValue instanceof ConstantReference) {
                            ++countDisables;
                        } else if (possibleValue.getTextLength() == 1) {
                            boolean disabled = !possibleValue.getText().equals("2");
                            if (disabled) {
                                ++countDisables;
                            } else {
                                ++countEnables;
                            }
                        }
                    }
                    discovered.clear();

                    result = countDisables > 0 && countEnables == 0;
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
                            if (disabled) {
                                ++countDisables;
                            } else {
                                ++countEnables;
                            }
                        } else if (possibleValue instanceof ConstantReference) {
                            boolean disabled = !PhpLanguageUtil.isTrue(possibleValue);
                            if (disabled) {
                                ++countDisables;
                            } else {
                                ++countEnables;
                            }
                        } else if (possibleValue.getTextLength() == 1) {
                            boolean disabled = !possibleValue.getText().equals("1");
                            if (disabled) {
                                ++countDisables;
                            } else {
                                ++countEnables;
                            }
                        }
                        /* other expressions are not supported currently */
                    }
                    discovered.clear();

                    result = countDisables > 0 && countEnables == 0;
                }

                return result;
            }
        };
    }
}
