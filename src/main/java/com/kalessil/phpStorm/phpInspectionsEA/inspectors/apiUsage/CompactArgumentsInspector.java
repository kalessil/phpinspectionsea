package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
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

public class CompactArgumentsInspector extends BasePhpInspection {
    private static final String messagePattern = "'$%s' might not be defined in the scope.";

    @NotNull
    @Override
    public String getShortName() {
        return "CompactArgumentsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'compact(...)' variables existence";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
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
                                }
                            }
                            /* if we have something to analyze, collect what scope provides */
                            if (!compactedVariables.isEmpty()) {
                                /* parameters and local variables can be compacted, just ensure the order is correct */
                                final Set<String> declaredVariables = Arrays.stream(scope.getParameters())
                                        .map(Parameter::getName)
                                        .collect(Collectors.toSet());
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
                                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), subject),
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
