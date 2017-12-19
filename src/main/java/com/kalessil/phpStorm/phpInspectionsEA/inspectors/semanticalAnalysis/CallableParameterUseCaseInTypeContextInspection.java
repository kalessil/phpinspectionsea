package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
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
                this.inspectUsages(method.getParameters(), method);
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                this.inspectUsages(function.getParameters(), function);
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
                        }
                        paramTypes.add(typeNormalized);
                    }
                    if (paramTypes.isEmpty()) {
                        continue;
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
                        final PsiElement parent        = instruction.getAnchor().getParent();
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
                                    final Set<String> resolved =
                                            ((PhpTypedElement) value).getType().global(project)
                                                    .filterUnknown().getTypes().stream()
                                                        .map(Types::getType)
                                                        .filter(type -> !type.equals(Types.strMixed))
                                                        .collect(Collectors.toSet());
                                    final boolean valueIsMethodCall = value instanceof MethodReference;
                                    for (String type : resolved) {
                                        /* translate static/self into FQNs */
                                        if (valueIsMethodCall && classReferences.contains(type)) {
                                            final PsiElement source = OpenapiResolveUtil.resolveReference((MethodReference) value);
                                            if (source instanceof Method) {
                                                final PhpClass clazz = ((Method) source).getContainingClass();
                                                if (clazz != null && !clazz.isAnonymous()) {
                                                    type = clazz.getFQN();
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
                        }

                        /* TODO: analyze comparison operations */
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
                    index.getAnyByFQN(type).forEach(
                            clazz -> InterfacesExtractUtil.getCrawlInheritanceTree(clazz, true).stream()
                                        .map(PhpNamedElement::getFQN)
                                        .forEach(possibleTypes::add)
                    );
                }

                return !possibleTypes.isEmpty() && allowedTypes.stream().anyMatch(possibleTypes::contains);
            }
        };
    }
}
