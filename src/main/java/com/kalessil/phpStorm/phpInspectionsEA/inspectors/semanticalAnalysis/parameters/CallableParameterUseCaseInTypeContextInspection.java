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
    private static final String messageNoSense               = "Makes no sense, because it's always true according to annotations.";
    private static final String messageViolationInCheck      = "Makes no sense, because this type is not defined in annotations.";
    private static final String patternViolationInAssignment = "New value type (%s%) is not in annotated types.";

    private static final Set<String> classReferences = new HashSet<>();
    static {
        classReferences.add("self");
        classReferences.add("static");
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
                    /* normalize parameter types, skip analysis when mixed or object appears */
                    final Set<String> paramTypes = new HashSet<>();
                    for (final String type : parameter.getType().global(project).filterUnknown().getTypes()) {
                        final String typeNormalized = Types.getType(type);
                        if (typeNormalized.equals(Types.strMixed) || typeNormalized.equals(Types.strObject)) {
                            paramTypes.clear();
                            break;
                        } else if (typeNormalized.equals(Types.strCallable)) {
                            paramTypes.add(Types.strArray);
                            paramTypes.add(Types.strString);
                            paramTypes.add("\\Closure");
                        }
                        paramTypes.add(typeNormalized);
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

                        /* check if is_* functions being used according to definitions */
                        /* TODO: method/strategy 1 */
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
                                    isTypeAnnounced =
                                        paramTypes.contains(Types.strNumber) || paramTypes.contains(Types.strString) ||
                                        paramTypes.contains(Types.strFloat) || paramTypes.contains(Types.strInteger);
                                    break;
                                case "is_callable":
                                    isTypeAnnounced =
                                        paramTypes.contains(Types.strCallable) || paramTypes.contains(Types.strArray) ||
                                        paramTypes.contains(Types.strString)   || paramTypes.contains("\\Closure");
                                    break;
                                case "is_object":
                                case "is_a":
                                    isTypeAnnounced =
                                        paramTypes.contains(Types.strObject) ||
                                        paramTypes.stream()
                                            .filter(t   -> !t.equals("\\Closure"))
                                            .anyMatch(t -> t.startsWith("\\") || classReferences.contains(t));
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
                            }
                            continue;
                        }

                        /* case: assignments violating parameter definition */
                        /* TODO: method/strategy 2 */
                        if (OpenapiTypesUtil.isAssignment(parent)) {
                            final AssignmentExpression assignment = (AssignmentExpression) parent;
                            final PhpPsiElement variable          = assignment.getVariable();
                            final PhpPsiElement value             = assignment.getValue();
                            if (variable instanceof Variable && value instanceof PhpTypedElement) {
                                final String variableName = variable.getName();
                                if (variableName != null && variableName.equals(parameterName)) {
                                    final PhpType resolvedType = OpenapiResolveUtil.resolveType((PhpTypedElement) value, project);
                                    final Set<String> resolved = resolvedType == null
                                            ? new HashSet<>()
                                            : resolvedType.filterUnknown().getTypes().stream()
                                                    .map(Types::getType)
                                                    .filter(type -> !type.equals(Types.strMixed))
                                                    .collect(Collectors.toSet());
                                    for (String type : resolved) {
                                        /* translate static/self into FQNs */
                                        if (classReferences.contains(type)) {
                                            PsiElement valueExtract = value;
                                            /* ` = <whatever> ?? <method call>` support */
                                            if (valueExtract instanceof BinaryExpression) {
                                                final BinaryExpression binary = (BinaryExpression) valueExtract;
                                                if (binary.getOperationType() == PhpTokenTypes.opCOALESCE) {
                                                    final PsiElement left = binary.getLeftOperand();
                                                    if (left != null && OpeanapiEquivalenceUtil.areEqual(variable, left)) {
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

                                        final boolean isViolation = !this.isTypeCompatibleWith(type, paramTypes, index);
                                        if (isViolation) {
                                            final String message = patternViolationInAssignment.replace("%s%", type);
                                            holder.registerProblem(value, message);
                                            break;
                                        }
                                    }
                                }
                            }
                            continue;
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
                                    /* ensure type expectations are met */
                                    if (requiredType != null) {
                                        if (operator == PhpTokenTypes.opIDENTICAL && !paramTypes.contains(requiredType)) {
                                            holder.registerProblem(binary, messageViolationInCheck);
                                        } else if (operator == PhpTokenTypes.opNOT_IDENTICAL && !paramTypes.contains(requiredType)) {
                                            holder.registerProblem(binary, messageNoSense);
                                        }
                                    }
                                }
                            }
                            /* TODO: number types can lead to true/false as well */
                        }

                        if (parent != null && !parameter.getDeclaredType().isEmpty()) {
                            InstanceOfCorrectnessStrategy.apply(holder, paramTypes, parent);
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
                    index.getAnyByFQN(type).forEach(clazz ->
                            InterfacesExtractUtil.getCrawlInheritanceTree(clazz, true).stream()
                                    .map(PhpNamedElement::getFQN)
                                    .forEach(possibleTypes::add)
                    );
                }

                return !possibleTypes.isEmpty() && allowedTypes.stream().anyMatch(possibleTypes::contains);
            }
        };
    }
}
