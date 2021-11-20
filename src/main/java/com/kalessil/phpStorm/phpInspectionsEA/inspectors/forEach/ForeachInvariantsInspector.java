package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ForeachInvariantsInspector extends BasePhpInspection {
    private static final String foreachInvariant = "Foreach can probably be used instead (easier to read and support).";
    private static final String eachFunctionUsed = "Foreach should be used instead (8x faster, also deprecated since PHP 7.2).";

    @NotNull
    @Override
    public String getShortName() {
        return "ForeachInvariantsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Foreach usage possible";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFor(@NotNull For expression) {
                if (expression.getRepeatedExpressions().length == 1) {
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(expression);
                    if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) > 0) {
                        final PsiElement indexVariable = this.getCounterVariable(expression);
                        if (indexVariable != null && this.isCounterVariableIncremented(expression, indexVariable)) {
                            final PsiElement limit = this.getLoopLimit(expression, indexVariable);
                            if (limit != null) {
                                final PsiElement container = this.getContainerByIndex(body, indexVariable);
                                if (container != null && this.isLimitFor(limit, container)) {
                                        holder.registerProblem(
                                                expression.getFirstChild(),
                                                MessagesPresentationUtil.prefixWithEa(foreachInvariant),
                                                new UseForeachFix(holder.getProject(), expression, indexVariable, null, container, limit)
                                        );
                                }
                            }
                        }
                    }
                }
            }

            private boolean isLimitFor(@NotNull PsiElement limit, @NotNull PsiElement container) {
                boolean result               = false;
                final Set<PsiElement> values = PossibleValuesDiscoveryUtil.discover(limit);
                if (values.size() == 1) {
                    final PsiElement value = values.iterator().next();
                    if (OpenapiTypesUtil.isFunctionReference(value)) {
                        final FunctionReference reference = (FunctionReference) value;
                        final String functionName         = reference.getName();
                        if (functionName != null && functionName.equals("count")) {
                            final PsiElement[] arguments = reference.getParameters();
                            result = arguments.length == 1 && OpenapiEquivalenceUtil.areEqual(arguments[0], container);
                        }
                    }
                }
                values.clear();
                return result;
            }

            @Override
            public void visitPhpMultiassignmentExpression(@NotNull MultiassignmentExpression assignmentExpression) {
                PsiElement value = assignmentExpression.getValue();
                if (OpenapiTypesUtil.isPhpExpressionImpl(value)) {
                    value = value.getFirstChild();
                }

                if (OpenapiTypesUtil.isFunctionReference(value)) {
                    final FunctionReference each = (FunctionReference) value;
                    final String functionName    = (each).getName();
                    final PsiElement[] arguments = each.getParameters();
                    if (arguments.length == 1 && functionName != null && functionName.equals("each")) {
                        final PsiElement parent = assignmentExpression.getParent();
                        if (parent instanceof While || parent instanceof For) {
                            final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(parent);
                            if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) > 0) {
                                UseForeachFix fixer = null;
                                if (parent instanceof While) {
                                    final List<PhpPsiElement> variables = assignmentExpression.getVariables();
                                    if (variables.size() == 2) {
                                        fixer = new UseForeachFix(holder.getProject(), parent, variables.get(0), variables.get(1), arguments[0], null);
                                    }
                                }

                                final PsiElement container    = arguments[0];
                                final boolean isContainerUsed = PsiTreeUtil.findChildrenOfType(body, container.getClass()).stream()
                                        .anyMatch(candidate -> OpenapiEquivalenceUtil.areEqual(candidate, container));
                                if (!isContainerUsed) {
                                    holder.registerProblem(
                                            parent.getFirstChild(),
                                            MessagesPresentationUtil.prefixWithEa(eachFunctionUsed),
                                            ProblemHighlightType.GENERIC_ERROR,
                                            fixer
                                    );
                                }
                            }
                        }
                    }
                }
            }

            @Nullable
            private PsiElement getCounterVariable(@NotNull For expression) {
                PsiElement result = null;
                for (final PhpPsiElement init : expression.getInitialExpressions()) {
                    if (OpenapiTypesUtil.isAssignment(init)) {
                        final AssignmentExpression assignment = (AssignmentExpression) init;
                        final PsiElement value                = assignment.getValue();
                        final PsiElement variable             = assignment.getVariable();
                        if (value != null && variable instanceof Variable && value.getText().equals("0")) {
                            result = variable;
                            break;
                        }
                    }
                }
                return result;
            }

            private boolean isCounterVariableIncremented(@NotNull For expression, @NotNull PsiElement variable) {
                boolean result = false;
                for (final PsiElement repeat : expression.getRepeatedExpressions()) {
                    if (repeat instanceof UnaryExpression) {
                        final UnaryExpression incrementCandidate = (UnaryExpression) repeat;
                        final PsiElement argument                = incrementCandidate.getValue();
                        if (
                            OpenapiTypesUtil.is(incrementCandidate.getOperation(), PhpTokenTypes.opINCREMENT) &&
                            argument != null && OpenapiEquivalenceUtil.areEqual(variable, argument)
                        ) {
                            result = true;
                            break;
                        }
                    }
                }
                return result;
            }

            private PsiElement getContainerByIndex(@NotNull GroupStatement body, @NotNull PsiElement variable) {
                final Map<String, PsiElement> containers = new HashMap<>();
                for (final ArrayAccessExpression offset : PsiTreeUtil.findChildrenOfType(body, ArrayAccessExpression.class)) {
                    final ArrayIndex index = offset.getIndex();
                    final PsiElement value = index == null ? null : index.getValue();
                    if (value instanceof Variable && OpenapiEquivalenceUtil.areEqual(variable, value)) {
                        final PsiElement container = offset.getValue();
                        if (container != null) {
                            containers.put(container.getText(), container);
                            if (containers.size() > 1) {
                                break;
                            }
                        }
                    }
                }
                final PsiElement result = containers.size() == 1 ? containers.values().iterator().next(): null;
                containers.clear();
                return result;
            }

            @Nullable
            private PsiElement getLoopLimit(@NotNull For expression, @NotNull PsiElement variable) {
                final PsiElement[] conditions = expression.getConditionalExpressions();
                if (conditions.length == 1) {
                    for (final PsiElement check : conditions) {
                        if (check instanceof BinaryExpression) {
                            final BinaryExpression condition = (BinaryExpression) check;
                            final PsiElement left            = condition.getLeftOperand();
                            final PsiElement right           = condition.getRightOperand();
                            if (left != null && right != null) {
                                final PsiElement value;
                                if (left instanceof Variable && OpenapiEquivalenceUtil.areEqual(variable, left)) {
                                    value = right;
                                } else if (right instanceof Variable && OpenapiEquivalenceUtil.areEqual(variable, right)) {
                                    value = left;
                                } else {
                                    value = null;
                                }
                                return value;
                            }
                        }
                    }
                }
                return null;
            }
        };
    }

    private static final class UseForeachFix implements LocalQuickFix {
        private static final String title = "Use foreach instead";

        @NotNull
        private final SmartPsiElementPointer<PsiElement> loop;
        @NotNull
        private final SmartPsiElementPointer<PsiElement> index;
        @Nullable
        private final SmartPsiElementPointer<PsiElement> value;
        @NotNull
        private final SmartPsiElementPointer<PsiElement> container;
        @Nullable
        private final SmartPsiElementPointer<PsiElement> limit;

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

        UseForeachFix(
            @NotNull Project project,
            @NotNull PsiElement loop,
            @NotNull PsiElement index,
            @Nullable PsiElement value,
            @NotNull PsiElement container,
            @Nullable PsiElement limit
        ) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(project);

            this.loop      = factory.createSmartPsiElementPointer(loop);
            this.index     = factory.createSmartPsiElementPointer(index);
            this.value     = value == null ? null : factory.createSmartPsiElementPointer(value);
            this.container = factory.createSmartPsiElementPointer(container);
            this.limit     = limit == null ? null : factory.createSmartPsiElementPointer(limit);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement loop      = this.loop.getElement();
            final PsiElement index     = this.index.getElement();
            final PsiElement container = this.container.getElement();
            if (loop != null && index != null && container != null && !project.isDisposed()) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(loop);
                if (body != null) {
                    final PsiElement value = this.value == null ? null : this.value.getElement();
                    final String pattern   = "foreach (%c% as %i% => %i%Value) {}"
                            .replace("%i%Value", value == null ? "%i%Value" : value.getText())
                            .replace("%i%", index.getText())
                            .replace("%i%", index.getText())
                            .replace("%c%", container.getText());
                    final ForeachStatement replacement    = PhpPsiElementFactory.createPhpPsiFromText(project, ForeachStatement.class, pattern);
                    final PsiElement replacementContainer = replacement.getValue();
                    final GroupStatement bodyHolder       = ExpressionSemanticUtil.getGroupStatement(replacement);
                    if (bodyHolder != null && replacementContainer != null) {
                        final Function scope   = ExpressionSemanticUtil.getScope(loop);
                        final PsiElement limit = this.limit == null ? null : this.limit.getElement();
                        this.updateContainersUsage(body, container, index, replacementContainer);
                        bodyHolder.replace(body);
                        this.cleanupUnusedIndex(replacement, body);
                        loop.replace(replacement);
                        this.cleanupUnusedLimit(scope, limit);
                    }
                }
            }
        }

        private void cleanupUnusedLimit(@Nullable Function scope, @Nullable PsiElement limit) {
            if (limit != null && scope != null) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(scope);
                if (body != null) {
                    final List<PsiElement> matches = PsiTreeUtil.findChildrenOfType(body, limit.getClass()).stream()
                            .filter(c -> OpenapiEquivalenceUtil.areEqual(c, limit))
                            .collect(Collectors.toList());
                    if (matches.size() == 1) {
                        final PsiElement match  = matches.get(0);
                        final PsiElement parent = match.getParent();
                        if (OpenapiTypesUtil.isAssignment(parent)) {
                            final PsiElement grandParent          = parent.getParent();
                            final AssignmentExpression assignment = (AssignmentExpression) parent;
                            if (assignment.getVariable() == match && OpenapiTypesUtil.isStatementImpl(grandParent)) {
                                grandParent.delete();
                            }
                        }
                    }
                    matches.clear();
                }
            }
        }

        private void cleanupUnusedIndex(@NotNull ForeachStatement loop, @NotNull GroupStatement body) {
            final Variable index   = loop.getKey();
            final Variable value   = loop.getValue();
            final String indexName = index == null ? null : index.getName();
            if (indexName != null && value != null) {
                final long usagesCount = PsiTreeUtil.findChildrenOfType(body, Variable.class).stream()
                        .filter(variable -> indexName.equals(variable.getName()))
                        .count();
                if (usagesCount == 0) {
                    index.getParent().deleteChildRange(index, value.getPrevSibling());
                }
            }
        }

        private void updateContainersUsage(
            @NotNull GroupStatement body,
            @NotNull PsiElement container,
            @NotNull PsiElement index,
            @NotNull PsiElement replacement
        ) {
            PsiTreeUtil.findChildrenOfType(body, ArrayAccessExpression.class).stream()
                .filter(offset  -> {
                    final PsiElement parent = offset.getParent();
                    if (
                        parent instanceof MemberReference || parent instanceof BinaryExpression ||
                        parent instanceof UnaryExpression || parent instanceof ParenthesizedExpression ||
                        parent instanceof ArrayIndex      || parent instanceof PhpEchoStatement ||
                        parent instanceof If              || parent instanceof ElseIf ||
                        parent instanceof PhpSwitch       || parent instanceof PhpCase ||
                        (parent instanceof Variable && parent.getParent() instanceof StringLiteralExpression)
                    ) {
                        return true;
                    } else if (parent instanceof ParameterList) {
                        final PsiElement grandParent = parent.getParent();
                        if (grandParent instanceof FunctionReference) {
                            final PsiElement resolved = OpenapiResolveUtil.resolveReference((FunctionReference) grandParent);
                            return resolved instanceof Function &&
                                   Arrays.stream(((Function) resolved).getParameters()).noneMatch(Parameter::isPassByRef);
                        }
                    } else if (parent instanceof AssignmentExpression) {
                        /* assignments, but not by reference */
                        final AssignmentExpression assignment = (AssignmentExpression) parent;
                        if (assignment.getValue() == offset) {
                            return !OpenapiTypesUtil.isAssignmentByReference(assignment);
                        }
                    } else if (parent instanceof ArrayAccessExpression) {
                        final ArrayAccessExpression access = (ArrayAccessExpression) parent;
                        if (access.getValue() == offset) {
                            PsiElement context = access;
                            while (context instanceof ArrayAccessExpression) {
                                context = context.getParent();
                            }
                            return !(context instanceof AssignmentExpression);
                        }
                    } else if (OpenapiTypesUtil.isLoop(parent)) {
                        return true;
                    }

                    return false;
                }).forEach(offset -> {
                    final ArrayIndex offsetIndex   = offset.getIndex();
                    final PsiElement usedIndex     = offsetIndex == null ? null : offsetIndex.getValue();
                    final PsiElement usedContainer = offset.getValue();
                    if (usedIndex != null && usedContainer != null) {
                        final boolean replace = OpenapiEquivalenceUtil.areEqual(index, usedIndex) &&
                                                OpenapiEquivalenceUtil.areEqual(container, usedContainer);
                        if (replace) {
                            final PsiElement parent = offset.getParent();
                            // PhpStorm backward compatibility: 2018.*+ the fixed tree structure differs
                            (parent instanceof Variable ? parent : offset).replace(replacement);
                        }
                    }
                });
        }
    }
}