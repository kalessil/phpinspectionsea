package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.elements.SelfAssignmentExpression;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class AdditionOperationOnArraysInspection extends BasePhpInspection {
    private static final String message = "Perhaps array_merge/array_replace can be used instead. Feel free to disable the inspection if '+' is intended.";

    @NotNull
    public String getShortName() {
        return "AdditionOperationOnArraysInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                final PsiElement operation = expression.getOperation();
                if (OpenapiTypesUtil.is(operation, PhpTokenTypes.opPLUS)) {
                    final boolean isNestedBinary = expression.getParent() instanceof BinaryExpression;
                    if (!isNestedBinary) {
                        /* do not report ' ... + []' and '[] + ...' */
                        final PsiElement right = expression.getRightOperand();
                        PsiElement left        = expression.getLeftOperand();
                        while (left instanceof BinaryExpression) {
                            left = ((BinaryExpression) left).getLeftOperand();
                        }
                        if (left instanceof PhpTypedElement && right instanceof PhpTypedElement) {
                            final boolean addsImplicitArray = left instanceof ArrayCreationExpression ||
                                                              right instanceof ArrayCreationExpression;
                            if (!addsImplicitArray) {
                                this.inspectExpression(operation, (PhpTypedElement) left, (PhpTypedElement) right);
                            }
                        }
                    }
                }
            }

            @Override
            public void visitPhpSelfAssignmentExpression(@NotNull SelfAssignmentExpression expression) {
                final PsiElement operation = expression.getOperation();
                if (OpenapiTypesUtil.is(operation, PhpTokenTypes.opPLUS_ASGN)) {
                    /* do not report '... += []' */
                    final PsiElement variable       = expression.getVariable();
                    final PsiElement value          = expression.getValue();
                    final boolean addsImplicitArray = value instanceof ArrayCreationExpression;
                    if (!addsImplicitArray && variable instanceof PhpTypedElement && value instanceof PhpTypedElement) {
                        this.inspectExpression(operation, (PhpTypedElement) variable, (PhpTypedElement) value);
                    }
                }
            }

            private void inspectExpression(
                    @NotNull PsiElement operation,
                    @NotNull PhpTypedElement left,
                    @NotNull PhpTypedElement right
            ) {
                final Project project      = holder.getProject();
                final PhpType leftResolved = OpenapiResolveUtil.resolveType(left, project);
                if (leftResolved != null) {
                    final boolean isLeftArray = leftResolved.filterUnknown().getTypes().stream()
                            .anyMatch(type -> Types.getType(type).equals(Types.strArray));
                    if (isLeftArray) {
                        final PhpType rightResolved = OpenapiResolveUtil.resolveType(right, project);
                        if (rightResolved != null) {
                            final boolean isRightArray = rightResolved.filterUnknown().getTypes().stream()
                                    .anyMatch(type -> Types.getType(type).equals(Types.strArray));
                            if (isRightArray) {
                                holder.registerProblem(operation, message);
                            }
                        }
                    }
                }
            }
        };
    }
}
