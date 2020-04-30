package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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

    @NotNull
    @Override
    public String getDisplayName() {
        return "CURL: SSL server spoofing (SSL MITM and Spoofing Attacks)";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpConstantReference(@NotNull ConstantReference reference) {
                final String constantName = reference.getName();
                if (constantName != null && constantName.startsWith("CURLOPT_")) {
                    final PsiElement parent  = reference.getParent();
                    final PsiElement context = parent == null ? null : parent.getParent();
                    if (context != null) {
                        final boolean isTarget = constantName.equals("CURLOPT_SSL_VERIFYHOST") || constantName.equals("CURLOPT_SSL_VERIFYPEER");
                        if (isTarget) {
                            this.analyze(context, constantName, reference);
                        }
                    }
                }
            }

            private void analyze(@NotNull PsiElement parent, @NotNull String constantName, @NotNull ConstantReference constant) {
                if (parent instanceof FunctionReference) {
                    final FunctionReference call = (FunctionReference) parent;
                    final String functionName    = call.getName();
                    if (functionName != null && functionName.equals("curl_setopt")) {
                        final PsiElement[] arguments = call.getParameters();
                        if (arguments.length == 3) {
                            final PsiElement value = arguments[2];
                            if (value != null) {
                                if (constantName.equals("CURLOPT_SSL_VERIFYHOST")) {
                                    if (this.isHostVerifyDisabled(value)) {
                                        holder.registerProblem(
                                                parent,
                                                MessagesPresentationUtil.prefixWithEa(messageVerifyHost),
                                                ProblemHighlightType.GENERIC_ERROR
                                        );
                                    }
                                } else if (constantName.equals("CURLOPT_SSL_VERIFYPEER")) {
                                    if (this.isPeerVerifyDisabled(value)) {
                                        holder.registerProblem(
                                                parent,
                                                MessagesPresentationUtil.prefixWithEa(messageVerifyPeer),
                                                ProblemHighlightType.GENERIC_ERROR
                                        );
                                    }
                                }
                            }
                        }
                    }
                } else if (parent instanceof ArrayHashElement && constant == ((ArrayHashElement) parent).getKey()) {
                    final PsiElement value = ((ArrayHashElement) parent).getValue();
                    if (value != null) {
                        if (constantName.equals("CURLOPT_SSL_VERIFYHOST")) {
                            if (this.isHostVerifyDisabled(value)) {
                                holder.registerProblem(
                                        parent,
                                        MessagesPresentationUtil.prefixWithEa(messageVerifyHost),
                                        ProblemHighlightType.GENERIC_ERROR
                                );
                            }
                        } else if (constantName.equals("CURLOPT_SSL_VERIFYPEER")) {
                            if (this.isPeerVerifyDisabled(value)) {
                                holder.registerProblem(
                                        parent,
                                        MessagesPresentationUtil.prefixWithEa(messageVerifyPeer),
                                        ProblemHighlightType.GENERIC_ERROR
                                );
                            }
                        }
                    }
                } else if (parent instanceof ArrayAccessExpression) {
                    PsiElement context = parent;
                    while (context instanceof ArrayAccessExpression) {
                        context = context.getParent();
                    }
                    if (OpenapiTypesUtil.isAssignment(context)) {
                        final PsiElement value = ((AssignmentExpression) context).getValue();
                        if (value != null) {
                            if (constantName.equals("CURLOPT_SSL_VERIFYHOST")) {
                                if (this.isHostVerifyDisabled(value)) {
                                    holder.registerProblem(
                                            context,
                                            MessagesPresentationUtil.prefixWithEa(messageVerifyHost),
                                            ProblemHighlightType.GENERIC_ERROR
                                    );
                                }
                            } else if (constantName.equals("CURLOPT_SSL_VERIFYPEER")) {
                                if (this.isPeerVerifyDisabled(value)) {
                                    holder.registerProblem(
                                            context,
                                            MessagesPresentationUtil.prefixWithEa(messageVerifyPeer),
                                            ProblemHighlightType.GENERIC_ERROR
                                    );
                                }
                            }
                        }
                    }
                }
            }

            private boolean isHostVerifyDisabled(@NotNull PsiElement value) {
                boolean result = false;

                final Set<PsiElement> discovered = PossibleValuesDiscoveryUtil.discover(value);
                if (! discovered.isEmpty()) {
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
                    result = countDisables > 0 && countEnables == 0;
                }
                discovered.clear();

                return result;
            }

            private boolean isPeerVerifyDisabled(@NotNull PsiElement value) {
                boolean result = false;

                final Set<PsiElement> discovered = PossibleValuesDiscoveryUtil.discover(value);
                if (! discovered.isEmpty()) {
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
                    result = countDisables > 0 && countEnables == 0;
                }
                discovered.clear();

                return result;
            }
        };
    }
}
