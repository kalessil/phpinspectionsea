package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class HashTimingAttacksInspector extends PhpInspection {
    private static final String message = "This construct is probably vulnerable to hash timing attacks, please use 'hash_equals(...)' or 'password_verify(...)' instead.";

    private static final Set<String> targetFunctions = new HashSet<>();
    static {
        targetFunctions.add("md5");
        targetFunctions.add("sha1");
        targetFunctions.add("crypt");
        targetFunctions.add("hash");
        targetFunctions.add("hash_hmac");
        targetFunctions.add("hash_hkdf");
        targetFunctions.add("hash_pbkdf2");
        targetFunctions.add("password_hash");
        targetFunctions.add("openssl_digest");
        targetFunctions.add("openssl_encrypt");
        targetFunctions.add("openssl_pbkdf2");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "HashTimingAttacksInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Hash timing attack";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_SECURITY)) { return; }

                if (PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP560)) {
                    final String name = reference.getName();
                    if (name != null && targetFunctions.contains(name) && this.isFromRootNamespace(reference)) {
                        final PsiElement parent = reference.getParent();
                        if (parent instanceof BinaryExpression || OpenapiTypesUtil.isAssignment(parent)) {
                            this.analyzeInContext(parent, reference);
                        } else if (parent instanceof ParameterList) {
                            final PsiElement grandParent = parent.getParent();
                            if (OpenapiTypesUtil.isFunctionReference(grandParent)) {
                                this.analyzeInContext(grandParent, reference);
                            }
                        }
                    }
                }
            }

            private void analyzeInContext(@NotNull PsiElement context, @NotNull FunctionReference reference) {
                if (context instanceof BinaryExpression) {
                    final PsiElement second = OpenapiElementsUtil.getSecondOperand((BinaryExpression) context, reference);
                    if (second != null && this.isTarget((BinaryExpression) context, second)) {
                        holder.registerProblem(context, ReportingUtil.wrapReportedMessage(message));
                    }
                } else if (OpenapiTypesUtil.isFunctionReference(context)) {
                    if (this.isTarget((FunctionReference) context)) {
                        holder.registerProblem(context, ReportingUtil.wrapReportedMessage(message));
                    }
                } else if (OpenapiTypesUtil.isAssignment(context)) {
                    final PsiElement container = ((AssignmentExpression) context).getVariable();
                    if (container instanceof Variable) {
                        final Function scope = ExpressionSemanticUtil.getScope(context);
                        if (scope != null) {
                            final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(scope);
                            final String variableName = ((Variable) container).getName();

                            boolean passedStartingPoint = false;
                            for (final Variable variable : PsiTreeUtil.findChildrenOfType(body, Variable.class)) {
                                if (variableName.equals(variable.getName())) {
                                    passedStartingPoint = passedStartingPoint || container == variable;
                                    if (passedStartingPoint && container != variable) {
                                        final PsiElement parent = variable.getParent();
                                        if (parent instanceof BinaryExpression) {
                                            final PsiElement second = OpenapiElementsUtil.getSecondOperand((BinaryExpression) parent, variable);
                                            if (second != null && this.isTarget((BinaryExpression) parent, second)) {
                                                holder.registerProblem(parent, ReportingUtil.wrapReportedMessage(message));
                                                break;
                                            }
                                        } else if (parent instanceof ParameterList) {
                                            final PsiElement grandParent = parent.getParent();
                                            if (OpenapiTypesUtil.isFunctionReference(grandParent) && this.isTarget((FunctionReference) grandParent)) {
                                                holder.registerProblem(grandParent, ReportingUtil.wrapReportedMessage(message));
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private boolean isTarget(@NotNull BinaryExpression binary, @NotNull PsiElement alternative) {
                final IElementType operation = binary.getOperationType();
                if (operation == PhpTokenTypes.opEQUAL || operation == PhpTokenTypes.opIDENTICAL || operation == PhpTokenTypes.opNOT_EQUAL || operation == PhpTokenTypes.opNOT_IDENTICAL) {
                    return !PhpLanguageUtil.isFalse(alternative);
                }
                return false;
            }

            private boolean isTarget(@NotNull FunctionReference call) {
                final String callName = call.getName();
                return callName != null && (callName.equals("strcmp") || callName.equals("strncmp"));
            }
        };
    }
}
