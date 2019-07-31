package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnusedMockInspector extends PhpInspection {
    private final static String message = "The mock seems to be not used, consider deleting it.";

    @NotNull
    public String getShortName() {
        return "UnusedMockInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                final String methodName = reference.getName();
                if (methodName != null) {
                    final boolean isMocking = methodName.equals("createMock") || methodName.equals("getMock");
                    if (isMocking && this.isTestContext(reference)) {
                        final PsiElement parent = reference.getParent();
                        if (OpenapiTypesUtil.isAssignment(parent) && OpenapiTypesUtil.isStatementImpl(parent.getParent())) {
                            final PsiElement container = ((AssignmentExpression) parent).getVariable();
                            if (container instanceof Variable) {
                                final Function scope = ExpressionSemanticUtil.getScope(reference);
                                if (scope != null) {
                                    // not a parameter/use variable

                                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(scope);
                                    if (body != null) {
                                        final String variableName = ((Variable) container).getName();
                                        final boolean isUsed      = PsiTreeUtil.findChildrenOfType(body, Variable.class).stream()
                                                .filter(v   -> variableName.equals(v.getName()) && v != container)
                                                .anyMatch(v -> {
                                                    final PsiElement directParent = v.getParent();
                                                    if (directParent instanceof MethodReference) {
                                                        final MethodReference call = (MethodReference) directParent;
                                                        return call.getFirstChild() != v || ! "expects".equals(call.getName());
                                                    }
                                                    return true;
                                                });
                                        if (!isUsed) {
                                            holder.registerProblem(container, message, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
