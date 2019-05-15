package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocVariable;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class OneTimeUseVariablesInspector extends PhpInspection {
    // Inspection options.
    public boolean ALLOW_LONG_STATEMENTS = false;

    private static final String messagePattern = "Variable $%s is redundant.";
    private static final String messageRename  = "The local variable introduction doesn't make much sense here, consider renaming a loop variable instead.";

    @NotNull
    public String getShortName() {
        return "OneTimeUseVariablesInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {

            private void checkOneTimeUse(
                    @NotNull PhpPsiElement construct,
                    @NotNull Variable argument,
                    @Nullable Function scope
            ) {
                /* false-negatives: php-doc should not stop the analysis */
                PhpPsiElement previous = construct.getPrevPsiSibling();
                while (previous instanceof PhpDocComment) {
                    previous = previous.getPrevPsiSibling();
                }

                if (previous != null && OpenapiTypesUtil.isAssignment(previous.getFirstChild())) {
                    final AssignmentExpression assign = (AssignmentExpression) previous.getFirstChild();

                    /* ensure variables are the same */
                    final PhpPsiElement assignVariable = assign.getVariable();
                    final PsiElement value             = ExpressionSemanticUtil.getExpressionTroughParenthesis(assign.getValue());
                    if (value != null && assignVariable instanceof Variable) {
                        final String variableName       = argument.getName();
                        final String assignVariableName = assignVariable.getName();
                        if (assignVariableName == null || !assignVariableName.equals(variableName)) {
                            return;
                        }

                        /* too long return/throw statements can be decoupled as a variable */
                        if (!ALLOW_LONG_STATEMENTS && assign.getTextLength() > 80) {
                            return;
                        }

                        if (construct instanceof ForeachStatement) {
                            /* do not suggest inlining require, ternaries and type-specified variables */
                            if (value instanceof Include || value instanceof TernaryExpression || this.isTypeAnnotated(previous, variableName)) {
                                return;
                            }
                            /* inlining is not possible when foreach value is a reference before PHP 5.5 */
                            final PsiElement targetContainer = ((ForeachStatement) construct).getValue();
                            if (targetContainer != null) {
                                PsiElement referenceCandidate = targetContainer.getPrevSibling();
                                if (referenceCandidate instanceof PsiWhiteSpace) {
                                    referenceCandidate = referenceCandidate.getPrevSibling();
                                }
                                if (
                                    OpenapiTypesUtil.is(referenceCandidate, PhpTokenTypes.opBIT_AND) &&
                                    PhpLanguageLevel.get(holder.getProject()).below(PhpLanguageLevel.PHP550)
                                ) {
                                    return;
                                }
                            }
                        }

                        if (scope != null) {
                            /* check if variable as a function/use(...) parameter by reference */
                            final boolean isReference = this.isArgumentReference(argument, scope) ||
                                                        this.isBoundReference(argument, scope);
                            if (isReference) {
                                return;
                            }

                            /* find usage inside function/method to analyze multiple writes */
                            final PhpAccessVariableInstruction[] usages = PhpControlFlowUtil.getFollowingVariableAccessInstructions(
                                    scope.getControlFlow().getEntryPoint(),
                                    variableName,
                                    false
                            );
                            int countWrites = 0;
                            int countReads  = 0;
                            for (final PhpAccessVariableInstruction oneCase: usages) {
                                final boolean isWrite = oneCase.getAccess().isWrite();
                                if (isWrite) {
                                    /* false-positives: type specification */
                                    final PsiElement context = oneCase.getAnchor().getParent();
                                    if (OpenapiTypesUtil.isAssignment(context)) {
                                        final boolean typeAnnotated = this.isTypeAnnotated((PhpPsiElement) context.getParent(), variableName);
                                        if (typeAnnotated) {
                                            return;
                                        }
                                    }
                                }

                                countWrites += isWrite ? 1 : 0;
                                countReads  += isWrite ? 0 : 1;
                                if (countWrites > 1 || countReads > 1) {
                                    return;
                                }
                            }
                        }

                        if (!(value instanceof NewExpression) || PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP540)) {
                            holder.registerProblem(
                                    assignVariable,
                                    String.format(messagePattern, variableName),
                                    new InlineValueFix(assign.getParent(), argument, value)
                            );
                        }
                    }
                }
            }

            private void checkInline(@NotNull PhpPsiElement construct, @NotNull Variable argument) {
                /* false-negatives: php-doc should not stop the analysis */
                PhpPsiElement previous = construct.getPrevPsiSibling();
                while (previous instanceof PhpDocComment) {
                    previous = previous.getPrevPsiSibling();
                }

                if (previous != null && OpenapiTypesUtil.isAssignment(previous.getFirstChild())) {
                    final AssignmentExpression assign = (AssignmentExpression) previous.getFirstChild();
                    final PsiElement container        = assign.getVariable();
                    final PsiElement value            = assign.getValue();
                    if (value != null && container instanceof Variable) {
                        final boolean canInline = OpenapiEquivalenceUtil.areEqual(container, argument);
                        if (canInline) {
                            holder.registerProblem(
                                    container,
                                    String.format(messagePattern, ((Variable) container).getName()),
                                    new InlineValueFix(assign.getParent(), argument, value)
                            );
                        }
                    }
                }
            }

            @Override
            public void visitPhpReturn(@NotNull PhpReturn expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                /* if function returning reference, do not inspect returns */
                final Function scope = ExpressionSemanticUtil.getScope(expression);
                if (scope != null) {
                    final PsiElement nameNode = NamedElementUtil.getNameIdentifier(scope);
                    if (nameNode != null) {
                        /* is defined like returning reference */
                        PsiElement referenceCandidate = nameNode.getPrevSibling();
                        if (referenceCandidate instanceof PsiWhiteSpace) {
                            referenceCandidate = referenceCandidate.getPrevSibling();
                        }
                        if (OpenapiTypesUtil.is(referenceCandidate, PhpTokenTypes.opBIT_AND)) {
                            return;
                        }
                    }
                }

                /* regular function, check one-time use variables */
                final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getArgument());
                if (argument instanceof PhpPsiElement) {
                    final Variable variable = this.getVariable(argument);
                    if (variable != null) {
                        this.checkOneTimeUse(expression, variable, scope);
                    }
                }
            }

            @Override
            public void visitPhpMultiassignmentExpression(@NotNull MultiassignmentExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                final PsiElement parent = expression.getParent();
                if (OpenapiTypesUtil.isStatementImpl(parent)) {
                    final Function scope = ExpressionSemanticUtil.getScope(expression);
                    if (scope != null) {
                        final PsiElement value  = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getValue());
                        final Variable variable = value == null ? null : this.getVariable(value);
                        if (variable != null) {
                            final PsiElement first = expression.getFirstChild();
                            final boolean isTarget = OpenapiTypesUtil.is(first, PhpTokenTypes.kwLIST) ||
                                                     OpenapiTypesUtil.is(first, PhpTokenTypes.chLBRACKET);
                            if (isTarget) {
                                this.checkOneTimeUse((PhpPsiElement) parent, variable, scope);
                            }
                        }
                    }
                }
            }

            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                if (OpenapiTypesUtil.isAssignment(expression)) {
                    final PsiElement parent = expression.getParent();
                    if (OpenapiTypesUtil.isStatementImpl(parent)) {
                        final Function scope = ExpressionSemanticUtil.getScope(expression);
                        if (scope != null) {
                            final PsiElement value = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getValue());
                            if (value instanceof BinaryExpression) {
                                // $variable = ...; $variable = $variable ?? ...;
                                final BinaryExpression binary = (BinaryExpression) value;
                                if (binary.getOperationType() == PhpTokenTypes.opCOALESCE) {
                                    final PsiElement left      = binary.getLeftOperand();
                                    final PsiElement container = expression.getVariable();
                                    if (container instanceof Variable && left instanceof Variable) {
                                        final PsiElement alternative = binary.getRightOperand();
                                        final Variable placeholder   = (Variable) left;
                                        if (alternative != null && OpenapiEquivalenceUtil.areEqual(container, placeholder)) {
                                            this.checkInline((PhpPsiElement) parent, placeholder);
                                        }
                                    }
                                }
                            } else if (value instanceof TernaryExpression) {
                                // $variable = ...; $variable = $variable ?: ...;
                                final TernaryExpression ternary = (TernaryExpression) value;
                                if (ternary.isShort()) {
                                    final PsiElement left      = ternary.getCondition();
                                    final PsiElement container = expression.getVariable();
                                    if (container instanceof Variable && left instanceof Variable) {
                                        final PsiElement alternative = ternary.getFalseVariant();
                                        final Variable placeholder   = (Variable) left;
                                        if (alternative != null && OpenapiEquivalenceUtil.areEqual(container, placeholder)) {
                                            this.checkInline((PhpPsiElement) parent, placeholder);
                                        }
                                    }
                                }
                            } else if (value != null) {
                                final Variable variable = this.getVariable(value);
                                if (variable != null) {
                                    this.checkOneTimeUse((PhpPsiElement) parent, variable, scope);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void visitPhpThrow(@NotNull PhpThrow expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getArgument());
                if (argument instanceof PhpPsiElement) {
                    final Variable variable = this.getVariable(argument);
                    if (variable != null) {
                        this.checkOneTimeUse(expression, variable, ExpressionSemanticUtil.getScope(expression));
                    }
                }
            }

            @Override
            public void visitPhpEchoStatement(@NotNull PhpEchoStatement expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                final PsiElement[] arguments = expression.getArguments();
                if (arguments.length == 1) {
                    final Function scope = ExpressionSemanticUtil.getScope(expression);
                    if (scope != null) {
                        final Variable variable = this.getVariable(arguments[0]);
                        if (variable != null) {
                            this.checkOneTimeUse(expression, variable,scope);
                        }
                    }
                }
            }

            @Override
            public void visitPhpForeach(@NotNull ForeachStatement expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                final PsiElement source = expression.getArray();
                if (source != null && !ExpressionSemanticUtil.isByReference(expression.getValue())) {
                    final Function scope = ExpressionSemanticUtil.getScope(expression);
                    if (scope != null) {
                        final Variable extractedSource = this.getVariable(source);
                        if (extractedSource != null) {
                            this.checkOneTimeUse(expression, extractedSource, scope);
                        }
                        Stream.of(expression.getKey(), expression.getValue())
                                .filter(Objects::nonNull)
                                .forEach(variable -> this.checkIfCanRename(expression, variable, scope));
                    }
                }
            }

            private void checkIfCanRename(
                    @NotNull ForeachStatement expression,
                    @NotNull Variable subject,
                    @NotNull Function scope
            ) {
                final GroupStatement loopBody = ExpressionSemanticUtil.getGroupStatement(expression);
                if (loopBody != null && ExpressionSemanticUtil.countExpressionsInGroup(loopBody) > 1) {
                    AssignmentExpression targetAssignment = null;
                    Variable targetVariable               = null;

                    /* find assignments directly in body, with subject as value */
                    for (final PsiElement child: loopBody.getChildren()) {
                        final PsiElement first = child.getFirstChild();
                        if (OpenapiTypesUtil.isAssignment(first)) {
                            final AssignmentExpression assignment = (AssignmentExpression) first;
                            final PsiElement container            = assignment.getVariable();
                            if (container instanceof Variable) {
                                final PsiElement value = assignment.getValue();
                                if (value instanceof Variable && OpenapiEquivalenceUtil.areEqual(subject, value)) {
                                    targetAssignment = assignment;
                                    targetVariable   = (Variable) container;
                                    break;
                                }
                            }
                        }
                    }

                    if (targetAssignment != null) {
                        /* check subject usage: subject value stored in a local variable, which is used instead */
                        final long subjectUsages = PsiTreeUtil.findChildrenOfType(loopBody, Variable.class).stream().filter(v -> OpenapiEquivalenceUtil.areEqual(v, subject)).count();
                        if (subjectUsages == 1) {
                            final GroupStatement scopeBody = ExpressionSemanticUtil.getGroupStatement(scope);
                            if (scopeBody != null) {
                                final Variable target        = targetVariable;
                                final long targetUsageInLoop = PsiTreeUtil.findChildrenOfType(loopBody, Variable.class).stream().filter(v -> OpenapiEquivalenceUtil.areEqual(v, target)).count();
                                if (targetUsageInLoop > 1) {
                                    final long targetUsageInScope = PsiTreeUtil.findChildrenOfType(scopeBody, Variable.class).stream().filter(v -> OpenapiEquivalenceUtil.areEqual(v, target)).count();
                                    if (targetUsageInLoop == targetUsageInScope) {
                                        holder.registerProblem(
                                                target,
                                                messageRename,
                                                new RenameLoopVariableFix(targetAssignment, subject, targetVariable)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Nullable
            private Variable getVariable(@NotNull PsiElement expression) {
                Variable result = null;
                if (expression instanceof Variable) {
                    result = (Variable) expression;
                } else if (expression instanceof FieldReference) {
                    final PsiElement candidate = expression.getFirstChild();
                    if (candidate instanceof Variable) {
                        result = (Variable) candidate;
                    }
                } else if (expression instanceof MethodReference) {
                    PsiElement candidate = expression.getFirstChild();
                    while (candidate instanceof MethodReference) {
                        candidate = candidate.getFirstChild();
                    }
                    if (candidate instanceof Variable) {
                        final Variable variable   = (Variable) candidate;
                        final String variableName = variable.getName();
                        final long variableUsages = PsiTreeUtil.findChildrenOfType(expression, Variable.class).stream()
                                .filter(v -> v == variable || v.getName().equals(variableName))
                                .count();
                        if (variableUsages == 1) {
                            result = variable;
                        }
                    }
                } else if (OpenapiTypesUtil.isPhpExpressionImpl(expression)) {
                    /* instanceof passes child classes as well, what isn't correct */
                    final PsiElement candidate = expression.getFirstChild();
                    if (candidate != null) {
                        result = this.getVariable(candidate);
                    }
                }
                return result;
            }

            private boolean isArgumentReference(@NotNull Variable variable, @NotNull Function function) {
                boolean result            = false;
                final String variableName = variable.getName();
                for (final Parameter parameter : function.getParameters()) {
                    if (parameter.getName().equals(variableName) && parameter.isPassByRef()) {
                        result = true;
                        break;
                    }
                }
                return result;
            }

            private boolean isBoundReference(@NotNull Variable variable, @NotNull Function function) {
                boolean result            = false;
                final List<Variable> used = ExpressionSemanticUtil.getUseListVariables(function);
                if (used != null) {
                    final String variableName      = variable.getName();
                    final Optional<Variable> match = used.stream().filter(v -> v.getName().equals(variableName)).findFirst();
                    if (match.isPresent()) {
                        final PsiElement previous  = match.get().getPrevSibling();
                        final PsiElement candidate = previous instanceof PsiWhiteSpace ? previous.getPrevSibling() : previous;
                        result                     = OpenapiTypesUtil.is(candidate, PhpTokenTypes.opBIT_AND);
                    }
                    used.clear();
                }
                return result;
            }

            private boolean isTypeAnnotated(@NotNull PhpPsiElement current, @NotNull String variableName) {
                boolean result = false;
                final PsiElement phpdocCandidate = current.getPrevPsiSibling();
                if (phpdocCandidate instanceof PhpDocComment) {
                    final PhpDocTag[] hints = ((PhpDocComment) phpdocCandidate).getTagElementsByName("@var");
                    if (hints.length == 1) {
                        final PhpDocVariable specifiedVariable = PsiTreeUtil.findChildOfType(hints[0], PhpDocVariable.class);
                        result = specifiedVariable != null && specifiedVariable.getName().equals(variableName);
                    }
                }
                return result;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addCheckbox("Allow long statements (80+ chars)", ALLOW_LONG_STATEMENTS, (isSelected) -> ALLOW_LONG_STATEMENTS = isSelected)
        );
    }

    private static final class RenameLoopVariableFix implements LocalQuickFix {
        private static final String title = "Rename the loop variable accordingly";

        private final SmartPsiElementPointer<PsiElement> assignment;
        private final SmartPsiElementPointer<Variable> variable;
        private final SmartPsiElementPointer<Variable> subject;

        RenameLoopVariableFix(@NotNull PsiElement assignment, @NotNull Variable subject, @NotNull Variable variable) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(variable.getProject());

            this.assignment = factory.createSmartPsiElementPointer(assignment);
            this.subject    = factory.createSmartPsiElementPointer(subject);
            this.variable   = factory.createSmartPsiElementPointer(variable);
        }

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement assignment = this.assignment.getElement();
            final Variable subject      = this.subject.getElement();
            final Variable variable     = this.variable.getElement();
            if (assignment != null && subject != null && variable != null && !project.isDisposed()) {
                subject.handleElementRename(variable.getName());
                assignment.getParent().delete();
            }
        }
    }

    private static final class InlineValueFix implements LocalQuickFix {
        private static final String title = "Inline value";

        private final SmartPsiElementPointer<PsiElement> assignment;
        private final SmartPsiElementPointer<PsiElement> value;
        private final SmartPsiElementPointer<Variable> variable;

        InlineValueFix(@NotNull PsiElement assignment, @NotNull Variable variable, @NotNull PsiElement value) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(value.getProject());

            this.assignment = factory.createSmartPsiElementPointer(assignment);
            this.variable   = factory.createSmartPsiElementPointer(variable);
            this.value      = factory.createSmartPsiElementPointer(value);
        }

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement assignment = this.assignment.getElement();
            final Variable variable     = this.variable.getElement();
            final PsiElement value      = this.value.getElement();
            if (assignment == null || variable == null || value == null || project.isDisposed()) {
                return;
            }

            /* delete preceding PhpDoc */
            final PhpPsiElement previous = ((Statement) assignment).getPrevPsiSibling();
            if (previous instanceof PhpDocComment) {
                previous.delete();
            }

            /* delete space after the method */
            PsiElement nextExpression = assignment.getNextSibling();
            if (nextExpression instanceof PsiWhiteSpace) {
                nextExpression.delete();
            }

            /* inline the value */
            boolean wrap = false;
            if (value instanceof NewExpression) {
                wrap = true;
            } else if (value instanceof UnaryExpression) {
                wrap = OpenapiTypesUtil.is(((UnaryExpression) value).getOperation(), PhpTokenTypes.kwCLONE);
            }

            if (wrap && variable.getParent() instanceof MemberReference) {
                final String wrappedPattern = '(' + value.getText() + ')';
                final ParenthesizedExpression wrapped
                    = PhpPsiElementFactory.createPhpPsiFromText(project, ParenthesizedExpression.class, wrappedPattern);
                variable.replace(wrapped);
            } else {
                variable.replace(value);
            }

            /* delete assignment itself */
            assignment.delete();
        }
    }
}

