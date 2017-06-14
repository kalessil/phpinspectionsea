package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
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
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromSignatureResolvingUtil;
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
                    final String parameterName = parameter.getName();
                    String parameterType       = parameter.getType().toString();
                    if (
                        StringUtil.isEmpty(parameterName) ||
                        StringUtil.isEmpty(parameterType) ||
                        parameterType.contains("mixed") ||
                        parameterType.contains("#")
                    ) {
                        continue;
                    }
                    /* too lazy to do anything more elegant */
                    parameterType = parameterType.replace(Types.strCallable, "array|string|callable");


                    /* resolve types for parameter */
                    HashSet<String> parameterTypesResolved = new HashSet<>();
                    TypeFromSignatureResolvingUtil.resolveSignature(parameterType, (Function) scopeHolder, index, parameterTypesResolved);

                    final PhpAccessVariableInstruction[] usages
                        = PhpControlFlowUtil.getFollowingVariableAccessInstructions(entryPoint, parameterName, false);
                    for (final PhpAccessVariableInstruction instruction : usages) {
                        final PsiElement parent        = instruction.getAnchor().getParent();
                        final PsiElement callCandidate = null == parent ? null : parent.getParent();

                        /* check if is_* functions being used according to definitions */
                        if (OpenapiTypesUtil.isFunctionReference(callCandidate)) {
                            final FunctionReference functionCall = (FunctionReference) callCandidate;
                            final String functionName            = functionCall.getName();
                            if (functionName == null) {
                                continue;
                            }

                            final boolean isTypeAnnounced;
                            switch (functionName) {
                                case "is_array":
                                    isTypeAnnounced =
                                        parameterType.contains(Types.strArray) ||
                                        parameterType.contains(Types.strIterable) ||
                                        parameterType.contains("[]")
                                    ;
                                    break;
                                case "is_string":
                                    isTypeAnnounced = parameterType.contains(Types.strString);
                                    break;
                                case "is_bool":
                                    isTypeAnnounced = parameterType.contains(Types.strBoolean);
                                    break;
                                case "is_int":
                                    isTypeAnnounced = parameterType.contains(Types.strInteger);
                                    break;
                                case "is_float":
                                    isTypeAnnounced = parameterType.contains(Types.strFloat);
                                    break;
                                case "is_resource":
                                    isTypeAnnounced = parameterType.contains(Types.strResource);
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
                                            = !this.isTypeCompatibleWith(normalizedType, parameterTypesResolved, index);
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

                    parameterTypesResolved.clear();
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
