package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class RepetitiveMethodCallsInspector extends PhpInspection {
    private static final String messageSequential = "Same as in the previous call, consider introducing a local variable instead.";
    private static final String messageLoop       = "Repetitive call, consider introducing a local variable outside of the loop.";

    @NotNull
    public String getShortName() {
        return "RepetitiveMethodCallsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }

                final IElementType operation = expression.getOperationType();
                if (operation == PhpTokenTypes.opAND || operation == PhpTokenTypes.opOR) {
                    final PsiElement parent = expression.getParent();
                    if (!(parent instanceof BinaryExpression) || ((BinaryExpression) parent).getOperationType() != operation) {
                        final List<PsiElement> conditions = ExpressionSemanticUtil.getConditions(expression, null);
                        if (conditions != null && !conditions.isEmpty()) {
                            final List<MethodReference> references = new ArrayList<>();
                            conditions.forEach(condition -> {
                                if (condition instanceof MethodReference) {
                                    final PsiElement base = condition.getFirstChild();
                                    if (base instanceof MethodReference) {
                                        references.add((MethodReference) base);
                                    }
                                } else if (condition instanceof BinaryExpression) {
                                    final BinaryExpression binary = (BinaryExpression) condition;
                                    final PsiElement left         = binary.getLeftOperand();
                                    if (left instanceof MethodReference) {
                                        final PsiElement base = left.getFirstChild();
                                        if (base instanceof MethodReference) {
                                            references.add((MethodReference) base);
                                        }
                                    }
                                    final PsiElement right        = binary.getRightOperand();
                                    if (right instanceof MethodReference) {
                                        final PsiElement base = right.getFirstChild();
                                        if (base instanceof MethodReference) {
                                            references.add((MethodReference) base);
                                        }
                                    }
                                }
                            });
                            conditions.clear();
                            this.analyzeReferences(references);
                            references.clear();
                        }
                    }
                }
            }

            @Override
            public void visitPhpArrayCreationExpression(@NotNull ArrayCreationExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }

                /* extract base method references */
                final List<MethodReference> references = new ArrayList<>();
                for (final PsiElement child : expression.getChildren()) {
                    /* find calls in keys */
                    final PsiElement key = child instanceof ArrayHashElement
                            ? ((ArrayHashElement) child).getKey()
                            : null;
                    if (key instanceof MethodReference) {
                        PsiElement base = key.getFirstChild();
                        while (base instanceof MethodReference) {
                            references.add((MethodReference) base);
                            base = base.getFirstChild();
                        }
                    }
                    /* find calls in values */
                    final PsiElement value = child instanceof ArrayHashElement
                            ? ((ArrayHashElement) child).getValue()
                            : child.getFirstChild();
                    if (value instanceof MethodReference) {
                        PsiElement base = value.getFirstChild();
                        while (base instanceof MethodReference) {
                            references.add((MethodReference) base);
                            base = base.getFirstChild();
                        }
                    }
                }
                this.analyzeReferences(references);
                references.clear();
            }

            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }

                if (!expression.isShort()) {
                    PsiElement base = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                    if (base instanceof BinaryExpression) {
                        /* instance of, comparison operations */
                        base = ((BinaryExpression) base).getLeftOperand();
                    }
                    if (base instanceof MethodReference) {
                        final List<MethodReference> references = new ArrayList<>();
                        references.add((MethodReference) base);

                        final PsiElement candidate = expression.getTrueVariant();
                        if (candidate instanceof MethodReference) {
                            final PsiElement candidateBase = candidate.getFirstChild();
                            if (candidateBase instanceof MethodReference) {
                                references.add((MethodReference) candidateBase);
                            }
                        }

                        this.analyzeReferences(references);
                        references.clear();
                    }
                }
            }

            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }

                final PsiElement currentBase = reference.getFirstChild();
                if (currentBase instanceof MethodReference) {
                    final PsiElement parent = reference.getParent();
                    if (OpenapiTypesUtil.isStatementImpl(parent)) {
                        final PsiElement grandParent = parent.getParent();
                        final PsiElement previous    = ((Statement) parent).getPrevPsiSibling();
                        if (previous == null && grandParent instanceof GroupStatement) {
                            /* case: call in a loop */
                            final PsiElement candidate = grandParent.getParent();
                            if (OpenapiTypesUtil.isLoop(candidate) && !this.isTestContext(parent)) {
                                final Set<String> variables = this.getLoopVariables((PhpPsiElement) candidate);
                                if (!variables.isEmpty()) {
                                    final boolean depends = PsiTreeUtil.findChildrenOfType(currentBase, Variable.class).stream()
                                            .anyMatch(v -> variables.contains(v.getName()));
                                    if (!depends) {
                                        holder.registerProblem(currentBase, messageLoop);
                                    }
                                    variables.clear();
                                }
                            }
                        } else if (OpenapiTypesUtil.isStatementImpl(previous)) {
                            /* case: sequential calls */
                            final PsiElement candidate = previous.getFirstChild();
                            if (candidate instanceof MethodReference && !this.isTestContext(parent)) {
                                final PsiElement previousBase = candidate.getFirstChild();
                                if (OpenapiEquivalenceUtil.areEqual(currentBase, previousBase)) {
                                    holder.registerProblem(currentBase, messageSequential);
                                }
                            }
                        }
                    }
                }
            }

            @NotNull
            private Set<String> getLoopVariables(@NotNull PhpPsiElement loop) {
                final Set<String> variables = new HashSet<>();
                if (loop instanceof For) {
                    /* get variables from assignments */
                    Stream.of(((For) loop).getInitialExpressions())
                            .filter(init  -> init instanceof AssignmentExpression)
                            .forEach(init -> {
                                final PhpPsiElement variable = ((AssignmentExpression) init).getVariable();
                                if (variable instanceof Variable) {
                                    final String variableName = variable.getName();
                                    if (variableName != null) {
                                        variables.add(variableName);
                                    }
                                }
                            });
                } else if (loop instanceof ForeachStatement) {
                    ((ForeachStatement) loop).getVariables().forEach(variable -> variables.add(variable.getName()));
                }
                return variables;
            }

            private void analyzeReferences(@NotNull List<MethodReference> references) {
                if (references.size() > 1) {
                    final List<MethodReference> checked = new ArrayList<>(references.size());
                    iterate:
                    for (final MethodReference first : references) {
                        final String firstName = first.getName();
                        for (final MethodReference second : references) {
                            if (first != second && !checked.contains(second)) {
                                final boolean matches;
                                if (firstName != null) {
                                    matches = firstName.equals(second.getName()) && OpenapiEquivalenceUtil.areEqual(first, second);
                                } else {
                                    matches = OpenapiEquivalenceUtil.areEqual(first, second);
                                }
                                if (matches) {
                                    holder.registerProblem(second, messageSequential);
                                    break iterate;
                                }
                            }
                        }
                        checked.add(first);
                    }
                    checked.clear();
                }
            }
        };
    }
}
