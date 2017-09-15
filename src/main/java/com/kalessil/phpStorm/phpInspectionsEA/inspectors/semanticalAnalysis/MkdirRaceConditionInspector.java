package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FileSystemUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MkdirRaceConditionInspector extends BasePhpInspection {
    private static final String patternMkdirDirectCall   = "Following construct should be used: 'if (!mkdir(%f%) && !is_dir(%f%)) { ... }'.";
    private static final String patternMkdirAndCondition = "Some check are missing: '!mkdir(%f%) && !is_dir(%f%)'.";
    private static final String patternMkdirOrCondition  = "Some check are missing: 'mkdir(%f%) || is_dir(%f%)'.";

    @NotNull
    public String getShortName() {
        return "MkdirRaceConditionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName    = reference.getName();
                final PsiElement[] arguments = reference.getParameters();
                if (functionName == null || arguments.length != 1 || !functionName.equals("mkdir")) {
                    return;
                }

                /* ignore test classes */
                final Function scope = ExpressionSemanticUtil.getScope(reference);
                if (scope instanceof Method) {
                    final PhpClass clazz = ((Method) scope).getContainingClass();
                    if (null != clazz && FileSystemUtil.isTestClass(clazz)) {
                        return;
                    }
                }

                /* ind out expression where the call is contained - quite big set of variations */
                final ExpressionLocateResult searchResult = new ExpressionLocateResult();
                this.getCompleteExpression(reference, searchResult);
                final PsiElement target  = searchResult.getReportingTarget();
                final PsiElement context = target == null ? null : target.getParent();
                if (target == null) {
                    return;
                }

                // case 1: if ([!]mkdir(...))
                if (context instanceof If || OpenapiTypesUtil.isStatementImpl(context)) {
                    final String resource = arguments[0].getText();
                    final String binary   = searchResult.isInverted ? patternMkdirAndCondition : patternMkdirOrCondition;
                    final String message  = (context instanceof If ? binary : patternMkdirDirectCall)
                            .replace("%f%", resource)
                            .replace("%f%", resource);
                    holder.registerProblem(context instanceof If ? target : context, message);
                }
                // case 2: && and || expressions
                else if (context instanceof BinaryExpression) {
                    boolean isSecondExistenceCheckExists = false;

                    /* deal with nested conditions */
                    BinaryExpression binary = (BinaryExpression) context;
                    if (binary.getRightOperand() == target && binary.getParent() instanceof BinaryExpression) {
                        binary = (BinaryExpression) binary.getParent();
                    }

                    /* check if following expression contains is_dir */
                    final PsiElement candidate          = binary.getRightOperand();
                    final List<FunctionReference> calls = new ArrayList<>();
                    if (candidate instanceof FunctionReference) {
                        calls.add((FunctionReference) candidate);
                    }
                    calls.addAll(PsiTreeUtil.findChildrenOfType(candidate, FunctionReference.class));

                    for (final FunctionReference call : calls) {
                        final String name = call.getName();
                        if (name != null && name.equals("is_dir") && OpenapiTypesUtil.isFunctionReference(call)) {
                            /* TODO: argument needs match as well */
                            isSecondExistenceCheckExists = true;
                            break;
                        }
                    }
                    calls.clear();

                    /* report when needed */
                    if (!isSecondExistenceCheckExists) {
                        final IElementType operation = binary.getOperationType();
                        final String message =
                                (operation == PhpTokenTypes.opAND ? patternMkdirAndCondition : patternMkdirOrCondition)
                                .replace("%f%", arguments[0].getText())
                                .replace("%f%", arguments[0].getText());
                        holder.registerProblem(target, message);
                    }
                }
            }

            private void getCompleteExpression(@NotNull PsiElement expression, @NotNull ExpressionLocateResult status) {
                final PsiElement parent = expression.getParent();

                if (
                    parent instanceof If || parent instanceof AssignmentExpression ||
                    OpenapiTypesUtil.isStatementImpl(parent)
                ) {
                    status.setReportingTarget(expression);
                    return;
                }
                if (parent instanceof ParenthesizedExpression) {
                    this.getCompleteExpression(parent, status);
                    return;
                }
                if (parent instanceof UnaryExpression) {
                    final UnaryExpression unary = (UnaryExpression) parent;
                    final PsiElement operation  = unary.getOperation();
                    if (operation != null) {
                        final IElementType operator = operation.getNode().getElementType();
                        if (operator == PhpTokenTypes.opNOT) {
                            status.setInverted(!status.isInverted());
                            this.getCompleteExpression(unary, status);
                            return;
                        }
                        if (operator == PhpTokenTypes.opSILENCE) {
                            this.getCompleteExpression(unary, status);
                        }
                    }
                    return;
                }
                if (parent instanceof BinaryExpression) {
                    final BinaryExpression binary = (BinaryExpression) parent;
                    final IElementType operation  = binary.getOperationType();
                    if (
                        PhpTokenTypes.tsSHORT_CIRCUIT_AND_OPS.contains(operation) ||
                        PhpTokenTypes.tsSHORT_CIRCUIT_OR_OPS.contains(operation)
                    ) {
                        status.setReportingTarget(expression);
                        return;
                    }
                    this.getCompleteExpression(binary, status);
                }
            }
        };
    }

    final class ExpressionLocateResult {
        private PsiElement reportingTarget;
        private boolean isInverted;

        boolean isInverted() {
            return isInverted;
        }
        void setInverted(boolean inverted) {
            isInverted = inverted;
        }

        @Nullable
        PsiElement getReportingTarget() {
            return reportingTarget;
        }
        void setReportingTarget(@NotNull PsiElement reportingTarget) {
            this.reportingTarget = reportingTarget;
        }
    }
}
