package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class CompactArgumentsInspector extends BasePhpInspection {
    private static final String patternUnknownVariable = "'$%s' might not be defined in the scope.";
    private static final String patternStringExpected  = "There is chance that it should be '%s' here.";

    @NotNull
    public String getShortName() {
        return "CompactArgumentsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(reference))              { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("compact")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > 0) {
                        final Function scope = ExpressionSemanticUtil.getScope(reference);
                        if (scope != null) {
                            /* extract variables names needed */
                            final Set<String> compactedVariables = new HashSet<>();
                            for (final PsiElement callArgument : arguments) {
                                if (callArgument instanceof StringLiteralExpression) {
                                    final StringLiteralExpression expression = (StringLiteralExpression) callArgument;
                                    final String name                        = expression.getContents();
                                    if (!name.isEmpty() && expression.getFirstPsiChild() == null) {
                                        compactedVariables.add(name);
                                    }
                                } else if (callArgument instanceof Variable) {
                                    final Variable argument     = (Variable) callArgument;
                                    final String argumentName   = argument.getName();
                                    final Set<String> variables = PossibleValuesDiscoveryUtil.discover(argument).stream()
                                        .filter(value  -> value instanceof StringLiteralExpression && ((PhpPsiElement) value).getFirstPsiChild() == null)
                                        .map(literal   -> ((StringLiteralExpression) literal).getContents())
                                        .filter(string -> !string.isEmpty() && !string.equals(argumentName) && string.matches("^[a-z0-9_]+$"))
                                        .collect(Collectors.toSet());
                                    boolean found = false;
                                    if (variables.size() == 1) {
                                        found = PsiTreeUtil.findChildrenOfType(scope, Variable.class).stream()
                                                .anyMatch(v -> v != argument && variables.contains(v.getName()));
                                    }
                                    if (!found) {
                                        holder.registerProblem(
                                                argument,
                                                String.format(patternStringExpected, argument.getName()),
                                                ProblemHighlightType.WEAK_WARNING
                                        );
                                    }
                                    variables.clear();
                                }
                            }
                            /* if we have something to analyze, collect what scope provides */
                            if (!compactedVariables.isEmpty()) {
                                /* parameters and local variables can be compacted, just ensure the order is correct */
                                final Set<String> declaredVariables = Arrays.stream(scope.getParameters()).map(Parameter::getName).collect(Collectors.toSet());
                                for (final PhpReference entry : PsiTreeUtil.findChildrenOfAnyType(scope, Variable.class, FunctionReference.class)) {
                                    if (entry == reference) {
                                        break;
                                    } else if (entry instanceof Variable) {
                                        declaredVariables.add(entry.getName());
                                    }
                                }

                                /* analyze and report suspicious parameters, release refs afterwards */
                                compactedVariables.forEach(subject -> {
                                    if (!declaredVariables.contains(subject)) {
                                        holder.registerProblem(
                                                reference,
                                                String.format(patternUnknownVariable, subject),
                                                ProblemHighlightType.GENERIC_ERROR
                                        );
                                    }
                                });
                                declaredVariables.clear();
                                compactedVariables.clear();
                            }
                        }
                    }
                }
            }
        };
    }
}
