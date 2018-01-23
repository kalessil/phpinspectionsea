package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
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

public class ExcessiveMethodCallsInspector extends BasePhpInspection {
    private static final String messageSequential = "Same as in the previous call, consider introducing a local variable instead.";
    private static final String messageLoop       = "Repetitive call, consider introducing a local variable instead outside of loop.";

    @NotNull
    public String getShortName() {
        return "ExcessiveMethodCallsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                /* TODO: no UTs */
                final PsiElement currentBase = reference.getFirstChild();
                if (currentBase instanceof MethodReference) {
                    final PsiElement parent = reference.getParent();
                    if (OpenapiTypesUtil.isStatementImpl(parent)) {
                        final PsiElement grandParent = parent.getParent();
                        final PsiElement previous    = ((Statement) parent).getPrevPsiSibling();
                        if (previous == null && grandParent instanceof GroupStatement) {
                            final PsiElement candidate = grandParent.getParent();
                            if (OpenapiTypesUtil.isLoop(candidate) && !this.isTestContext(parent)) {
                                final Set<String> variables = this.getLoopVariables((PhpPsiElement) candidate);
                                if (!variables.isEmpty()) {
                                    final boolean depends = PsiTreeUtil.findChildrenOfType(currentBase, Variable.class).stream()
                                            .anyMatch(v -> variables.contains(v.getName()));
                                    if (!depends) {
                                        holder.registerProblem(currentBase, messageLoop);
                                    }
                                }
                                variables.clear();
                            }
                        } else if (OpenapiTypesUtil.isStatementImpl(previous)) {
                            /* case: sequential calls */
                            final PsiElement candidate = previous.getFirstChild();
                            if (candidate instanceof MethodReference) {
                                final PsiElement previousBase = candidate.getFirstChild();
                                if (OpeanapiEquivalenceUtil.areEqual(currentBase, previousBase) && !this.isTestContext(parent)) {
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
        };
    }
}
