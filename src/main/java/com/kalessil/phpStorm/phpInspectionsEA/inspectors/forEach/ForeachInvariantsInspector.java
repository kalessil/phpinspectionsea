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
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
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

public class ForeachInvariantsInspector extends PhpInspection {
    private static final String foreachInvariant = "Foreach can probably be used instead (easier to read and support).";
    private static final String eachFunctionUsed = "Foreach should be used instead (8x faster, also deprecated since PHP 7.2).";

    @NotNull
    public String getShortName() {
        return "ForeachInvariantsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFor(@NotNull For expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

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
                                                foreachInvariant,
                                                new UseForeachFix(expression, indexVariable, null, container, limit)
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
            public void visitPhpWhile(@NotNull While whileStatement) {
                if (this.shouldSkipAnalysis(whileStatement, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(whileStatement);
                if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) > 0) {
                    final AssignmentExpression assignment = this.getValueExtraction(whileStatement.getCondition());
                    if (assignment != null) {
                        final FunctionReference reference = (FunctionReference) assignment.getValue();
                        if (reference != null) {
                            final PsiElement container    = reference.getParameters()[0];
                            final boolean isContainerUsed = PsiTreeUtil.findChildrenOfType(body, container.getClass()).stream()
                                    .anyMatch(candidate -> OpenapiEquivalenceUtil.areEqual(candidate, container));
                            if (!isContainerUsed) {
                                holder.registerProblem(
                                        whileStatement.getFirstChild(),
                                        foreachInvariant,
                                        new UseForeachFix(whileStatement, null, assignment.getVariable(), container, null)
                                );
                            }
                        }
                    }
                }
            }

            @Nullable
            private AssignmentExpression getValueExtraction(@Nullable PsiElement condition) {
                PsiElement current = ExpressionSemanticUtil.getExpressionTroughParenthesis(condition);
                if (current != null) {
                    /* [$array &&] [null !=[=]] $value = array_shift($array) */
                    PsiElement source = null;
                    /* filter source check */
                    if (current instanceof BinaryExpression) {
                        final BinaryExpression binary = (BinaryExpression) current;
                        if (binary.getOperationType() == PhpTokenTypes.opAND) {
                            source  = ExpressionSemanticUtil.getExpressionTroughParenthesis(binary.getLeftOperand());
                            current = ExpressionSemanticUtil.getExpressionTroughParenthesis(binary.getRightOperand());
                        }
                    }
                    /* filter null comparison */
                    if (current instanceof BinaryExpression) {
                        final BinaryExpression binary = (BinaryExpression) current;
                        final IElementType operation  = binary.getOperationType();
                        if (operation == PhpTokenTypes.opNOT_IDENTICAL || operation == PhpTokenTypes.opNOT_EQUAL) {
                            final PsiElement left  = binary.getLeftOperand();
                            final PsiElement right = binary.getRightOperand();
                            if (PhpLanguageUtil.isNull(right) || PhpLanguageUtil.isNull(left)) {
                                current = ExpressionSemanticUtil.getExpressionTroughParenthesis(PhpLanguageUtil.isNull(right) ? left : right);
                            }
                        }
                    }
                    /* check for the assignment */
                    if (current != null && OpenapiTypesUtil.isAssignment(current)) {
                        final AssignmentExpression assignment = (AssignmentExpression) current;
                        final PsiElement value                = assignment.getValue();
                        if (OpenapiTypesUtil.isFunctionReference(value)) {
                            final FunctionReference reference = (FunctionReference) value;
                            final String functionName         = reference.getName();
                            if (functionName != null && functionName.equals("array_shift")) {
                                final PsiElement[] arguments = reference.getParameters();
                                if (arguments.length == 1) {
                                    final boolean sameSource = source == null || OpenapiEquivalenceUtil.areEqual(source, arguments[0]);
                                    if (sameSource) {
                                        return assignment;
                                    }
                                }
                            }
                        }
                    }
                }
                return null;
            }

            @Override
            public void visitPhpMultiassignmentExpression(@NotNull MultiassignmentExpression assignmentExpression) {
                if (this.shouldSkipAnalysis(assignmentExpression, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

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
                                        fixer = new UseForeachFix(parent, variables.get(0), variables.get(1), arguments[0], null);
                                    }
                                }

                                final PsiElement container    = arguments[0];
                                final boolean isContainerUsed = PsiTreeUtil.findChildrenOfType(body, container.getClass()).stream()
                                        .anyMatch(candidate -> OpenapiEquivalenceUtil.areEqual(candidate, container));
                                if (!isContainerUsed) {
                                    holder.registerProblem(
                                            parent.getFirstChild(),
                                            eachFunctionUsed,
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
        @Nullable
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
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        UseForeachFix(
            @NotNull PsiElement loop,
            @Nullable PsiElement index,
            @Nullable PsiElement value,
            @NotNull PsiElement container,
            @Nullable PsiElement limit
        ) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(loop.getProject());

            this.loop      = factory.createSmartPsiElementPointer(loop);
            this.index     = index == null ? null : factory.createSmartPsiElementPointer(index);
            this.value     = value == null ? null : factory.createSmartPsiElementPointer(value);
            this.container = factory.createSmartPsiElementPointer(container);
            this.limit     = limit == null ? null : factory.createSmartPsiElementPointer(limit);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement loop      = this.loop.getElement();
            final PsiElement container = this.container.getElement();
            if (loop != null && container != null) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(loop);
                if (body != null) {
                    final PsiElement index = this.index == null ? null : this.index.getElement();
                    final PsiElement value = this.value == null ? null : this.value.getElement();
                    final String pattern   = "foreach (%c% as %i% => %i%Value) {}"
                            .replace("%i%Value", value == null ? "%i%Value" : value.getText())
                            .replace("%i% => ",  index == null ? "" : "%i% => ")
                            .replace("%i%",      index == null ? "" : index.getText())
                            .replace("%i%",      index == null ? "" : index.getText())
                            .replace("%c%",      container.getText());
                    final ForeachStatement replacement    = PhpPsiElementFactory.createPhpPsiFromText(project, ForeachStatement.class, pattern);
                    final PsiElement replacementContainer = replacement.getValue();
                    final GroupStatement bodyHolder       = ExpressionSemanticUtil.getGroupStatement(replacement);
                    if (bodyHolder != null && replacementContainer != null) {
                        final PsiElement limit = this.limit == null ? null : this.limit.getElement();
                        final Function scope   = ExpressionSemanticUtil.getScope(loop);
                        if (index != null) {
                            this.updateContainersUsage(body, container, index, replacementContainer);
                        }
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
                            if (resolved instanceof Function) {
                                return Arrays.stream(((Function) resolved).getParameters()).noneMatch(Parameter::isPassByRef);
                            }
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