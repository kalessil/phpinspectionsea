package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import org.jetbrains.annotations.NotNull;

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
            public void visitPhpConstantReference(ConstantReference reference) {
                final String constantName = reference.getName();
                if (StringUtil.isEmpty(constantName) || !constantName.startsWith("CURLOPT_")) {
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
                        StringUtil.isEmpty(functionName) || !functionName.equals("curl_setopt")
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
                if (value instanceof ConstantReference) {
                    return true;
                }
                if (value instanceof StringLiteralExpression) {
                    return  !((StringLiteralExpression) value).getContents().equals("2");
                }
                if (1 == value.getTextLength() && !value.getText().equals("2")) {
                    return true;
                }
                if (value instanceof TernaryExpression) {
                    final PsiElement trueVariant  = ((TernaryExpression) value).getTrueVariant();
                    final PsiElement falseVariant = ((TernaryExpression) value).getFalseVariant();
                    if (null != trueVariant && null != falseVariant) {
                        return isHostVerifyDisabled(trueVariant) && isHostVerifyDisabled(falseVariant);
                    }
                }

                return false;
            }

            private boolean isPeerVerifyDisabled(@NotNull PsiElement value) {
                if (value instanceof ConstantReference) {
                    final String optionName = ((ConstantReference) value).getName();
                    return !StringUtil.isEmpty(optionName) && !optionName.equalsIgnoreCase("true");
                }
                if (value instanceof StringLiteralExpression) {
                    return  ((StringLiteralExpression) value).getContents().equals("0");
                }
                //noinspection RedundantIfStatement
                if (1 == value.getTextLength() && value.getText().equals("0")) {
                    return true;
                }

                return false;
            }
        };
    }
}
