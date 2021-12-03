package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils.ExpressionCostEstimateUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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
    // Inspection options.
    public boolean IGNORE_INCLUDES = true;

    private static final String messagePattern = "'$%s' seems to be not defined in the scope.";

    private static final Set<String> specialVariables = new HashSet<>(ExpressionCostEstimateUtil.predefinedVars);
    static {
        specialVariables.add("this");
        specialVariables.add("php_errormsg");
        specialVariables.add("http_response_header");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "IssetArgumentExistenceInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Isset operations variables existence";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                final PsiElement candidate = expression.getLeftOperand();
                if (candidate instanceof Variable && PhpTokenTypes.opCOALESCE == expression.getOperationType()) {
                    final Variable variable = (Variable) candidate;
                    if (!this.getSuppliedVariables(expression).contains(variable.getName())) {
                        analyzeExistence(variable);
                    }
                }
            }

            @Override
            public void visitPhpEmpty(@NotNull PhpEmpty expression) {
                this.analyzeArgumentsExistence(expression.getVariables());
            }

            @Override
            public void visitPhpIsset(@NotNull PhpIsset expression) {
                this.analyzeArgumentsExistence(expression.getVariables());
            }

            private void analyzeArgumentsExistence(@NotNull PhpExpression[] arguments) {
                if (arguments.length > 0) {
                    final Set<String> parameters = this.getSuppliedVariables(arguments[0]);
                    for (final PhpExpression argument : arguments) {
                        if (argument instanceof Variable && ! parameters.contains(argument.getName())) {
                            this.analyzeExistence((Variable) argument);
                        }
                    }
                    parameters.clear();
                }
            }

            private void analyzeExistence (@NotNull Variable variable) {
                final String variableName = variable.getName();
                if (! variableName.isEmpty() && ! specialVariables.contains(variableName)) {
                    final Function scope      = ExpressionSemanticUtil.getScope(variable);
                    final GroupStatement body = scope == null ? null : ExpressionSemanticUtil.getGroupStatement(scope);
                    if (body != null) {
                        for (final Variable reference : PsiTreeUtil.findChildrenOfType(body, Variable.class)) {
                            if (reference.getName().equals(variableName)) {
                                boolean report = reference == variable;
                                if (! report) {
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
                                    if (report && (IGNORE_INCLUDES || ! this.hasIncludes(scope)) && ! this.hasGotos(scope)) {
                                        holder.registerProblem(
                                                variable,
                                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), variableName),
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
                    if (used != null && ! used.isEmpty()) {
                        used.forEach(v -> result.add(v.getName()));
                        used.clear();
                    }
                }
                return result;
            }

            private boolean hasIncludes(@NotNull Function function) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(function);
                if (body != null) {
                    return PsiTreeUtil.findChildOfType(body, Include.class) != null;
                }
                return false;
            }

            private boolean hasGotos(@NotNull Function function) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(function);
                if (body != null) {
                    return PsiTreeUtil.findChildOfType(body, PhpGoto.class) != null;
                }
                return false;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
                component.addCheckbox("Ignore 'include' and 'require' statements", IGNORE_INCLUDES, (isSelected) -> IGNORE_INCLUDES = isSelected)
        );
    }
}
