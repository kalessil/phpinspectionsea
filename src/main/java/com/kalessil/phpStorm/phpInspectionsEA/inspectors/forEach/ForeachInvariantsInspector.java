package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

import com.intellij.codeInsight.PsiEquivalenceUtil;
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
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public class ForeachInvariantsInspector extends BasePhpInspection {
    private static final String foreachInvariant = "Foreach can probably be used instead (easier to read and support; ensure a string is not iterated).";
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
                    final PsiElement variable = this.getCounterVariable(forExpression);
                    if (
                        variable != null &&
                        this.isCounterVariableIncremented(forExpression, variable) &&
                        this.isCheckedAsExpected(forExpression, variable)
                    ) {
                        final PsiElement container = this.getContainerByIndex(body, variable);
                        if (container != null) {
                            holder.registerProblem(
                                    forExpression.getFirstChild(),
                                    foreachInvariant,
                                    new UseForeachFix(forExpression, variable, container)
                            );
                        }
                    }
                }
            }

            @Override
            public void visitPhpMultiassignmentExpression(@NotNull MultiassignmentExpression assignmentExpression) {
                PsiElement value = assignmentExpression.getValue();
                if (OpenapiTypesUtil.is(value, PhpElementTypes.EXPRESSION)) {
                    value = value.getFirstChild();
                }

                if (OpenapiTypesUtil.isFunctionReference(value)) {
                    final String functionName = ((FunctionReference) value).getName();
                    if (functionName != null && functionName.equals("each")) {
                        final PsiElement parent = assignmentExpression.getParent();
                        if (parent instanceof While || parent instanceof For) {
                            holder.registerProblem(parent.getFirstChild(), eachFunctionUsed, ProblemHighlightType.GENERIC_ERROR);
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
                            argument != null && PsiEquivalenceUtil.areElementsEquivalent(variable, argument)
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
                    if (value instanceof Variable && PsiEquivalenceUtil.areElementsEquivalent(variable, value)) {
                        final PsiElement container = offset.getValue();
                        if (container != null) {
                            containers.put(container.getText(), container);
                            if (containers.size() > 1) {
                                break;
                            }
                        }
                    }
                }
                final PsiElement result = containers.size() == 1 ? containers.values().iterator().next().copy(): null;
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
                            if (left instanceof Variable && PsiEquivalenceUtil.areElementsEquivalent(variable, left)) {
                                value = right;
                            } else if (right instanceof Variable && PsiEquivalenceUtil.areElementsEquivalent(variable, right)) {
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

    private class UseForeachFix implements LocalQuickFix {
        private final SmartPsiElementPointer<PsiElement> loop;
        private final SmartPsiElementPointer<PsiElement> index;
        private final SmartPsiElementPointer<PsiElement> container;

        @NotNull
        @Override
        public String getName() {
            return "Use foreach instead";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        UseForeachFix(@NotNull For expression, @NotNull PsiElement index, @NotNull PsiElement container) {
            final SmartPointerManager factory = SmartPointerManager.getInstance(expression.getProject());

            this.loop      = factory.createSmartPsiElementPointer(expression);
            this.index     = factory.createSmartPsiElementPointer(index);
            this.container = factory.createSmartPsiElementPointer(container);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement loop      = this.loop.getElement();
            final PsiElement index     = this.index.getElement();
            final PsiElement container = this.container.getElement();
            if (loop != null && index != null && container != null) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(loop);
                if (body != null) {
                    final String pattern = "foreach (%c% as %i% => %i%Value) {}"
                            .replace("%i%", index.getText())
                            .replace("%i%", index.getText())
                            .replace("%c%", container.getText());
                    final PsiElement replacement    = PhpPsiElementFactory.createPhpPsiFromText(project, ForeachStatement.class, pattern);
                    final GroupStatement bodyHolder = ExpressionSemanticUtil.getGroupStatement(replacement);
                    if (bodyHolder != null) {
                        /* TODO: body find container[$index]->memberReferences an use indexMax */
                        bodyHolder.replace(body);
                        loop.replace(replacement);
                    }
                }
            }
        }
    }
}