package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class MissingArrayInitializationInspector extends BasePhpInspection {
    private static final String message = "The array initialization is missing, please place it at a proper place.";

    @NotNull
    @Override
    public String getShortName() {
        return "MissingArrayInitializationInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Missing array initialization";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpArrayAccessExpression(@NotNull ArrayAccessExpression expression) {
                final ArrayIndex index = expression.getIndex();
                if (index != null && index.getValue() == null) {
                    final Function scope = ExpressionSemanticUtil.getScope(expression);
                    if (scope != null) {
                        /* identify nesting level */
                        int nestingLevel  = 0;
                        PsiElement parent = expression.getParent();
                        while (parent != null && parent != scope) {
                            if (OpenapiTypesUtil.isLoop(parent) && ++nestingLevel >= 2) {
                                break;
                            }
                            parent = parent.getParent();
                        }

                        /* target 2+ nesting levels */
                        if (nestingLevel >= 2) {
                            PsiElement container  = expression.getValue();
                            while (container instanceof ArrayAccessExpression) {
                                container = ((ArrayAccessExpression) container).getValue();
                            }
                            if (container instanceof Variable) {
                                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(scope);
                                if (body != null) {
                                    final String variableName = ((Variable) container).getName();
                                    /* false-positives: parameters */
                                    if (Arrays.stream(scope.getParameters()).anyMatch(p -> p.getName().equals(variableName))) {
                                        return;
                                    }
                                    /* false-positives: use-variables */
                                    final List<Variable> uses = ExpressionSemanticUtil.getUseListVariables(scope);
                                    if (uses != null && uses.stream().anyMatch(p -> p.getName().equals(variableName))) {
                                        return;
                                    }

                                    for (final PsiElement candidate : PsiTreeUtil.findChildrenOfType(body, container.getClass())) {
                                        final PsiElement context = candidate.getParent();
                                        /* a value has been written */
                                        if (context instanceof AssignmentExpression) {
                                            final PsiElement value = ((AssignmentExpression) context).getValue();
                                            if (value != container && OpenapiEquivalenceUtil.areEqual(candidate, container)) {
                                                return;
                                            }
                                        }
                                        /* container initialized by a foreach loop */
                                        if (context instanceof ForeachStatement) {
                                            final boolean areSame = OpenapiEquivalenceUtil.areEqual(candidate, container);
                                            if (areSame) {
                                                return;
                                            }
                                        }
                                    }
                                    problemsHolder.registerProblem(
                                            expression,
                                            MessagesPresentationUtil.prefixWithEa(message)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
