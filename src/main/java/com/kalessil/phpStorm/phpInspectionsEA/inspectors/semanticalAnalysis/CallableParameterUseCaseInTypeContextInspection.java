package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class CallableParameterUseCaseInTypeContextInspection extends BasePhpInspection {
    private static final String messageNoSense               = "Makes no sense, because it's always true according to resolved type. Ensure the parameter is not reused.";
    private static final String messageViolationInCheck      = "Makes no sense, because it's always false according to resolved type. Ensure the parameter is not reused.";
    private static final String patternViolationInAssignment = "New value type (%s) is not matching the resolved parameter type and might introduce types-related false-positives.";

    private static final Set<String> classReferences = new HashSet<>();
    static {
        classReferences.add(Types.strSelf);
        classReferences.add(Types.strStatic);
    }

    @NotNull
    @Override
    public String getShortName() {
        return "CallableParameterUseCaseInTypeContextInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Callable parameter usage violates definition";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (!this.isTestContext(method)) {
                    this.inspectUsages(method.getParameters(), method);
                }
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (!this.isTestContext(function)) {
                    this.inspectUsages(function.getParameters(), function);
                }
            }

            private void inspectUsages(@NotNull Parameter[] parameters, @NotNull PhpScopeHolder scopeHolder) {
                final Project project                     = holder.getProject();
                final PhpIndex index                      = PhpIndex.getInstance(project);
                final PhpEntryPointInstruction entryPoint = scopeHolder.getControlFlow().getEntryPoint();

                for (final Parameter parameter : parameters) {
                    /* normalize parameter types, skip analysis when mixed or object appears */
                    final Set<String> paramTypes = new HashSet<>();
                    final PhpType parameterType  = OpenapiResolveUtil.resolveType(parameter, project);
                    if (parameterType != null) {
                        label:
                        for (final String type : parameterType.filterUnknown().getTypes()) {
                            final String typeNormalized = Types.getType(type);
                            switch (typeNormalized) {
                                case Types.strMixed:
                                case Types.strObject:
                                    paramTypes.clear();
                                    break label;
                                case Types.strCallable:
                                    paramTypes.add(Types.strArray);
                                    paramTypes.add(Types.strString);
                                    paramTypes.add("\\Closure");
                                    break;
                                case Types.strIterable:
                                    paramTypes.add(Types.strArray);
                                    paramTypes.add("\\Traversable");
                                    break;
                            }
                            paramTypes.add(typeNormalized);
                        }
                    }
                    if (paramTypes.isEmpty()) {
                        continue;
                    } else {
                        /* in some case PhpStorm is not recognizing default value as parameter type */
                        final PsiElement defaultValue = parameter.getDefaultValue();
                        if (defaultValue instanceof PhpTypedElement) {
                            final PhpType defaultType = OpenapiResolveUtil.resolveType((PhpTypedElement) defaultValue, project);
                            if (defaultType != null) {
                                defaultType.filterUnknown().getTypes().forEach(t -> paramTypes.add(Types.getType(t)));
                            }
                        }
                    }

                    /* false-positive: type is not resolved correctly, default null is taken */
                    if (paramTypes.size() == 1 && paramTypes.contains(Types.strNull)) {
                        final PsiElement defaultValue = parameter.getDefaultValue();
                        if (PhpLanguageUtil.isNull(defaultValue)) {
                            continue;
                        }
                    }

                    /* now find instructions operating on the parameter and perform analysis */
                    final String parameterName = parameter.getName();
                    for (final PhpAccessVariableInstruction instruction : OpenapiControlFlowUtil.getFollowingVariableAccessInstructions(entryPoint, parameterName)) {
                        final PsiElement parent        = instruction.getAnchor().getParent();
                        final PsiElement callCandidate = null == parent ? null : parent.getParent();

                        /* Case 1: check if is_* functions being used according to definitions */
                        if (OpenapiTypesUtil.isFunctionReference(callCandidate)) {
                            final FunctionReference functionCall = (FunctionReference) callCandidate;
                            final String functionName            = functionCall.getName();
                            if (functionName == null) {
                                continue;
                            }

                            /* we expect that aliases usage has been fixed already */
                            final boolean isTypeAnnounced;
                            switch (functionName) {
                                case "is_array":
                                    isTypeAnnounced =
                                        paramTypes.contains(Types.strArray) || paramTypes.contains(Types.strIterable);
                                    break;
                                case "is_string":
                                    isTypeAnnounced = paramTypes.contains(Types.strString);
                                    break;
                                case "is_bool":
                                    isTypeAnnounced = paramTypes.contains(Types.strBoolean);
                                    break;
                                case "is_int":
                                    isTypeAnnounced =
                                        paramTypes.contains(Types.strInteger) || paramTypes.contains(Types.strNumber);
                                    break;
                                case "is_float":
                                    isTypeAnnounced =
                                        paramTypes.contains(Types.strFloat) || paramTypes.contains(Types.strNumber);
                                    break;
                                case "is_resource":
                                    isTypeAnnounced = paramTypes.contains(Types.strResource);
                                    break;
                                case "is_numeric":
                                    if (paramTypes.contains(Types.strString)) { continue; }
                                    isTypeAnnounced =
                                        paramTypes.contains(Types.strNumber) ||
                                        paramTypes.contains(Types.strFloat)  || paramTypes.contains(Types.strInteger);
                                    break;
                                case "is_callable":
                                    isTypeAnnounced =
                                        paramTypes.contains(Types.strCallable) || paramTypes.contains(Types.strArray) ||
                                        paramTypes.contains(Types.strString)   || paramTypes.contains("\\Closure");
                                    break;
                                case "is_object":
                                    isTypeAnnounced =
                                        paramTypes.contains(Types.strObject) || paramTypes.contains(Types.strCallable) ||
                                        paramTypes.stream().anyMatch(t -> classReferences.contains(t) ||
                                                                          (t.startsWith("\\") && !t.equals("\\Closure")));
                                    break;
                                case "is_a":
                                    isTypeAnnounced =
                                        paramTypes.contains(Types.strObject) || paramTypes.contains(Types.strString) ||
                                        paramTypes.stream().anyMatch(t ->
                                            (t.startsWith("\\") && !t.equals("\\Closure")) || classReferences.contains(t)
                                        );
                                    break;
                                default:
                                    continue;
                            }

                            /* cases: call makes no sense, violation of defined types set */
                            if (!isTypeAnnounced) {
                                final PsiElement callParent = functionCall.getParent();
                                boolean isReversedCheck     = false;
                                if (callParent instanceof UnaryExpression) {
                                    final PsiElement operation = ((UnaryExpression) callParent).getOperation();
                                    isReversedCheck            = OpenapiTypesUtil.is(operation, PhpTokenTypes.opNOT);
                                }
                                holder.registerProblem(
                                        functionCall,
                                        MessagesPresentationUtil.prefixWithEa(isReversedCheck ? messageNoSense : messageViolationInCheck)
                                );
                            }
                            continue;
                        }

                        /* Case 2: assignments violating parameter definition */
                        if (OpenapiTypesUtil.isAssignment(parent)) {
                            final AssignmentExpression assignment = (AssignmentExpression) parent;
                            final PhpPsiElement variable          = assignment.getVariable();
                            final PhpPsiElement value             = assignment.getValue();
                            if (variable instanceof Variable && value instanceof PhpTypedElement) {
                                final String variableName = variable.getName();
                                if (variableName != null && variableName.equals(parameterName)) {
                                    final PhpType resolvedType = OpenapiResolveUtil.resolveType((PhpTypedElement) value, project);
                                    final Set<String> resolved = new HashSet<>();
                                    if (resolvedType != null) {
                                        resolvedType.filterUnknown().getTypes().forEach(t -> resolved.add(Types.getType(t)));
                                    }

                                    if (resolved.size() >= 2) {
                                        /* false-positives: core functions returning string|array & false|null */
                                        if (resolved.contains(Types.strString) || resolved.contains(Types.strArray)) {
                                            if (resolved.contains(Types.strBoolean)) {
                                                final boolean isFunctionCall = OpenapiTypesUtil.isFunctionReference(value);
                                                if (isFunctionCall) {
                                                    resolved.remove(Types.strBoolean);
                                                }
                                            } else if (resolved.contains(Types.strNull)) {
                                                final boolean isFunctionCall = OpenapiTypesUtil.isFunctionReference(value);
                                                if (isFunctionCall) {
                                                    resolved.remove(Types.strNull);
                                                }
                                            }
                                        }
                                        /* false-positives: nullable objects */
                                        else if (resolved.contains(Types.strNull)) {
                                            final boolean isNullableObject = paramTypes.stream()
                                                    .anyMatch(t -> classReferences.contains(t) ||
                                                                   t.startsWith("\\") && !t.equals("\\Closure"));
                                            if (isNullableObject) {
                                                resolved.remove(Types.strNull);
                                            }
                                        }
                                    }

                                    resolved.remove(Types.strMixed);
                                    for (String type : resolved) {
                                        /* translate static/self into FQNs */
                                        if (classReferences.contains(type)) {
                                            PsiElement valueExtract = value;
                                            /* ` = <whatever> ?? <method call>` support */
                                            if (valueExtract instanceof BinaryExpression) {
                                                final BinaryExpression binary = (BinaryExpression) valueExtract;
                                                if (binary.getOperationType() == PhpTokenTypes.opCOALESCE) {
                                                    final PsiElement left = binary.getLeftOperand();
                                                    if (left != null && OpenapiEquivalenceUtil.areEqual(variable, left)) {
                                                        final PsiElement right = binary.getRightOperand();
                                                        if (right != null) {
                                                            valueExtract = right;
                                                        }
                                                    }
                                                }
                                            }
                                            /* method call lookup */
                                            if (valueExtract instanceof MethodReference) {
                                                final PsiElement base = valueExtract.getFirstChild();
                                                if (base instanceof ClassReference) {
                                                    final PsiElement resolvedClass = OpenapiResolveUtil.resolveReference((ClassReference) base);
                                                    if (resolvedClass instanceof PhpClass) {
                                                        type = ((PhpClass) resolvedClass).getFQN();
                                                    }
                                                } else if (base instanceof PhpTypedElement) {
                                                    final PhpType clazzTypes = OpenapiResolveUtil.resolveType((PhpTypedElement) base, project);
                                                    if (clazzTypes != null) {
                                                        final Set<String> filteredTypes = clazzTypes.filterUnknown().getTypes().stream()
                                                                .map(Types::getType)
                                                                .filter(t -> t.startsWith("\\"))
                                                                .collect(Collectors.toSet());
                                                        final int filteredTypesCount = filteredTypes.size();
                                                        /* clear resolved class or interface + class */
                                                        if (filteredTypesCount == 1 || filteredTypesCount == 2) {
                                                            type = filteredTypes.iterator().next();
                                                        }
                                                        filteredTypes.clear();
                                                    }
                                                }
                                            }
                                            /* translate static/self into FQNs didn't work, skip */
                                            if (classReferences.contains(type)) {
                                                continue;
                                            }
                                        }

                                        final boolean isViolation = !this.isTypeCompatibleWith(type, paramTypes, index);
                                        if (isViolation) {
                                            holder.registerProblem(
                                                    value,
                                                    String.format(MessagesPresentationUtil.prefixWithEa(patternViolationInAssignment), type)
                                            );
                                            break;
                                        }
                                    }
                                    resolved.clear();
                                }
                            }
                        }
                    }

                    paramTypes.clear();
                }
            }

            private boolean isTypeCompatibleWith(
                    @NotNull String type,
                    @NotNull Collection<String> allowedTypes,
                    @NotNull PhpIndex index
            ) {
                /* first case: implicit match */
                if (allowedTypes.contains(type)) {
                    return true;
                }

                /* second case: inherited classes/interfaces */
                final Set<String> possibleTypes = new HashSet<>();
                if (type.startsWith("\\")) {
                    index.getAnyByFQN(type)
                        .forEach(clazz ->
                            InterfacesExtractUtil.getCrawlInheritanceTree(clazz, true)
                                .forEach(c -> possibleTypes.add(c.getFQN()))
                        );
                }

                return !possibleTypes.isEmpty() && allowedTypes.stream().anyMatch(possibleTypes::contains);
            }
        };
    }
}
