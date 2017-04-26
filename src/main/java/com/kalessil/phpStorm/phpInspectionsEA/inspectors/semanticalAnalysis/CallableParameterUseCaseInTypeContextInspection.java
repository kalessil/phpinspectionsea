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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class CallableParameterUseCaseInTypeContextInspection extends BasePhpInspection {
    private static final String strProblemNoSense                      = "Makes no sense, because it's always true according to annotations.";
    private static final String strProblemCheckViolatesDefinition      = "Makes no sense, because this type is not defined in annotations.";
    private static final String strProblemAssignmentViolatesDefinition = "New value type (%s%) is not in annotated types.";

    @NotNull
    public String getShortName() {
        return "CallableParameterUseCaseInTypeContextInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                this.inspectUsages(method.getParameters(), method);
            }

            public void visitPhpFunction(Function function) {
                this.inspectUsages(function.getParameters(), function);
            }

            private void inspectUsages(Parameter[] arrParameters, PhpScopeHolder objScopeHolder) {
                PhpIndex objIndex = PhpIndex.getInstance(holder.getProject());
                PhpEntryPointInstruction objEntryPoint = objScopeHolder.getControlFlow().getEntryPoint();

                for (Parameter objParameter : arrParameters) {
                    String strParameterName = objParameter.getName();
                    String strParameterType = objParameter.getType().toString();
                    if (
                        StringUtil.isEmpty(strParameterName) ||
                        StringUtil.isEmpty(strParameterType) ||
                        strParameterType.contains("mixed") ||   /* fair enough */
                        strParameterType.contains("#")          /* TODO: types lookup */
                    ) {
                        continue;
                    }
                    /* too lazy to do anything more elegant */
                    strParameterType = strParameterType.replace(Types.strCallable, "array|string|callable");


                    /* resolve types for parameter */
                    HashSet<String> objParameterTypesResolved = new HashSet<>();
                    TypeFromSignatureResolvingUtil.resolveSignature(strParameterType, (Function) objScopeHolder, objIndex, objParameterTypesResolved);

                    PhpAccessVariableInstruction[] arrUsages = PhpControlFlowUtil.getFollowingVariableAccessInstructions(objEntryPoint, strParameterName, false);
                    if (arrUsages.length == 0) {
                        continue;
                    }

                    PsiElement objExpression;
                    FunctionReference objFunctionCall;
                    /* TODO: dedicate to method */
                    for (PhpAccessVariableInstruction objInstruction : arrUsages) {
                        objExpression = objInstruction.getAnchor().getParent();

                        /* inspect type checks are used */
                        if (
                            objExpression instanceof ParameterList &&
                            objExpression.getParent() instanceof FunctionReference
                        ) {
                            objFunctionCall = (FunctionReference) objExpression.getParent();
                            String strFunctionName = objFunctionCall.getName();
                            if (StringUtil.isEmpty(strFunctionName)) {
                                continue;
                            }

                            boolean isReversedCheck = false;
                            if (objFunctionCall.getParent() instanceof UnaryExpression) {
                                UnaryExpression objCallWrapper = (UnaryExpression) objFunctionCall.getParent();
                                isReversedCheck = (
                                    null != objCallWrapper.getOperation() &&
                                    null != objCallWrapper.getOperation().getNode() &&
                                    PhpTokenTypes.opNOT == objCallWrapper.getOperation().getNode().getElementType()
                                );
                            }

                            boolean isCallHasNoSense;
                            boolean isCallViolatesDefinition;
                            boolean isTypeAnnounced;

                            if (strFunctionName.equals("is_array"))
                                isTypeAnnounced = (
                                    strParameterType.contains(Types.strArray) ||
                                    strParameterType.contains(Types.strIterable) ||
                                    strParameterType.contains("[]")
                                );
                            else if (strFunctionName.equals("is_string"))
                                isTypeAnnounced = strParameterType.contains(Types.strString);
                            else if (strFunctionName.equals("is_bool"))
                                isTypeAnnounced = strParameterType.contains(Types.strBoolean);
                            else if (strFunctionName.equals("is_int"))
                                isTypeAnnounced = strParameterType.contains(Types.strInteger);
                            else if (strFunctionName.equals("is_float"))
                                isTypeAnnounced = strParameterType.contains(Types.strFloat);
                            else if (strFunctionName.equals("is_resource"))
                                isTypeAnnounced = strParameterType.contains(Types.strResource);
                            else
                                continue;

                            isCallHasNoSense = !isTypeAnnounced && isReversedCheck;
                            if (isCallHasNoSense) {
                                holder.registerProblem(objFunctionCall, strProblemNoSense, ProblemHighlightType.WEAK_WARNING);
                                continue;
                            }

                            isCallViolatesDefinition = !isTypeAnnounced;
                            if (isCallViolatesDefinition) {
                                holder.registerProblem(objFunctionCall, strProblemCheckViolatesDefinition, ProblemHighlightType.WEAK_WARNING);
                                continue;
                            }

                            continue;
                        }

                        /* check if assignments not violating defined interfaces */
                        /* TODO: dedicate to method */
                        if (objExpression instanceof AssignmentExpression) {
                            AssignmentExpression objAssignment = (AssignmentExpression) objExpression;

                            PhpPsiElement objVariable = objAssignment.getVariable();
                            PhpPsiElement objValue = objAssignment.getValue();
                            if (null == objVariable || null == objValue) {
                                continue;
                            }

                            if (
                                objVariable instanceof Variable &&
                                null != objVariable.getName() && objVariable.getName().equals(strParameterName)
                            ) {
                                final HashSet<String> objTypesResolved = new HashSet<>();
                                TypeFromPlatformResolverUtil.resolveExpressionType(objValue, objTypesResolved);

                                boolean isCallViolatesDefinition;
                                for (String strType : objTypesResolved) {
                                    if (
                                        /* custom resolving artifacts */
                                        strType.equals(Types.strResolvingAbortedOnPsiLevel) ||
                                        strType.equals(Types.strClassNotResolved) ||
                                        /* we should not report mixed, bad annotation => bad analysis */
                                        strType.equals(Types.strMixed) ||
                                        /* sometimes types containing both keyword and resolved class */
                                        strType.equals(Types.strStatic) ||
                                        strType.equals(Types.strSelf)
                                    ) {
                                        continue;
                                    }

                                    isCallViolatesDefinition = (!this.isTypeCompatibleWith(strType, objParameterTypesResolved, objIndex));
                                    if (isCallViolatesDefinition) {
                                        final String message = strProblemAssignmentViolatesDefinition.replace("%s%", strType);
                                        holder.registerProblem(objValue, message, ProblemHighlightType.WEAK_WARNING);

                                        break;
                                    }
                                }
                                objTypesResolved.clear();
                            }
                        }

                        /* TODO: can be analysed comparison operations */
                    }

                    objParameterTypesResolved.clear();
                }
            }

            private boolean isTypeCompatibleWith(String strType, HashSet<String> listAllowedTypes, PhpIndex objIndex) {
                /* identical definitions */
                for (String strPossibleType: listAllowedTypes) {
                    if (strPossibleType.equals(strType)) {
                        return true;
                    }
                }

                /* classes/interfaces */
                if (strType.length() > 0 && strType.charAt(0) == '\\') {
                    /* collect classes/interfaces for type we going to analyse for compatibility */
                    Collection<PhpClass> classesToTest = PhpIndexUtil.getObjectInterfaces(strType, objIndex, false);
                    if (classesToTest.size() == 0) {
                        return false;
                    }

                    /* collect parent classes/interfaces for bulk check */
                    LinkedList<PhpClass> classesAllowed = new LinkedList<>();
                    for (String strAllowedType: listAllowedTypes) {
                        if (
                            strAllowedType.length() == 0 || strAllowedType.charAt(0) != '\\' ||
                            strAllowedType.equals(Types.strClassNotResolved)
                        ) {
                            continue;
                        }

                        classesAllowed.addAll(PhpIndexUtil.getObjectInterfaces(strAllowedType, objIndex, false));
                    }

                    /* run test through 2 sets */
                    for (PhpClass testSubject: classesToTest) {
                        /* collect hierarchy chain for interface inheritance checks */
                        LinkedList<PhpClass> testSubjectInheritanceChain = new LinkedList<>();
                        testSubjectInheritanceChain.add(testSubject);
                        Collections.addAll(testSubjectInheritanceChain, testSubject.getSupers());

                        for (PhpClass testAgainst: classesAllowed) {
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
                                for (PhpClass oneClassForInterfaceCheck : testSubjectInheritanceChain) {
                                    for (PhpClass objInterface : oneClassForInterfaceCheck.getImplementedInterfaces()) {
                                        if (objInterface.getFQN().equals(testAgainst.getFQN())) {
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
