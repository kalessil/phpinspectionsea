package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

public class CompactArgumentsInspector extends PhpInspection {
    private static final String patternUnknownVariable = "'$%s' might not be defined in the scope.";
    private static final String patternStringExpected  = "There is chance that it should be '%s' here.";

    @NotNull
    public String getShortName() {
        return "CompactArgumentsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("compact")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > 0) {
                        final Function scope = ExpressionSemanticUtil.getScope(reference);
                        if (scope != null) {
                            /* extract variables names needed */
                            final Map<String, PsiElement> compactedVariables = new HashMap<>();
                            for (final PsiElement argument : arguments) {
                                if (argument instanceof StringLiteralExpression) {
                                    final StringLiteralExpression expression = (StringLiteralExpression) argument;
                                    final String name                        = expression.getContents();
                                    if (!name.isEmpty() && expression.getFirstPsiChild() == null) {
                                        compactedVariables.put(name, argument);
                                    }
                                } else if (argument instanceof Variable) {
                                    final String argumentName   = ((Variable) argument).getName();
                                    /* argument can be a variable variable, hence we need to verify value container existence */
                                    final Set<String> variables = PossibleValuesDiscoveryUtil.discover(argument).stream()
                                        .filter(value  -> value instanceof StringLiteralExpression)
                                        .map(literal   -> ((StringLiteralExpression) literal).getContents())
                                        .filter(string -> !string.isEmpty() && !string.equals(argumentName) && string.matches("^[a-z0-9_]+$"))
                                        .collect(Collectors.toSet());
                                    boolean found = variables.size() == 1 &&
                                                    PsiTreeUtil.findChildrenOfType(scope, Variable.class).stream().anyMatch(v -> variables.contains(v.getName()));
                                    if (!found) {
                                        holder.registerProblem(
                                                argument,
                                                String.format(patternStringExpected, argumentName),
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
                                compactedVariables.keySet().forEach(subject -> {
                                    if (!declaredVariables.contains(subject)) {
                                        holder.registerProblem(
                                                compactedVariables.get(subject),
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
