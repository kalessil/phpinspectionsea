package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class CurlSslServerSpoofingInspector extends LocalInspectionTool {
    private static final String messageVerifyHost = "CURLOPT_SSL_VERIFYHOST should be 2";
    private static final String messageVerifyPeer = "CURLOPT_SSL_VERIFYPEER should be 1";

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
                        boolean disabled = false;
                        if (params[2] instanceof ConstantReference) {
                            disabled = true;
                        }
                        if (!disabled && params[2] instanceof StringLiteralExpression) {
                            disabled = !((StringLiteralExpression) params[2]).getContents().equals("2");
                        }
                        if (!disabled && 1 == params[2].getTextLength() && !params[2].getText().equals("2")) {
                            disabled = true;
                        }

                        if (disabled) {
                            holder.registerProblem(parent, messageVerifyHost, ProblemHighlightType.GENERIC_ERROR);
                        }
                        return;
                    }

                    if (constantName.equals("CURLOPT_SSL_VERIFYPEER")) {
                        boolean disabled = false;
                        if (params[2] instanceof ConstantReference) {
                            final String optionName = ((ConstantReference) params[2]).getName();
                            disabled = !StringUtil.isEmpty(optionName) && !optionName.equalsIgnoreCase("true");
                        }
                        if (!disabled && params[2] instanceof StringLiteralExpression) {
                            disabled = ((StringLiteralExpression) params[2]).getContents().equals("0");
                        }
                        if (!disabled && 1 == params[2].getTextLength() && params[2].getText().equals("0")) {
                            disabled = true;
                        }

                        if (disabled) {
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
                        boolean disabled = false;
                        if (value instanceof ConstantReference) {
                            disabled = true;
                        }
                        if (!disabled && value instanceof StringLiteralExpression) {
                            disabled = !((StringLiteralExpression) value).getContents().equals("2");
                        }
                        if (!disabled && 1 == value.getTextLength() && !value.getText().equals("2")) {
                            disabled = true;
                        }

                        if (disabled) {
                            holder.registerProblem(parent, messageVerifyHost, ProblemHighlightType.GENERIC_ERROR);
                        }
                        return;
                    }

                    if (constantName.equals("CURLOPT_SSL_VERIFYPEER")) {
                        boolean disabled = false;
                        if (value instanceof ConstantReference) {
                            final String optionName = ((ConstantReference) value).getName();
                            disabled = !StringUtil.isEmpty(optionName) && !optionName.equalsIgnoreCase("true");
                        }
                        if (!disabled && value instanceof StringLiteralExpression) {
                            disabled = ((StringLiteralExpression) value).getContents().equals("0");
                        }
                        if (!disabled && 1 == value.getTextLength() && value.getText().equals("0")) {
                            disabled = true;
                        }

                        if (disabled) {
                            holder.registerProblem(parent, messageVerifyPeer, ProblemHighlightType.GENERIC_ERROR);
                        }
                        // return;
                    }

                    // return;
                }
            }
        };
    }
}
