package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
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

public class InsufficientTypesControlInspector extends PhpInspection {
    private static final String message = "In multiple cases the result can be evaluated as false, consider hardening the condition.";

    private static final Set<String> functions  = new HashSet<>();
    static {
        functions.add("array_search");
        functions.add("array_shift");
        functions.add("array_pop");

        functions.add("strpos");
        functions.add("stripos");
        functions.add("strrpos");
        functions.add("strripos");
        functions.add("strstr");
        functions.add("stristr");
        functions.add("substr");

        functions.add("mb_strpos");
        functions.add("mb_stripos");
        functions.add("mb_strrpos");
        functions.add("mb_strripos");
        functions.add("mb_strstr");
        functions.add("mb_stristr");
        functions.add("mb_substr");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "InsufficientTypesControlInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Insufficient types control";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functions.contains(functionName) && reference.getParameters().length > 0) {
                    final boolean isTarget = ExpressionSemanticUtil.isUsedAsLogicalOperand(reference);
                    if (isTarget && this.isFromRootNamespace(reference)) {
                        final PsiElement target = NamedElementUtil.getNameIdentifier(reference);
                        if (target != null) {
                            holder.registerProblem(
                                    target,
                                    MessagesPresentationUtil.prefixWithEa(message)
                            );
                        }
                    }
                }
            }

            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                PhpTypedElement subject      = null;
                final IElementType operation = expression.getOperationType();
                if (operation == PhpTokenTypes.opLESS || operation == PhpTokenTypes.opLESS_OR_EQUAL) {
                    final PsiElement left = expression.getLeftOperand();
                    if (left instanceof PhpTypedElement) {
                        subject = (PhpTypedElement) left;
                    }
                } else if (operation == PhpTokenTypes.opGREATER || operation == PhpTokenTypes.opGREATER_OR_EQUAL) {
                    final PsiElement right = expression.getRightOperand();
                    if (right instanceof PhpTypedElement) {
                        subject = (PhpTypedElement) right;
                    }
                }
                if (subject != null) {
                    final PhpType type = OpenapiResolveUtil.resolveType(subject, holder.getProject());
                    if (type != null && ! type.isEmpty()) {
                        final boolean isTarget = type.getTypes().stream().map(Types::getType).anyMatch(t -> t.equals(Types.strNull) || t.equals(Types.strBoolean));
                        if (isTarget) {
                            holder.registerProblem(
                                    expression,
                                    MessagesPresentationUtil.prefixWithEa(message)
                            );
                        }
                    }
                }
            }
        };
    }
}
