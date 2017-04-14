package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.strategy;

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
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

final public class NullableParameterStrategy {
    private static final String message = "Null pointer exception may occur here.";

    private static final Set<String> objectTypes = new HashSet<>();
    static {
        objectTypes.add(Types.strSelf);
        objectTypes.add(Types.strStatic);
        objectTypes.add(Types.strObject);
    }

    public static void apply(@NotNull Method method, @NotNull ProblemsHolder holder) {
        final PhpEntryPointInstruction controlFlowStart = method.getControlFlow().getEntryPoint();
        for (Parameter parameter : method.getParameters()) {
            final Set<String> declaredTypes = new HashSet<>();
            for (String type: parameter.getDeclaredType().getTypes()) {
                declaredTypes.add(Types.getType(type));
            }
            if (declaredTypes.contains(Types.strNull) || PhpLanguageUtil.isNull(parameter.getDefaultValue())) {
                declaredTypes.remove(Types.strNull);

                boolean isObject = !declaredTypes.isEmpty();
                for (String type : declaredTypes) {
                    if (!type.startsWith("\\") && !objectTypes.contains(type)) {
                        isObject = false;
                        break;
                    }
                }

                if (isObject) {
                    applyToParameter(parameter.getName(), controlFlowStart, holder);
                }
            }
        }
    }

    private static void applyToParameter(
        @NotNull String parameterName,
        @NotNull PhpEntryPointInstruction controlFlowStart,
        @NotNull ProblemsHolder holder
    ) {
        final PhpAccessVariableInstruction[] uses
                = PhpControlFlowUtil.getFollowingVariableAccessInstructions(controlFlowStart, parameterName, false);
        for (PhpAccessVariableInstruction instruction : uses) {
            final PhpPsiElement variable = instruction.getAnchor();
            final PsiElement parent      = variable.getParent();

            /* instanceof, implicit null comparisons */
            if (parent instanceof BinaryExpression) {
                final BinaryExpression expression = (BinaryExpression) parent;
                final IElementType operation      = expression.getOperationType();
                if (PhpTokenTypes.kwINSTANCEOF == operation) {
                    return;
                }
                if (PhpTokenTypes.opIDENTICAL == operation || PhpTokenTypes.opNOT_IDENTICAL == operation) {
                    PsiElement second = expression.getLeftOperand();
                    second            = second == variable ? expression.getRightOperand() : second;
                    if (PhpLanguageUtil.isFalse(second)) {
                        return;
                    }

                    continue;
                }
            }
            /*non-implicit null comparisons */
            if (parent instanceof PhpEmpty || parent instanceof PhpIsset) {
                return;
            }
            /* logical operand context */
            if (ExpressionSemanticUtil.isUsedAsLogicalOperand(variable)) {
                return;
            }

            /* cases when NPE can be introduced: call on the variable */
            if (parent instanceof MemberReference) {
                final MemberReference reference = (MemberReference) parent;
                final PsiElement subject        = reference.getClassReference();
                if (subject instanceof Variable && ((Variable) subject).getName().equals(parameterName)) {
                    holder.registerProblem(subject, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
            /* TODO: dispatching as an argument: resolve, check if non-nullable object */
        }
    }
}
