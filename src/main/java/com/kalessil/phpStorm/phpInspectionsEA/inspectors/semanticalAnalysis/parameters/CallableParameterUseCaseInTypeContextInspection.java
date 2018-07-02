package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.parameters;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.parameters.strategy.InstanceOfCorrectnessStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

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
    private static final String messageNoSense               = "Makes no sense, because it's always true according to annotations.";
    private static final String messageTypeHint              = "Makes no sense, because of parameter type declaration.";
    private static final String messageViolationInCheck      = "Makes no sense, because this type is not defined in annotations.";
    private static final String patternViolationInAssignment = "New value type (%s%) is not in annotated types.";

    private static final Set<String> classReferences = new HashSet<>();
    static {
        classReferences.add(Types.strSelf);
        classReferences.add(Types.strStatic);
    }

    @NotNull
    public String getShortName() {
        return "CallableParameterUseCaseInTypeContextInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (EAUltimateApplicationComponent.areFeaturesEnabled()) {
                    this.inspectUsages(method.getParameters(), method);
                }
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (EAUltimateApplicationComponent.areFeaturesEnabled()) {
                    this.inspectUsages(function.getParameters(), function);
                }
            }

            private void inspectUsages(@NotNull Parameter[] parameters, @NotNull PhpScopeHolder scopeHolder) {
                final Project project                     = holder.getProject();
                final PhpIndex index                      = PhpIndex.getInstance(project);
                final PhpEntryPointInstruction entryPoint = scopeHolder.getControlFlow().getEntryPoint();

                for (final Parameter parameter : parameters) {
                    final boolean hasTypeDeclared    = !parameter.getDeclaredType().isEmpty();
                    /* normalize parameter types, skip analysis when mixed or object appears */
                    final Set<String> parameterTypes = this.getParameterType(parameter, project);
                    if (parameterTypes.isEmpty()) {
                        continue;
                    } else {
                        /* in some cases PhpStorm is not recognizing default value as parameter type */
                        final PsiElement defaultValue = parameter.getDefaultValue();
                        if (defaultValue instanceof PhpTypedElement) {
                            final PhpType defaultType = OpenapiResolveUtil.resolveType((PhpTypedElement) defaultValue, project);
                            if (defaultType != null) {
                                defaultType.filterUnknown().getTypes().forEach(t -> parameterTypes.add(Types.getType(t)));
                            }
                        }
                    }

                    /* false-positive: type is not resolved correctly, default null is taken */
                    if (parameterTypes.size() == 1 && parameterTypes.contains(Types.strNull)) {
                        final PsiElement defaultValue = parameter.getDefaultValue();
                        if (defaultValue != null && PhpLanguageUtil.isNull(defaultValue)) {
                            continue;
                        }
                    }

                    /* now find instructions operating on the parameter and perform analysis */
                    final String parameterName = parameter.getName();
                    final PhpAccessVariableInstruction[] usages
                        = PhpControlFlowUtil.getFollowingVariableAccessInstructions(entryPoint, parameterName, false);
                    for (final PhpAccessVariableInstruction instruction : usages) {
                        final PsiElement expression    = instruction.getAnchor();
                        final PsiElement parent        = expression.getParent();
                        final PsiElement callCandidate = null == parent ? null : parent.getParent();

                        /* Case 1: check if is_* functions being used according to definitions */
                        if (OpenapiTypesUtil.isFunctionReference(callCandidate)) {
                            final FunctionReference functionCall = (FunctionReference) callCandidate;
                            final String functionName            = functionCall.getName();
                            if (functionName == null) {
                                continue;
                            }
                            final PsiElement[] arguments    = functionCall.getParameters();
                            final boolean isTargetParameter = arguments.length > 0 && arguments[0] == expression;
                            if (!isTargetParameter) {
                                continue;
                            }

                            /* we expect that aliases usage has been fixed already */
                            final boolean isTypeAnnounced;
                            switch (functionName) {
                                case "is_array":
                                    isTypeAnnounced =
                                        parameterTypes.contains(Types.strArray) || parameterTypes.contains(Types.strIterable);
                                    break;
                                case "is_string":
                                    isTypeAnnounced = parameterTypes.contains(Types.strString);
                                    break;
                                case "is_bool":
                                    isTypeAnnounced = parameterTypes.contains(Types.strBoolean);
                                    break;
                                case "is_int":
                                    isTypeAnnounced =
                                        parameterTypes.contains(Types.strInteger) || parameterTypes.contains(Types.strNumber);
                                    break;
                                case "is_float":
                                    isTypeAnnounced =
                                        parameterTypes.contains(Types.strFloat) || parameterTypes.contains(Types.strNumber);
                                    break;
                                case "is_resource":
                                    isTypeAnnounced = parameterTypes.contains(Types.strResource);
                                    break;
                                case "is_numeric":
                                    isTypeAnnounced =
                                        parameterTypes.contains(Types.strNumber) || parameterTypes.contains(Types.strString) ||
                                        parameterTypes.contains(Types.strFloat)  || parameterTypes.contains(Types.strInteger);
                                    break;
                                case "is_scalar":
                                    isTypeAnnounced =
                                        parameterTypes.contains(Types.strNumber)  ||
                                        parameterTypes.contains(Types.strInteger) || parameterTypes.contains(Types.strFloat) ||
                                        parameterTypes.contains(Types.strString)  || parameterTypes.contains(Types.strBoolean);
                                    break;
                                case "is_callable":
                                    isTypeAnnounced =
                                        parameterTypes.contains(Types.strCallable) || parameterTypes.contains(Types.strArray) ||
                                        parameterTypes.contains(Types.strString)   || parameterTypes.contains("\\Closure");
                                    break;
                                case "is_object":
                                case "is_subclass_of":
                                    isTypeAnnounced =
                                        parameterTypes.contains(Types.strObject) ||
                                        parameterTypes.stream().anyMatch(t ->
                                            (t.startsWith("\\") && !t.equals("\\Closure")) || classReferences.contains(t)
                                        );
                                    break;
                                case "is_a":
                                    isTypeAnnounced =
                                        parameterTypes.contains(Types.strObject) || parameterTypes.contains(Types.strString) ||
                                        parameterTypes.stream().anyMatch(t ->
                                            (t.startsWith("\\") && !t.equals("\\Closure")) || classReferences.contains(t)
                                        );
                                    break;
                                case "is_iterable":
                                    isTypeAnnounced =
                                        parameterTypes.contains(Types.strArray) ||parameterTypes.contains(Types.strObject) ||
                                        parameterTypes.stream().anyMatch(t ->
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
                                holder.registerProblem(functionCall, isReversedCheck ? messageNoSense : messageViolationInCheck);
                            } else {
                                if (hasTypeDeclared && parameterTypes.size() == 1) {
                                    holder.registerProblem(functionCall, messageTypeHint);
                                }
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
                                        /* false-positives: core functions returning string|false, string|null */
                                        if (resolved.contains(Types.strString)) {
                                            if (resolved.contains(Types.strBoolean)) {
                                                final boolean isFunctionCall = OpenapiTypesUtil.isFunctionReference(value);
                                                if (isFunctionCall) {
                                                    resolved.remove(Types.strBoolean);
                                                }
                                            } else if (resolved.contains(Types.strNull)) {
                                                final boolean isFunctionCall = OpenapiTypesUtil.isFunctionReference(value);
                                                if (isFunctionCall) {
                                                    resolved.remove(Types.strNull);
                                                    /* preg_replace got better stub and brought lots of false-positives */
                                                    if ("preg_replace".equals(value.getName())) {
                                                        resolved.remove(Types.strArray);
                                                    }
                                                }
                                            }
                                        }
                                        /* false-positives: nullable objects */
                                        else if (resolved.contains(Types.strNull)) {
                                            final boolean isNullableObject = resolved.stream().anyMatch(t ->
                                                t.startsWith("\\") && !t.equals("\\Closure") || classReferences.contains(t)
                                            );
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
                                                        if (filteredTypes.size() == 1) {
                                                            type = filteredTypes.iterator().next();
                                                        }
                                                        filteredTypes.clear();
                                                    }
                                                }
                                            }
                                        }

                                        final boolean isViolation = !this.isTypeCompatibleWith(type, parameterTypes, index);
                                        if (isViolation) {
                                            final String message = patternViolationInAssignment.replace("%s%", type);
                                            holder.registerProblem(value, message);
                                            break;
                                        }
                                    }
                                    resolved.clear();
                                }
                            }
                            //continue;
                        } else if (parent instanceof BinaryExpression) {
                            final BinaryExpression binary = (BinaryExpression) parent;
                            final IElementType operator   = binary.getOperationType();
                            if (operator == PhpTokenTypes.opIDENTICAL || operator == PhpTokenTypes.opNOT_IDENTICAL) {
                                final PsiElement secondOperand = OpenapiElementsUtil.getSecondOperand(binary, expression);
                                if (secondOperand != null) {
                                    final String requiredType;
                                    /* identify the required type */
                                    if (secondOperand instanceof StringLiteralExpression) {
                                        requiredType = Types.strString;
                                    } else if (secondOperand instanceof ArrayCreationExpression) {
                                        requiredType = Types.strArray;
                                    } else if (PhpLanguageUtil.isNull(secondOperand)) {
                                        requiredType = Types.strNull;
                                    } else if (PhpLanguageUtil.isBoolean(secondOperand)) {
                                        requiredType = Types.strBoolean;
                                    } else {
                                        requiredType = null;
                                    }
                                    if (requiredType != null) {
                                        /* ensure generic types expectations are met */
                                        if (operator == PhpTokenTypes.opIDENTICAL && !parameterTypes.contains(requiredType)) {
                                            holder.registerProblem(binary, messageViolationInCheck);
                                        } else if (operator == PhpTokenTypes.opNOT_IDENTICAL && !parameterTypes.contains(requiredType)) {
                                            holder.registerProblem(binary, messageNoSense);
                                        }
                                    } else if (OpenapiTypesUtil.isNumber(secondOperand)) {
                                        /* ensure numeric types expectations are met */
                                        final boolean isNumber =
                                                parameterTypes.contains(Types.strFloat) ||
                                                parameterTypes.contains(Types.strInteger) ||
                                                parameterTypes.contains(Types.strNumber);
                                        if (!isNumber) {
                                            if (operator == PhpTokenTypes.opIDENTICAL) {
                                                holder.registerProblem(binary, messageViolationInCheck);
                                            } else if (operator == PhpTokenTypes.opNOT_IDENTICAL) {
                                                holder.registerProblem(binary, messageNoSense);
                                            }
                                        }
                                    }
                                }
                            } else if (operator == PhpTokenTypes.kwINSTANCEOF) {
                                if (!parameter.getDeclaredType().isEmpty()) {
                                    InstanceOfCorrectnessStrategy.apply(holder, parameterTypes, binary);
                                }
                            }
                            //continue;
                        }
                    }

                    parameterTypes.clear();
                }
            }

            private boolean isTypeCompatibleWith(@NotNull String given, @NotNull Set<String> allowed, @NotNull PhpIndex index) {
                /* first case: implicit match */
                if (allowed.contains(given)) {
                    return true;
                }

                /* second case: inherited classes/interfaces */
                final Set<String> possibleTypes = new HashSet<>();
                if (given.startsWith("\\")) {
                    index.getAnyByFQN(given)
                        .forEach(clazz ->
                            InterfacesExtractUtil.getCrawlInheritanceTree(clazz, true)
                                .forEach(c -> possibleTypes.add(c.getFQN()))
                        );
                }
                return !possibleTypes.isEmpty() && allowed.stream().anyMatch(possibleTypes::contains);
            }

            @NotNull
            private Set<String> getParameterType(@NotNull Parameter parameter, @NotNull Project project) {
                PhpType result = new PhpType();
                final PhpType resolved = OpenapiResolveUtil.resolveType(parameter, project);
                if (resolved != null) {
                    for (final String type : resolved.getTypes()) {
                        result.add(type);
                        final String normalizedType = Types.getType(type);
                        if (normalizedType.equals(Types.strCallable)) {
                            result.add(PhpType.ARRAY).add(PhpType.STRING).add(PhpType._CLOSURE);
                        } else if (normalizedType.equals(Types.strIterable)) {
                            result.add(PhpType.ARRAY).add("\\Traversable");
                        } else if (normalizedType.equals(Types.strMixed) || normalizedType.equals(Types.strObject)) {
                            result = new PhpType();
                            break;
                        }
                    }
                }
                return result.isEmpty()
                        ? new HashSet<>()
                        : result.filterUnknown().getTypes().stream().map(Types::getType).collect(Collectors.toSet());
            }
        };
    }
}
