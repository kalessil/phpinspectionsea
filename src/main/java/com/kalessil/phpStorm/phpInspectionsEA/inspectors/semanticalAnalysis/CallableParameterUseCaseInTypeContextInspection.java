package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
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
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
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

public class CallableParameterUseCaseInTypeContextInspection extends BasePhpInspection {
    private static final String messageNoSense                      = "Makes no sense, because it's always true according to annotations.";
    private static final String messageCheckViolatesDefinition      = "Makes no sense, because this type is not defined in annotations.";
    private static final String patternAssignmentViolatesDefinition = "New value type (%s%) is not in annotated types.";

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
                    final Set<String> parameterTypes = new HashSet<>();
                    for (final String type : parameter.getType().global(project).filterUnknown().getTypes()) {
                        final String typeNormalized = Types.getType(type);
                        if (typeNormalized.equals(Types.strMixed) || typeNormalized.equals(Types.strObject)) {
                            parameterTypes.clear();
                            break;
                        } else if (typeNormalized.equals(Types.strCallable)) {
                            parameterTypes.add(Types.strArray);
                            parameterTypes.add(Types.strString);
                        }
                        parameterTypes.add(typeNormalized);
                    }
                    if (parameterTypes.isEmpty()) {
                        continue;
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
                                        parameterTypes.contains(Types.strArray) ||
                                        parameterTypes.contains(Types.strIterable)
                                    ;
                                    break;
                                case "is_string":
                                    isTypeAnnounced = parameterTypes.contains(Types.strString);
                                    break;
                                case "is_bool":
                                    isTypeAnnounced = parameterTypes.contains(Types.strBoolean);
                                    break;
                                case "is_int":
                                    isTypeAnnounced = parameterTypes.contains(Types.strInteger);
                                    break;
                                case "is_float":
                                    isTypeAnnounced = parameterTypes.contains(Types.strFloat);
                                    break;
                                case "is_resource":
                                    isTypeAnnounced = parameterTypes.contains(Types.strResource);
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
                                    isReversedCheck
                                        = operation != null && PhpTokenTypes.opNOT == operation.getNode().getElementType();
                                }

                                final String message = isReversedCheck ? messageNoSense : messageCheckViolatesDefinition;
                                holder.registerProblem(functionCall, message, ProblemHighlightType.WEAK_WARNING);
                            }

                            continue;
                        }

                        /* case: assignments violating parameter definition */
                        /* TODO: method/strategy 2 */
                        if (parent instanceof AssignmentExpression) {
                            final AssignmentExpression assignment = (AssignmentExpression) parent;
                            final PhpPsiElement variable          = assignment.getVariable();
                            final PhpPsiElement value             = assignment.getValue();
                            if (variable instanceof Variable && value instanceof PhpTypedElement) {
                                final String variableName = variable.getName();
                                if (variableName != null && variableName.equals(parameterName)) {
                                    for (final String type : ((PhpTypedElement) value).getType().global(project).filterUnknown().getTypes()) {
                                        final String normalizedType = Types.getType(type);
                                        if (normalizedType.equals(Types.strMixed)) {
                                            continue;
                                        }

                                        final boolean isDefinitionViolation
                                            = !this.isTypeCompatibleWith(normalizedType, parameterTypes, index);
                                        if (isDefinitionViolation) {
                                            final String message
                                                = patternAssignmentViolatesDefinition.replace("%s%", normalizedType);
                                            holder.registerProblem(value, message, ProblemHighlightType.WEAK_WARNING);
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        /* TODO: analyze comparison operations */
                    }

                    parameterTypes.clear();
                }
            }

            private boolean isTypeCompatibleWith(@NotNull String type, @NotNull Set<String> allowedTypes, @NotNull PhpIndex index) {
                /* first case: implicit match */
                if (allowedTypes.contains(type)) {
                    return true;
                }

                /* second case: inherited classes/interfaces */
                final Set<String> possibleTypes = new HashSet<>();
                if (type.startsWith("\\")) {
                    final Set<PhpClass> foundClasses = new HashSet<>();
                    foundClasses.addAll(index.getClassesByFQN(type));
                    foundClasses.addAll(index.getInterfacesByFQN(type));
                    for (final PhpClass clazz : foundClasses) {
                        for (final PhpClass parent : InterfacesExtractUtil.getCrawlCompleteInheritanceTree(clazz, true)) {
                            possibleTypes.add(parent.getFQN());
                        }
                    }
                }

                return !possibleTypes.isEmpty() && allowedTypes.stream().anyMatch(possibleTypes::contains);
            }
        };
    }
}
