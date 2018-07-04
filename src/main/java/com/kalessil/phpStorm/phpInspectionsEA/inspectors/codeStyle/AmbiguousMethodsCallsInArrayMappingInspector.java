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
            public void visitPhpFor(@NotNull For loop) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(loop);
                if (body != null) {
                    this.analyzeBody(body);
                }
            }

            @Override
            public void visitPhpForeach(@NotNull ForeachStatement loop) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(loop);
                if (body != null) {
                    this.analyzeBody(body);
                }
            }

            private void analyzeBody(@NotNull GroupStatement body) {
                for (final PsiElement instruction : body.getStatements()) {
                    final PsiElement candidate = instruction.getFirstChild();
                    if (candidate instanceof AssignmentExpression) {
                        final AssignmentExpression assignment = (AssignmentExpression) candidate;
                        final PsiElement value                = assignment.getValue();
                        final PsiElement container            = assignment.getVariable();
                        if (value != null && container instanceof ArrayAccessExpression) {
                            this.analyzeStatement((ArrayAccessExpression) container, value);
                        }
                    }
                }
            }

            private void analyzeStatement(@NotNull ArrayAccessExpression container, @NotNull PsiElement value) {
                final Collection<FunctionReference> left = PsiTreeUtil.findChildrenOfType(container, FunctionReference.class);
                if (!left.isEmpty()) {
                    final Collection<FunctionReference> right = PsiTreeUtil.findChildrenOfType(value, FunctionReference.class);
                    if (value instanceof FunctionReference) {
                        right.add((FunctionReference) value);
                    }
                    if (!right.isEmpty()) {
                        iterate:
                        for (final FunctionReference current : right) {
                            final String currentName = current.getName();
                            for (final FunctionReference candidate : left) {
                                final String candidateName = candidate.getName();
                                if (currentName != null && currentName.equals(candidateName) && OpenapiEquivalenceUtil.areEqual(current, candidate)) {
                                    holder.registerProblem(current, message);
                                    break iterate;
                                }
                            }
                        }
                        right.clear();
                    }
                    left.clear();
                }
            }
        };
    }
}
