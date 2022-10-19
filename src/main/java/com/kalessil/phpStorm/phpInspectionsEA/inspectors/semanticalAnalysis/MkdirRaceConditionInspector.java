package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

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
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class MkdirRaceConditionInspector extends BasePhpInspection {
    private static final String patternDirectCall       = "Following construct should be used: 'if (!is_dir(%s) && !mkdir(...)) { ... }'.";
    private static final String patternFailAndCondition = "Some check are missing: '!is_dir(%s) && !mkdir(...)'.";
    private static final String patternFailOrCondition  = "Some check are missing: 'is_dir(%s) || mkdir(...)'.";

    @NotNull
    @Override
    public String getShortName() {
        return "MkdirRaceConditionInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'mkdir(...)' race condition";
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

                /* false-positives: test classes and functions not from root NS */
                if (this.isTestContext(reference) || !this.isFromRootNamespace(reference)) {
                    return;
                }

                /* ind out expression where the call is contained - quite big set of variations */
                final ExpressionLocateResult searchResult = new ExpressionLocateResult();
                this.locateExpression(reference, searchResult);
                final PsiElement target = searchResult.getReportingTarget();
                if (target == null) {
                    return;
                }

                final PsiElement context = target.getParent();
                // case 1: if ([!]mkdir(...) [===|!== true|false])
                if (context instanceof If || OpenapiTypesUtil.isStatementImpl(context)) {
                    final List<String> fixerArguments = Arrays.stream(arguments).map(PsiElement::getText).collect(Collectors.toList());
                    final String binary               = searchResult.isInverted ? patternFailAndCondition : patternFailOrCondition;
                    final String messagePattern       = (context instanceof If ? binary : patternDirectCall);
                    holder.registerProblem(
                            context instanceof If ? target : context,
                            MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, String.join(", ", fixerArguments))),
                            context instanceof If ? new HardenConditionFix(arguments[0], fixerArguments, searchResult.isInverted) : new ThrowExceptionFix(arguments[0], fixerArguments)
                    );
                }
                // case 2: && and || expressions
                else if (context instanceof BinaryExpression) {
                    boolean isSecondExistenceCheckExists = false;

                    /* false-positive: `... or die` construct */
                    BinaryExpression binary = (BinaryExpression) context;
                    if (binary.getRightOperand() instanceof PhpExit) {
                        return;
                    }

                    /* deal with nested conditions */
                    final PsiElement parent = binary.getParent();
                    if (binary.getRightOperand() == target && parent instanceof BinaryExpression) {
                        binary = (BinaryExpression) parent;
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
                        final List<String> fixerArguments = Arrays.stream(arguments).map(PsiElement::getText).collect(Collectors.toList());
                        final String messagePattern       = (PhpTokenTypes.tsSHORT_CIRCUIT_AND_OPS.contains(binary.getOperationType()) ? patternFailAndCondition : patternFailOrCondition);
                         holder.registerProblem(
                                target,
                                MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, String.join(", ", fixerArguments), arguments[0].getText())),
                                new HardenConditionFix(arguments[0], fixerArguments, searchResult.isInverted)
                        );
                    }
                }
            }

            private void locateExpression(@NotNull PsiElement expression, @NotNull ExpressionLocateResult status) {
                final PsiElement parent = expression.getParent();
                if (parent instanceof If || parent instanceof AssignmentExpression || OpenapiTypesUtil.isStatementImpl(parent)) {
                    status.setReportingTarget(expression);
                } else if (parent instanceof ParenthesizedExpression) {
                    this.locateExpression(parent, status);
                } else if (parent instanceof UnaryExpression) {
                    final PsiElement operation = ((UnaryExpression) parent).getOperation();
                    if (operation != null) {
                        if (OpenapiTypesUtil.is(operation, PhpTokenTypes.opNOT)) {
                            status.setInverted(!status.isInverted());
                            this.locateExpression(parent, status);
                        } else if (OpenapiTypesUtil.is(operation, PhpTokenTypes.opSILENCE)) {
                            this.locateExpression(parent, status);
                        }
                    }
                } else if (parent instanceof BinaryExpression) {
                    final BinaryExpression binary = (BinaryExpression) parent;
                    final IElementType operation  = binary.getOperationType();
                    if (PhpTokenTypes.tsSHORT_CIRCUIT_AND_OPS.contains(operation) || PhpTokenTypes.tsSHORT_CIRCUIT_OR_OPS.contains(operation)) {
                        status.setReportingTarget(expression);
                    } else {
                        if (operation == PhpTokenTypes.opIDENTICAL || operation == PhpTokenTypes.opNOT_IDENTICAL) {
                            final PsiElement second = OpenapiElementsUtil.getSecondOperand(binary, expression);
                            if (PhpLanguageUtil.isBoolean(second)) {
                                if (PhpLanguageUtil.isFalse(second))            { status.setInverted(!status.isInverted()); }
                                if (operation == PhpTokenTypes.opNOT_IDENTICAL) { status.setInverted(!status.isInverted()); }
                            }
                        }
                        this.locateExpression(parent, status);
                    }
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

    private static final class ThrowExceptionFix implements LocalQuickFix {
        private static final String title = "Replace with conditional expression";

        private final String resource;
        private final String arguments;
        private final boolean withVariable;

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        ThrowExceptionFix(@Nullable PsiElement directory, @NotNull List<String> arguments) {
            this.arguments    = String.join(", ", arguments);
            this.resource     = arguments.get(0);
            this.withVariable = !(directory instanceof Variable) && !(directory instanceof StringLiteralExpression);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null && !project.isDisposed()) {
                final String code;
                if (this.withVariable) {
                    final String throwPart = "throw new \\RuntimeException(sprintf('Directory \"%s\" was not created', $concurrentDirectory));";
                    final String pattern   = "if (!is_dir($concurrentDirectory = %s) && !mkdir($concurrentDirectory)) { %s }";
                    code                   = String.format(pattern, this.arguments, throwPart);
                } else {
                    final String throwPart = "throw new \\RuntimeException(sprintf('Directory \"%%s\" was not created', %s));";
                    final String pattern   = "if (!is_dir(%s) && !mkdir(%s)) { %s }";
                    code                   = String.format(pattern, this.arguments, this.resource, String.format(throwPart, this.resource));
                }
                target.replace(PhpPsiElementFactory.createPhpPsiFromText(project, If.class, code));
            }
        }
    }

    private static final class HardenConditionFix implements LocalQuickFix {
        private static final String title = "Harden the condition";

        private final String resource;
        private final String arguments;
        private final boolean withVariable;
        private final boolean isInverted;

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        HardenConditionFix(@Nullable PsiElement directory, @NotNull List<String> arguments, boolean isInverted) {
            this.arguments    = String.join(", ", arguments);
            this.resource     = arguments.get(0);
            this.withVariable = !(directory instanceof Variable) && !(directory instanceof StringLiteralExpression);
            this.isInverted   = isInverted;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null && !project.isDisposed()) {
                final PsiElement parent = target.getParent();
                if (parent instanceof If) {
                    final String code;
                    if (this.withVariable) {
                        if (this.isInverted) {
                            code = String.format("(!is_dir($concurrentDirectory = %s) && !mkdir($concurrentDirectory))", this.arguments);
                        } else {
                            code = String.format("(is_dir($concurrentDirectory = %s) || !mkdir($concurrentDirectory)", this.arguments);
                        }
                    } else {
                        if (this.isInverted) {
                            code = String.format("(!is_dir(%s) && !mkdir(%s))", this.arguments, this.resource);
                        } else {
                            code = String.format("(is_dir(%s) || mkdir(%s))", this.arguments, this.resource);
                        }
                    }
                    target.replace(PhpPsiElementFactory.createPhpPsiFromText(project, ParenthesizedExpression.class, code).getArgument());
                } else if (parent instanceof BinaryExpression) {
                    final BinaryExpression binary = (BinaryExpression) parent;
                    final IElementType operation  = binary.getOperationType();
                    final String conditions       = binary.getLeftOperand().getText();
                    if (PhpTokenTypes.tsSHORT_CIRCUIT_AND_OPS.contains(operation)) {
                        final String code;
                        if (this.withVariable) {
                            code = String.format("(%s && !is_dir($concurrentDirectory = %s) && !mkdir($concurrentDirectory))", conditions, this.arguments);
                        } else {
                            code = String.format("(%s && !is_dir(%s) && !mkdir(%s))", conditions, this.arguments, this.resource);
                        }
                        parent.replace(PhpPsiElementFactory.createPhpPsiFromText(project, ParenthesizedExpression.class, code).getArgument());
                    } else if (PhpTokenTypes.tsSHORT_CIRCUIT_OR_OPS.contains(operation)) {
                        final String code;
                        if (this.withVariable) {
                            code = String.format("(%s || is_dir($concurrentDirectory = %s) || mkdir($concurrentDirectory))", conditions, this.arguments);
                        } else {
                            code = String.format("(%s || is_dir(%s) || mkdir(%s))", conditions, this.arguments, this.resource);
                        }
                        parent.replace(PhpPsiElementFactory.createPhpPsiFromText(project, ParenthesizedExpression.class, code).getArgument());
                    }
                }
            }
        }
    }
}
