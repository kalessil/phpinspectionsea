package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NonSecureOpensslVerifyUsageInspector extends PhpInspection {
    private static final String messageReturn = "Please return '... === 1' instead (to prevent any flaws).";
    private static final String messageHarden = "Please compare with 1 instead (see openssl_verify(...) return codes).";

    @NotNull
    public String getShortName() {
        return "NonSecureOpensslVerifyUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_SECURITY)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("openssl_verify")) {
                    final List<PsiElement> targets = new ArrayList<>();
                    /* identify targets */
                    if (OpenapiTypesUtil.isAssignment(reference.getParent())) {
                        final AssignmentExpression assignment = (AssignmentExpression) reference.getParent();
                        final PsiElement container            = assignment.getVariable();
                        if (container instanceof Variable) {
                            final Function scope      = ExpressionSemanticUtil.getScope(assignment);
                            final GroupStatement body = scope == null ? null : ExpressionSemanticUtil.getGroupStatement(scope);
                            if (scope != null) {
                                final String variableName = ((Variable) container).getName();
                                PsiTreeUtil.findChildrenOfType(body, Variable.class).forEach(variable -> {
                                    if (variable != container && variable.getName().equals(variableName)) {
                                        targets.add(variable);
                                    }
                                });
                            }
                        }
                    } else {
                        targets.add(reference);
                    }
                    /* process targets */
                    for (final PsiElement target : targets) {
                        final PsiElement parent = target.getParent();
                        if (parent instanceof BinaryExpression) {
                            if (target instanceof FunctionReference) {
                                final BinaryExpression binary = (BinaryExpression) parent;
                                if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(binary.getOperationType())) {
                                    final PsiElement second = OpenapiElementsUtil.getSecondOperand(binary, target);
                                    if (second != null && OpenapiTypesUtil.isNumber(second) && !second.getText().equals("1")) {
                                        holder.registerProblem(target, messageHarden, ProblemHighlightType.GENERIC_ERROR);
                                    }
                                }
                            }
                        } else if (parent instanceof PhpReturn) {
                            holder.registerProblem(target, messageReturn, ProblemHighlightType.GENERIC_ERROR);
                        } else if (ExpressionSemanticUtil.isUsedAsLogicalOperand(target)) {
                            holder.registerProblem(target, messageHarden, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                    targets.clear();
                }
            }
        };
    }
}
