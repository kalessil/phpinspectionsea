package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
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

import java.util.HashSet;
import java.util.Set;

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
                    /* do not check nested operations */
                    final boolean isNestedBinary = expression.getParent() instanceof BinaryExpression;
                    if (!isNestedBinary) {
                        /* do not report ' ... + []' and '[] + ...' */
                        final PsiElement right = expression.getRightOperand();
                        PsiElement left        = expression.getLeftOperand();
                        while (left instanceof BinaryExpression) {
                            left = ((BinaryExpression) left).getLeftOperand();
                        }
                        if (left != null && right != null) {
                            final boolean addsImplicitArray
                                    = left instanceof ArrayCreationExpression || right instanceof ArrayCreationExpression;
                            if (!addsImplicitArray) {
                                this.inspectExpression(operation, expression);
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
                    final boolean addsImplicitArray = expression.getValue() instanceof ArrayCreationExpression;
                    if (!addsImplicitArray) {
                        this.inspectExpression(operation, expression);
                    }
                }
            }

            /* inspection itself */
            private void inspectExpression(@NotNull PsiElement operation, @NotNull PsiElement expression) {
                if (expression instanceof PhpTypedElement) {
                    final Set<String> types = new HashSet<>();
                    final PhpType resolved  = OpenapiResolveUtil.resolveType((PhpTypedElement) expression, holder.getProject());
                    if (resolved != null) {
                        resolved.filterUnknown().getTypes().forEach(t -> types.add(Types.getType(t)));
                    }
                    if (types.size() == 1 && types.contains(Types.strArray)) {
                        holder.registerProblem(operation, message);
                    }
                    types.clear();
                }
            }
        };
    }
}
