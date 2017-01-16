package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ComparisonOperandsOrderInspector extends BasePhpInspection {
    final static private String messageUseYoda    = "Yoda conditions style should be used instead";
    final static private String messageUseRegular = "Regular conditions style should be used instead";

    final static private Set<IElementType> operations   = new HashSet<>();
    static {
        operations.add(PhpTokenTypes.opEQUAL);
        operations.add(PhpTokenTypes.opNOT_EQUAL);
        operations.add(PhpTokenTypes.opIDENTICAL);
        operations.add(PhpTokenTypes.opNOT_IDENTICAL);
    }

    @NotNull
    public String getShortName() {
        return "ComparisonOperandsOrderInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(BinaryExpression expression) {
                /* verify general structure */
                final IElementType operator = expression.getOperationType();
                final PsiElement left       = expression.getLeftOperand();
                final PsiElement right      = expression.getRightOperand();
                if (null == operator || null == left || null == right || !operations.contains(operator)) {
                    return;
                }

                /* identify operands type */
                final boolean isLeftConstant =
                    left instanceof StringLiteralExpression ||
                    left instanceof ConstantReference ||
                    PhpTokenTypes.tsNUMBERS.contains(left.getNode().getElementType());
                final boolean isRightConstant =
                    right instanceof StringLiteralExpression ||
                    right instanceof ConstantReference ||
                    PhpTokenTypes.tsNUMBERS.contains(right.getNode().getElementType());
                if (isLeftConstant && isRightConstant) {
                    return;
                }

                if (!isLeftConstant) {
                    holder.registerProblem(expression, messageUseYoda, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}
