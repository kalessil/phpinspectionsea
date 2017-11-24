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
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class CompactArgumentsInspector extends BasePhpInspection {
    private static final String patternUnknownVariable = "$%s might not be defined in the scope.";
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

                final PsiElement[] arguments = reference.getParameters();
                final String functionName    = reference.getName();
                if (arguments.length > 0 && functionName != null && functionName.equals("compact")) {
                    final Function scope = ExpressionSemanticUtil.getScope(reference);
                    if (scope != null) {
                        /* extract variables names needed */
                        final Set<String> compacted = new HashSet<>();
                        for (final PsiElement argument : arguments) {
                            if (argument instanceof StringLiteralExpression) {
                                final StringLiteralExpression expression = (StringLiteralExpression) argument;
                                final String name                        = expression.getContents();
                                if (!name.isEmpty() && expression.getFirstPsiChild() == null) {
                                    compacted.add(name);
                                }
                            } else if (argument instanceof Variable) {
                                final String message = String.format(patternStringExpected, ((Variable) argument).getName());
                                holder.registerProblem(argument, message, ProblemHighlightType.WEAK_WARNING);
                            }
                        }

                        /* if we have something to analyze, collect what scope provides */
                        if (!compacted.isEmpty()) {
                            final Set<String> declarations = Stream.of(scope.getParameters())
                                    .map(Parameter::getName)
                                    .collect(Collectors.toSet());

                            /* local variables can be compacted, just ensure the order is correct */
                            //noinspection unchecked - want to keep the code clean from castings
                            for (final PhpReference entry : PsiTreeUtil.findChildrenOfAnyType(scope, Variable.class, FunctionReference.class)) {
                                if (entry instanceof Variable) {
                                    declarations.add(entry.getName());
                                }
                                if (entry == reference) {
                                    break;
                                }
                            }

                            /* analyze and report suspicious parameters, release refs afterwards */
                            compacted.stream()
                                    .filter(subject  -> !declarations.contains(subject))
                                    .forEach(subject -> {
                                        final String message = String.format(patternUnknownVariable, subject);
                                        holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);
                                    });

                            declarations.clear();
                            compacted.clear();
                        }
                    }
                }
            }
        };
    }
}
