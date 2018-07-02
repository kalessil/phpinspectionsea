package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class AmbiguousMethodsCallsInArrayMappingInspector extends BasePhpInspection {
    private static final String message = "Duplicated method calls should be moved to a local variable.";

    @NotNull
    public String getShortName() {
        return "AmbiguousMethodsCallsInArrayMappingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement foreach) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(foreach);
                if (body != null) {
                    for (final PsiElement instruction : body.getStatements()) {
                        final PsiElement candidate = instruction.getFirstChild();
                        if (candidate instanceof AssignmentExpression) {
                            final AssignmentExpression assignment = (AssignmentExpression) candidate;
                            final PsiElement value                = assignment.getValue();
                            final PsiElement container            = assignment.getVariable();
                            if (value != null && container instanceof ArrayAccessExpression) {
                                this.analyze((ArrayAccessExpression) container, value);
                            }
                        }
                    }
                }
            }

            private void analyze(@NotNull ArrayAccessExpression container, @NotNull PsiElement value) {
                final Collection<PsiElement> leftCalls = PsiTreeUtil.findChildrenOfType(container, FunctionReference.class);
                if (!leftCalls.isEmpty()) {
                    final Collection<PsiElement> rightCalls = PsiTreeUtil.findChildrenOfType(value, FunctionReference.class);
                    if (value instanceof FunctionReference) {
                        rightCalls.add(value);
                    }
                    if (!rightCalls.isEmpty()) {
                        iterate:
                        for (final PsiElement rightOccurrence : rightCalls) {
                            for (final PsiElement leftOccurrence : leftCalls) {
                                if (OpenapiEquivalenceUtil.areEqual(rightOccurrence, leftOccurrence)) {
                                    holder.registerProblem(rightOccurrence, message);
                                    break iterate;
                                }
                            }
                        }
                        rightCalls.clear();
                    }
                    leftCalls.clear();
                }
            }
        };
    }
}
