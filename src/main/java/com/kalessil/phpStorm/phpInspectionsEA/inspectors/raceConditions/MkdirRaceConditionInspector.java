package com.kalessil.phpStorm.phpInspectionsEA.inspectors.raceConditions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class MkdirRaceConditionInspector extends BasePhpInspection {
    private static final String patternDirectCall   = "Following construct should be used: 'if (!mkdir(%f%) && !is_dir(%f%)) { ... }'.";
    private static final String patternAndCondition = "Some check are missing: '!mkdir(%f%) && !is_dir(%f%)'.";
    private static final String patternOrCondition  = "Some check are missing: 'mkdir(%f%) || is_dir(%f%)'.";

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
                final String functionName = reference.getName();
                if (functionName == null || !functionName.equals("mkdir")) {
                    return;
                }
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length == 0 || arguments.length > 3) {
                    return;
                }

                /* false-positives: test classes */
                if (this.isTestContext(reference)) {
                    return;
                }

                /* ind out expression where the call is contained - quite big set of variations */
                final ExpressionLocateResult searchResult = new ExpressionLocateResult();
                this.locateExpression(reference, searchResult);
                final PsiElement target  = searchResult.getReportingTarget();
                final PsiElement context = target == null ? null : target.getParent();
                if (target == null) {
                    return;
                }

                // case 1: if ([!]mkdir(...))
                if (context instanceof If || OpenapiTypesUtil.isStatementImpl(context)) {
                    final String resource = arguments[0].getText();
                    final String binary   = searchResult.isInverted ? patternAndCondition : patternOrCondition;
                    final String message  = (context instanceof If ? binary : patternDirectCall)
                            .replace("%f%", resource)
                            .replace("%f%", resource);

                    final LocalQuickFix fixer
                        = (context instanceof If ? new HardenConditionFix(resource) : new ThrowExceptionFix(resource));
                    holder.registerProblem(context instanceof If ? target : context, message, fixer);
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
                        final String resource        = arguments[0].getText();
                        final IElementType operation = binary.getOperationType();
                        final String message =
                            (PhpTokenTypes.tsSHORT_CIRCUIT_AND_OPS.contains(operation) ? patternAndCondition : patternOrCondition)
                                .replace("%f%", resource)
                                .replace("%f%", resource);
                        holder.registerProblem(target, message, new HardenConditionFix(resource));
                    }
                }
            }

            private void locateExpression(@NotNull PsiElement expression, @NotNull ExpressionLocateResult status) {
                final PsiElement parent = expression.getParent();

                if (
                    parent instanceof If || parent instanceof AssignmentExpression ||
                    OpenapiTypesUtil.isStatementImpl(parent)
                ) {
                    status.setReportingTarget(expression);
                    return;
                }
                if (parent instanceof ParenthesizedExpression) {
                    this.locateExpression(parent, status);
                    return;
                }
                if (parent instanceof UnaryExpression) {
                    final UnaryExpression unary = (UnaryExpression) parent;
                    final PsiElement operation  = unary.getOperation();
                    if (operation != null) {
                        final IElementType operator = operation.getNode().getElementType();
                        if (operator == PhpTokenTypes.opNOT) {
                            status.setInverted(!status.isInverted());
                            this.locateExpression(unary, status);
                            return;
                        }
                        if (operator == PhpTokenTypes.opSILENCE) {
                            this.locateExpression(unary, status);
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
                    this.locateExpression(binary, status);
                }
            }
        };
    }

    private static class ExpressionLocateResult {
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

    private static class ThrowExceptionFix implements LocalQuickFix {
        private final String resource;

        @NotNull
        @Override
        public String getName() {
            return "Replace with conditional expression";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        ThrowExceptionFix(@NotNull String resource) {
            this.resource = resource;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null) {
                final String throwPart = "throw new \\RuntimeException(sprintf('Directory \"%%s\" was not created', %s));";
                final String pattern   = "if (!mkdir(%s) && !is_dir(%s)) { %s }";
                final String code      = String.format(pattern, resource, resource, String.format(throwPart, resource));
                target.replace(PhpPsiElementFactory.createPhpPsiFromText(project, If.class, code));
            }
        }
    }

    private static class HardenConditionFix implements LocalQuickFix {
        private final String resource;

        @NotNull
        @Override
        public String getName() {
            return "Harden the condition";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        HardenConditionFix(@NotNull String resource) {
            this.resource = resource;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null) {
                final PsiElement parent = target.getParent();
                if (parent instanceof If) {
                    final String code = String.format("(!mkdir(%s) && !is_dir(%s))", resource, resource);
                    target.replace(PhpPsiElementFactory.createPhpPsiFromText(project, ParenthesizedExpression.class, code).getArgument());
                } else if (parent instanceof BinaryExpression) {
                    final BinaryExpression binary = (BinaryExpression) parent;
                    final IElementType operation  = binary.getOperationType();
                    final String conditions       = binary.getLeftOperand().getText();
                    if (PhpTokenTypes.tsSHORT_CIRCUIT_AND_OPS.contains(operation)) {
                        final String code = String.format("(%s && !mkdir(%s) && !is_dir(%s))", conditions, resource, resource);
                        parent.replace(PhpPsiElementFactory.createPhpPsiFromText(project, ParenthesizedExpression.class, code).getArgument());
                    } else if (PhpTokenTypes.tsSHORT_CIRCUIT_OR_OPS.contains(operation)) {
                        final String code = String.format("(%s || mkdir(%s) || is_dir(%s))", conditions, resource, resource);
                        parent.replace(PhpPsiElementFactory.createPhpPsiFromText(project, ParenthesizedExpression.class, code).getArgument());
                    }
                }
            }
        }
    }
}
