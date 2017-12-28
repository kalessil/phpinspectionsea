package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class NullableVariablesStrategy {
    private static final String message = "Null pointer exception may occur here.";

    private static final Set<String> objectTypes = new HashSet<>();
    static {
        objectTypes.add(Types.strSelf);
        objectTypes.add(Types.strStatic);
        objectTypes.add(Types.strObject);
    }

    public static void applyToLocalVariables(@NotNull Function function, @NotNull ProblemsHolder holder) {
        final Set<String> parameters = Arrays.stream(function.getParameters()).map(Parameter::getName).collect(Collectors.toSet());
        final GroupStatement body    = ExpressionSemanticUtil.getGroupStatement(function);

        /* group variables assignments, except parameters */
        final Map<String, List<AssignmentExpression>> assignments = new HashMap<>();
        for (final Variable variable : PsiTreeUtil.findChildrenOfType(body, Variable.class)) {
            final String variableName = variable.getName();
            final PsiElement parent   = variable.getParent();
            if (parent instanceof AssignmentExpression && !parameters.contains(variableName)) {
                final AssignmentExpression assignment = (AssignmentExpression) parent;
                if (
                    assignment.getVariable() == variable &&
                    OpenapiTypesUtil.isStatementImpl(assignment.getParent()) &&
                    !(assignment.getValue() instanceof FieldReference) /* TODO: strict method reference type check */
                ) {
                    assignments.computeIfAbsent(variableName, v -> new ArrayList<>()).add(assignment);
                }
            }
        }

        /* check if the variable has been written only once, inspect when null/void values are possible */
        final PhpEntryPointInstruction controlFlowStart = function.getControlFlow().getEntryPoint();
        final Project project                           = holder.getProject();
        for (final Map.Entry<String, List<AssignmentExpression>> pair : assignments.entrySet()) {
            final List<AssignmentExpression> variableAssignments = pair.getValue();
            if (!variableAssignments.isEmpty()) {
                final AssignmentExpression assignment = variableAssignments.iterator().next();
                if (isNullableResult(assignment, project)) {
                    apply(pair.getKey(), assignment, controlFlowStart, holder);
                }
                variableAssignments.clear();
            }
        }
        assignments.clear();
    }

    static private boolean isNullableResult(@NotNull AssignmentExpression assignment, @NotNull Project project) {
        boolean result                   = false;
        final PsiElement assignmentValue = assignment.getValue();
        if (assignmentValue instanceof PhpTypedElement) {
            final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) assignmentValue, project);
            if (resolved != null) {
                final Set<String> types = resolved.filterUnknown().getTypes().stream()
                        .map(Types::getType)
                        .collect(Collectors.toSet());
                if (types.contains(Types.strNull) || types.contains(Types.strVoid)) {
                    types.remove(Types.strNull);
                    types.remove(Types.strVoid);
                    if (types.stream().filter(t -> !t.startsWith("\\") && !objectTypes.contains(t)).count() == 0) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    public static void applyToParameters(@NotNull Function function, @NotNull ProblemsHolder holder) {
        final PhpEntryPointInstruction controlFlowStart = function.getControlFlow().getEntryPoint();
        for (final Parameter parameter : function.getParameters()) {
            final Set<String> declaredTypes = parameter.getDeclaredType().getTypes().stream()
                    .map(Types::getType)
                    .collect(Collectors.toSet());
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
                    apply(parameter.getName(), null, controlFlowStart, holder);
                }
            }
            declaredTypes.clear();
        }
    }

    private static void apply(
        @NotNull String variableName,
        @Nullable AssignmentExpression variableDeclaration,
        @NotNull PhpEntryPointInstruction controlFlowStart,
        @NotNull ProblemsHolder holder
    ) {
        final Project project                     = holder.getProject();
        final PhpAccessVariableInstruction[] uses = PhpControlFlowUtil.getFollowingVariableAccessInstructions(controlFlowStart, variableName, false);
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
                if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operation)) {
                    final PsiElement secondOperand = OpenapiElementsUtil.getSecondOperand(expression, variable);
                    if (PhpLanguageUtil.isNull(secondOperand)) {
                        return;
                    }
                    continue;
                }
            }

            /* non-implicit null comparisons; `else if` here would change semantics */
            if (parent instanceof PhpEmpty || parent instanceof PhpIsset || ExpressionSemanticUtil.isUsedAsLogicalOperand(variable)) {
                return;
            }
            /* re-defined in catch-statements */
            else if (parent instanceof Catch) {
                return;
            }
            /* PhpUnit-specific null check */
            else if (parent instanceof ParameterList && parent.getParent() instanceof MethodReference) {
                final String methodName = ((MethodReference) parent.getParent()).getName();
                if (methodName != null && methodName.equals("assertNotNull")) {
                    return;
                }
            }

            /* show stoppers: overriding the variable; except the variable declarations of course */
            if (parent instanceof AssignmentExpression) {
                if (parent == variableDeclaration) {
                    continue;
                }

                final AssignmentExpression assignment = (AssignmentExpression) parent;
                final PsiElement candidate            = assignment.getVariable();
                if (candidate instanceof Variable && ((Variable) candidate).getName().equals(variableName)) {
                    if (!isNullableResult(assignment, project)) {
                        return;
                    }
                }
            }
            /* cases when NPE can be introduced: array access */
            else if (parent instanceof ArrayAccessExpression) {
                final PsiElement container = ((ArrayAccessExpression) parent).getValue();
                if (variable == container) {
                    holder.registerProblem(variable, message);
                }
            }
            /* cases when NPE can be introduced: member reference */
            else if (parent instanceof MemberReference) {
                final MemberReference reference = (MemberReference) parent;
                final PsiElement subject        = reference.getClassReference();
                if (subject instanceof Variable && ((Variable) subject).getName().equals(variableName)) {
                    holder.registerProblem(subject, message);
                }
            }
            /* cases when NPE can be introduced: __invoke calls */
            else if (OpenapiTypesUtil.isFunctionReference(parent) && variable == parent.getFirstChild()) {
                holder.registerProblem(variable, message);
            }
            /* cases when NPE can be introduced: clone operator */
            else if (parent instanceof UnaryExpression) {
                if (OpenapiTypesUtil.is(((UnaryExpression) parent).getOperation(), PhpTokenTypes.kwCLONE)) {
                    holder.registerProblem(variable, message);
                }
            }
            /* cases when null dispatched into to non-null parameter */
            else if (parent instanceof ParameterList && parent.getParent() instanceof FunctionReference) {
                final FunctionReference reference = (FunctionReference) parent.getParent();
                final PsiElement resolved         = OpenapiResolveUtil.resolveReference(reference);
                if (resolved != null)  {
                    /* get the parameter definition */
                    final int position           = Arrays.asList(reference.getParameters()).indexOf(variable);
                    final Parameter[] parameters = ((Function) resolved).getParameters();
                    if (position >= parameters.length) {
                        continue;
                    }

                    /* lookup types, if no null declarations - report class-only declarations */
                    final Parameter parameter       = parameters[position];
                    final Set<String> declaredTypes = parameter.getDeclaredType().getTypes().stream()
                            .map(Types::getType)
                            .collect(Collectors.toSet());
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
                            holder.registerProblem(variable, message);
                        }
                    }
                    declaredTypes.clear();
                }
            }
        }
    }
}
