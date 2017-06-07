package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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
        for (final Parameter parameter : method.getParameters()) {
            final Set<String> declaredTypes = new HashSet<>();
            for (final String type: parameter.getDeclaredType().getTypes()) {
                declaredTypes.add(Types.getType(type));
            }
            if (declaredTypes.contains(Types.strNull) || PhpLanguageUtil.isNull(parameter.getDefaultValue())) {
                declaredTypes.remove(Types.strNull);

                boolean isObject = !declaredTypes.isEmpty();
                for (final String type : declaredTypes) {
                    if (!type.startsWith("\\") && !objectTypes.contains(type)) {
                        isObject = false;
                        break;
                    }
                }

                if (isObject) {
                    applyToParameter(parameter.getName(), controlFlowStart, holder);
                }
            }
            declaredTypes.clear();
        }
    }

    private static void applyToParameter(
        @NotNull String parameterName,
        @NotNull PhpEntryPointInstruction controlFlowStart,
        @NotNull ProblemsHolder holder
    ) {
        final PhpAccessVariableInstruction[] uses
                = PhpControlFlowUtil.getFollowingVariableAccessInstructions(controlFlowStart, parameterName, false);
        for (final PhpAccessVariableInstruction instruction : uses) {
            final PhpPsiElement variable = instruction.getAnchor();
            final PsiElement parent      = variable.getParent();

            /* instanceof, implicit null comparisons */
            if (parent instanceof BinaryExpression) {
                final BinaryExpression expression = (BinaryExpression) parent;
                final IElementType operation      = expression.getOperationType();
                if (PhpTokenTypes.kwINSTANCEOF == operation) {
                    return;
                }
                if (
                    PhpTokenTypes.opIDENTICAL == operation || PhpTokenTypes.opNOT_IDENTICAL == operation ||
                    PhpTokenTypes.opEQUAL == operation     || PhpTokenTypes.opNOT_EQUAL == operation
                ) {
                    PsiElement second = expression.getLeftOperand();
                    second            = second == variable ? expression.getRightOperand() : second;
                    if (PhpLanguageUtil.isNull(second)) {
                        return;
                    }

                    continue;
                }
            }
            /* non-implicit null comparisons */
            if (parent instanceof PhpEmpty || parent instanceof PhpIsset) {
                return;
            }
            /* logical operand context */
            if (ExpressionSemanticUtil.isUsedAsLogicalOperand(variable)) {
                return;
            }

            /* show stoppers: overriding the variable;  */
            if (parent instanceof AssignmentExpression) {
                final AssignmentExpression assignment = (AssignmentExpression) parent;
                final PsiElement candidate            = assignment.getVariable();
                if (candidate instanceof Variable && ((Variable) candidate).getName().equals(parameterName)) {
                    return;
                }
            }

            /* cases when NPE can be introduced: array access */
            if (parent instanceof ArrayAccessExpression) {
                final PsiElement container = ((ArrayAccessExpression) parent).getValue();
                if (variable == container) {
                    holder.registerProblem(variable, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
                continue;
            }

            /* cases when NPE can be introduced: member reference */
            if (parent instanceof MemberReference) {
                final MemberReference reference = (MemberReference) parent;
                final PsiElement subject        = reference.getClassReference();
                if (subject instanceof Variable && ((Variable) subject).getName().equals(parameterName)) {
                    holder.registerProblem(subject, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
                continue;
            }
            /* cases when NPE can be introduced: __invoke calls */
            if (OpenapiTypesUtil.isFunctionReference(parent) && variable == parent.getFirstChild()) {
                holder.registerProblem(variable, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                continue;
            }

            /* cases when null dispatched into to non-null parameter */
            if (parent instanceof ParameterList && parent.getParent() instanceof FunctionReference) {
                final FunctionReference reference = (FunctionReference) parent.getParent();
                final PsiElement resolved         = reference.resolve();
                if (resolved != null)  {
                    /* get the parameter definition */
                    final int position           = Arrays.asList(reference.getParameters()).indexOf(variable);
                    final Parameter[] parameters = ((Function) resolved).getParameters();
                    if (position >= parameters.length) {
                        continue;
                    }

                    /* lookup types, if no null declarations - report class-only declarations */
                    final Parameter parameter = parameters[position];
                    final Set<String> declaredTypes = new HashSet<>();
                    for (final String type: parameter.getDeclaredType().getTypes()) {
                        declaredTypes.add(Types.getType(type));
                    }
                    if (!declaredTypes.contains(Types.strNull) && !PhpLanguageUtil.isNull(parameter.getDefaultValue())) {
                        declaredTypes.remove(Types.strNull);

                        boolean isObject = !declaredTypes.isEmpty();
                        for (final String type : declaredTypes) {
                            if (!type.startsWith("\\") && !objectTypes.contains(type)) {
                                isObject = false;
                                break;
                            }
                        }
                        if (isObject) {
                            holder.registerProblem(variable, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                    declaredTypes.clear();
                }
                // continue;
            }
        }
    }
}
