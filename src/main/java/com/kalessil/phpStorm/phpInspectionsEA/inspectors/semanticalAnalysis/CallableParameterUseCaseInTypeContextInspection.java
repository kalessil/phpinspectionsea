package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpClassHierarchyUtils;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpIndexUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPlatformResolverUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromSignatureResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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

            private void inspectUsages(Parameter[] parameters, PhpScopeHolder scopeHolder) {
                final PhpIndex index                      = PhpIndex.getInstance(holder.getProject());
                final PhpEntryPointInstruction entryPoint = scopeHolder.getControlFlow().getEntryPoint();

                for (final Parameter parameter : parameters) {
                    final String parameterName = parameter.getName();
                    String parameterType       = parameter.getType().toString();
                    if (
                        StringUtil.isEmpty(parameterName) ||
                        StringUtil.isEmpty(parameterType) ||
                        parameterType.contains("mixed") ||   /* fair enough */
                        parameterType.contains("#")          /* TODO: types lookup */
                    ) {
                        continue;
                    }
                    /* too lazy to do anything more elegant */
                    parameterType = parameterType.replace(Types.strCallable, "array|string|callable");


                    /* resolve types for parameter */
                    HashSet<String> parameterTypesResolved = new HashSet<>();
                    TypeFromSignatureResolvingUtil.resolveSignature(parameterType, (Function) scopeHolder, index, parameterTypesResolved);

                    PhpAccessVariableInstruction[] usages = PhpControlFlowUtil.getFollowingVariableAccessInstructions(entryPoint, parameterName, false);
                    if (usages.length == 0) {
                        continue;
                    }

                    /* TODO: dedicate to method */
                    for (final PhpAccessVariableInstruction instruction : usages) {
                        final PsiElement expression = instruction.getAnchor().getParent();

                        /* inspect type checks are used */
                        if (
                            expression instanceof ParameterList &&
                            expression.getParent() instanceof FunctionReference
                        ) {
                            final FunctionReference functionCall = (FunctionReference) expression.getParent();
                            final String functionName            = functionCall.getName();
                            if (StringUtil.isEmpty(functionName)) {
                                continue;
                            }

                            boolean isReversedCheck = false;
                            if (functionCall.getParent() instanceof UnaryExpression) {
                                final UnaryExpression callWrapper = (UnaryExpression) functionCall.getParent();
                                isReversedCheck = (
                                    null != callWrapper.getOperation() &&
                                    null != callWrapper.getOperation().getNode() &&
                                    PhpTokenTypes.opNOT == callWrapper.getOperation().getNode().getElementType()
                                );
                            }

                            boolean isCallHasNoSense;
                            boolean isCallViolatesDefinition;
                            boolean isTypeAnnounced;

                            if (functionName.equals("is_array"))
                                isTypeAnnounced = (
                                    parameterType.contains(Types.strArray) ||
                                    parameterType.contains(Types.strIterable) ||
                                    parameterType.contains("[]")
                                );
                            else if (functionName.equals("is_string"))
                                isTypeAnnounced = parameterType.contains(Types.strString);
                            else if (functionName.equals("is_bool"))
                                isTypeAnnounced = parameterType.contains(Types.strBoolean);
                            else if (functionName.equals("is_int"))
                                isTypeAnnounced = parameterType.contains(Types.strInteger);
                            else if (functionName.equals("is_float"))
                                isTypeAnnounced = parameterType.contains(Types.strFloat);
                            else if (functionName.equals("is_resource"))
                                isTypeAnnounced = parameterType.contains(Types.strResource);
                            else
                                continue;

                            isCallHasNoSense = !isTypeAnnounced && isReversedCheck;
                            if (isCallHasNoSense) {
                                holder.registerProblem(functionCall, messageNoSense, ProblemHighlightType.WEAK_WARNING);
                                continue;
                            }

                            isCallViolatesDefinition = !isTypeAnnounced;
                            if (isCallViolatesDefinition) {
                                holder.registerProblem(functionCall, messageCheckViolatesDefinition, ProblemHighlightType.WEAK_WARNING);
                                continue;
                            }

                            continue;
                        }

                        /* check if assignments not violating defined interfaces */
                        /* TODO: dedicate to method */
                        if (expression instanceof AssignmentExpression) {
                            final AssignmentExpression assignment = (AssignmentExpression) expression;
                            final PhpPsiElement variable          = assignment.getVariable();
                            final PhpPsiElement value             = assignment.getValue();
                            if (null == variable || null == value) {
                                continue;
                            }

                            if (
                                variable instanceof Variable &&
                                null != variable.getName() && variable.getName().equals(parameterName)
                            ) {
                                final HashSet<String> typesResolved = new HashSet<>();
                                TypeFromPlatformResolverUtil.resolveExpressionType(value, typesResolved);

                                boolean isCallViolatesDefinition;
                                for (final String type : typesResolved) {
                                    if (
                                        /* custom resolving artifacts */
                                        type.equals(Types.strResolvingAbortedOnPsiLevel) ||
                                        type.equals(Types.strClassNotResolved) ||
                                        /* we should not report mixed, bad annotation => bad analysis */
                                        type.equals(Types.strMixed) ||
                                        /* sometimes types containing both keyword and resolved class */
                                        type.equals(Types.strStatic) ||
                                        type.equals(Types.strSelf)
                                    ) {
                                        continue;
                                    }

                                    isCallViolatesDefinition = (!this.isTypeCompatibleWith(type, parameterTypesResolved, index));
                                    if (isCallViolatesDefinition) {
                                        final String message = patternAssignmentViolatesDefinition.replace("%s%", type);
                                        holder.registerProblem(value, message, ProblemHighlightType.WEAK_WARNING);

                                        break;
                                    }
                                }
                                typesResolved.clear();
                            }
                        }

                        /* TODO: can be analysed comparison operations */
                    }

                    parameterTypesResolved.clear();
                }
            }

            private boolean isTypeCompatibleWith(@NotNull String type, @NotNull Set<String> allowedTypes, @NotNull PhpIndex index) {
                /* identical definitions */
                if (allowedTypes.contains(type)) {
                    return true;
                }

                /* classes/interfaces */
                if (type.length() > 0 && type.charAt(0) == '\\') {
                    /* collect classes/interfaces for type we going to analyse for compatibility */
                    final Collection<PhpClass> classesToTest = PhpIndexUtil.getObjectInterfaces(type, index, false);
                    if (classesToTest.isEmpty()) {
                        return false;
                    }

                    /* collect parent classes/interfaces for bulk check */
                    final List<PhpClass> classesAllowed = new ArrayList<>();
                    for (final String allowedType: allowedTypes) {
                        if (
                            allowedType.length() == 0 || allowedType.charAt(0) != '\\' ||
                            allowedType.equals(Types.strClassNotResolved)
                        ) {
                            continue;
                        }

                        classesAllowed.addAll(PhpIndexUtil.getObjectInterfaces(allowedType, index, false));
                    }

                    /* run test through 2 sets */
                    for (final PhpClass testSubject: classesToTest) {
                        /* collect hierarchy chain for interface inheritance checks */
                        final List<PhpClass> testSubjectInheritanceChain = new ArrayList<>();
                        testSubjectInheritanceChain.add(testSubject);
                        Collections.addAll(testSubjectInheritanceChain, testSubject.getSupers());

                        for (final PhpClass testAgainst: classesAllowed) {
                            /* TODO: not clear why, but isSuperClass receives a null on VCS commit */
                            if (null == testAgainst || null == testSubject) {
                                continue;
                            }

                            /* interface implementation checks */
                            if (testAgainst.isInterface()) {
                                /*
                                 * PhpClassHierarchyUtils.isSuperClass not handling interfaces,
                                 * so scan complete inheritance tree
                                 */
                                for (final PhpClass oneClassForInterfaceCheck : testSubjectInheritanceChain) {
                                    for (final PhpClass interfaze : oneClassForInterfaceCheck.getImplementedInterfaces()) {
                                        if (interfaze.getFQN().equals(testAgainst.getFQN())) {
                                            return true;
                                        }
                                    }
                                }
                            }

                            /* class-hierarchy checks */
                            if (PhpClassHierarchyUtils.isSuperClass(testAgainst, testSubject, true)) {
                                return true;
                            }
                        }

                        testSubjectInheritanceChain.clear();
                    }

                    return false;
                }

                return false;
            }
        };
    }
}
