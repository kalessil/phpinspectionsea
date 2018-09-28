package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils.ExpressionCostEstimateUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class IssetArgumentExistenceInspector extends BasePhpInspection {
    private static final String messagePattern = "'$%s' seems to be not defined in the scope.";

    private static final Set<String> specialVariables = new HashSet<>(ExpressionCostEstimateUtil.predefinedVars);
    static {
        specialVariables.add("this");
        specialVariables.add("php_errormsg");
    }

    @NotNull
    public String getShortName() {
        return "IssetArgumentExistenceInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(expression))             { return; }

                final PsiElement argument = expression.getLeftOperand();
                if (argument != null && PhpTokenTypes.opCOALESCE == expression.getOperationType()) {
                    this.analyzeArgumentsExistence(new PhpExpression[]{(PhpExpression) argument});
                }
            }

            @Override
            public void visitPhpEmpty(@NotNull PhpEmpty expression) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(expression))             { return; }

                this.analyzeArgumentsExistence(expression.getVariables());
            }

            @Override
            public void visitPhpIsset(@NotNull PhpIsset expression) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(expression))             { return; }

                this.analyzeArgumentsExistence(expression.getVariables());
            }

            private void analyzeArgumentsExistence(@NotNull PhpExpression[] arguments) {
                if (arguments.length > 0) {
                    final Set<String> parameters = this.getSuppliedVariables(arguments[0]);
                    for (final PhpExpression argument : arguments) {
                        /* support array accesses: extract variables */
                        PsiElement subject = argument;
                        while (subject instanceof ArrayAccessExpression || subject instanceof MemberReference) {
                            if (subject instanceof MemberReference) {
                                subject = ((MemberReference) subject).getClassReference();
                            } else {
                                subject = ((ArrayAccessExpression) subject).getValue();
                            }
                        }
                        /* now check variable existence */
                        if (subject instanceof Variable) {
                            final Variable variable = (Variable) subject;
                            if (!parameters.contains(variable.getName())) {
                                this.analyzeExistence(variable);
                            }
                        }
                    }
                    parameters.clear();
                }
            }

            private void analyzeExistence (@NotNull Variable variable) {
                final String variableName = variable.getName();
                if (!variableName.isEmpty() && !specialVariables.contains(variableName)) {
                    final Function scope      = ExpressionSemanticUtil.getScope(variable);
                    final GroupStatement body = scope == null ? null : ExpressionSemanticUtil.getGroupStatement(scope);
                    if (body != null) {
                        for (final Variable reference : PsiTreeUtil.findChildrenOfType(body, Variable.class)) {
                            if (reference.getName().equals(variableName)) {
                                boolean report = reference == variable;
                                if (!report) {
                                    final PsiElement parent = reference.getParent();
                                    if (parent instanceof AssignmentExpression) {
                                        report = PsiTreeUtil.findCommonParent(reference, variable) == parent;
                                    }
                                }
                                if (report) {
                                    /* variable created dynamically in a loop: hacky stuff, but nevertheless */
                                    PsiElement loopCandidate = reference.getParent();
                                    while (loopCandidate != null && loopCandidate != scope) {
                                        if (OpenapiTypesUtil.isLoop(loopCandidate)) {
                                            report = PsiTreeUtil.findChildrenOfType(loopCandidate, AssignmentExpression.class).stream()
                                                    .noneMatch(assignment -> {
                                                        final PsiElement container = assignment.getVariable();
                                                        return
                                                                container instanceof Variable &&
                                                               ((Variable) container).getName().equals(variableName);
                                                    });
                                            break;
                                        }
                                        loopCandidate = loopCandidate.getParent();
                                    }
                                    if (report) {
                                        holder.registerProblem(
                                                variable,
                                                String.format(messagePattern, variableName),
                                                ProblemHighlightType.GENERIC_ERROR
                                        );
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }

            @NotNull
            private Set<String> getSuppliedVariables(@NotNull PsiElement expression) {
                final Set<String> result = new HashSet<>();
                final Function scope     = ExpressionSemanticUtil.getScope(expression);
                if (scope != null) {
                    for (final Parameter parameter : scope.getParameters()) {
                        result.add(parameter.getName());
                    }
                    final List<Variable> used = ExpressionSemanticUtil.getUseListVariables(scope);
                    if (used != null && !used.isEmpty()) {
                        used.forEach(v -> result.add(v.getName()));
                        used.clear();
                    }
                }
                return result;
            }
        };
    }
}
