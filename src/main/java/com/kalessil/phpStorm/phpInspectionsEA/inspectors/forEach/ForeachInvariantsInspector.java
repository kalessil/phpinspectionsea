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
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final String eachFunctionUsed = "Foreach should be used instead (8x faster).";

    @NotNull
    public String getShortName() {
        return "ForeachInvariantsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFor(@NotNull For forExpression) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(forExpression);
                if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) > 0) {
                    final PsiElement indexVariable = this.getCounterVariable(forExpression);
                    if (
                        indexVariable != null &&
                        this.isCounterVariableIncremented(forExpression, indexVariable) &&
                        this.isCheckedAsExpected(forExpression, indexVariable)
                    ) {
                        final PsiElement container = this.getContainerByIndex(body, indexVariable);
                        if (container != null && this.isIterableContainer(container)) {
                            holder.registerProblem(
                                    forExpression.getFirstChild(),
                                    foreachInvariant,
                                    new UseForeachFix(forExpression, indexVariable, null, container)
                            );
                        }
                    }
                }
            }

            private boolean isIterableContainer(@NotNull PsiElement container) {
                boolean result = false;
                if (container instanceof PhpTypedElement) {
                    final Project project       = holder.getProject();
                    final PhpType containerType = OpenapiResolveUtil.resolveType((PhpTypedElement) container, project);
                    if (containerType != null && !containerType.hasUnknown()) {
                        result = containerType.getTypes().stream().noneMatch(t -> Types.getType(t).equals(Types.strString));
                    }
                }
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
                                        fixer = new UseForeachFix(parent, variables.get(0), variables.get(1), arguments[0]);
                                    }
                                }

                                holder.registerProblem(
                                    parent.getFirstChild(), eachFunctionUsed, ProblemHighlightType.GENERIC_ERROR, fixer
                                );
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

            private boolean isCheckedAsExpected(@NotNull For expression, @NotNull PsiElement variable) {
                boolean result               = false;
                final PsiElement[] conditions = expression.getConditionalExpressions();
                if (conditions.length == 1) {
                    for (final PsiElement check : conditions) {
                        if (check instanceof BinaryExpression) {
                            final BinaryExpression condition = (BinaryExpression) check;
                            final PsiElement left            = condition.getLeftOperand();
                            final PsiElement right           = condition.getRightOperand();

                            final PsiElement value;
                            if (left instanceof Variable && OpenapiEquivalenceUtil.areEqual(variable, left)) {
                                value = right;
                            } else if (right instanceof Variable && OpenapiEquivalenceUtil.areEqual(variable, right)) {
                                value = left;
                            } else {
                                value = null;
                            }

                            result = value instanceof Variable;
                            break;
                        }
                    }
                }
                return result;
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
            @NotNull PsiElement index,
            @Nullable PsiElement value,
            @NotNull PsiElement container
        ) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(loop.getProject());

            this.loop      = factory.createSmartPsiElementPointer(loop);
            this.index     = factory.createSmartPsiElementPointer(index);
            this.value     = value == null ? null : factory.createSmartPsiElementPointer(value);
            this.container = factory.createSmartPsiElementPointer(container);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement loop      = this.loop.getElement();
            final PsiElement index     = this.index.getElement();
            final PsiElement value     = this.value == null ? null : this.value.getElement();
            final PsiElement container = this.container.getElement();
            if (loop != null && index != null && container != null) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(loop);
                if (body != null) {
                    final String pattern = "foreach (%c% as %i% => %i%Value) {}"
                            .replace("%i%Value", value == null ? "%i%Value" : value.getText())
                            .replace("%i%", index.getText())
                            .replace("%i%", index.getText())
                            .replace("%c%", container.getText());
                    final ForeachStatement replacement    = PhpPsiElementFactory.createPhpPsiFromText(project, ForeachStatement.class, pattern);
                    final PsiElement replacementContainer = replacement.getValue();
                    final GroupStatement bodyHolder       = ExpressionSemanticUtil.getGroupStatement(replacement);
                    if (bodyHolder != null && replacementContainer != null) {
                        this.updateContainersUsage(body, container, index, replacementContainer);
                        bodyHolder.replace(body);
                        this.cleanupUnusedIndex(replacement, body);
                        loop.replace(replacement);
                    }
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
                    boolean result          = false;
                    final PsiElement parent = offset.getParent();
                    if (parent instanceof MemberReference || parent instanceof BinaryExpression) {
                        /* common cases which can be fixed */
                        result = true;
                    } else if (parent instanceof AssignmentExpression) {
                        /* assignments, but not by reference */
                        final AssignmentExpression assignment = (AssignmentExpression) parent;
                        if (assignment.getValue() == offset) {
                            PsiElement operation = offset.getPrevSibling();
                            while (operation != null && !OpenapiTypesUtil.is(operation, PhpTokenTypes.opASGN)) {
                                operation = operation.getPrevSibling();
                            }
                            result = operation != null && !operation.getText().replaceAll("\\s+", "").equals("=&");
                        }
                    }
                    return result;
                }).forEach(offset -> {
                    final ArrayIndex offsetIndex   = offset.getIndex();
                    final PsiElement usedIndex     = offsetIndex == null ? null : offsetIndex.getValue();
                    final PsiElement usedContainer = offset.getValue();
                    if (
                        usedIndex != null && usedContainer != null &&
                        OpenapiEquivalenceUtil.areEqual(index, usedIndex) &&
                        OpenapiEquivalenceUtil.areEqual(container, usedContainer)
                    ) {
                        offset.replace(replacement);
                    }
                });
        }
    }
}