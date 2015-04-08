package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.refactoring.PhpRefactoringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class ReferenceMismatchInspector extends BasePhpInspection {

    private static final PhpType legalizedTypesForMismatchingSet = new PhpType();
    private static final HashSet<String> legalizedMismatchingFunctions = new HashSet<String>();
    static {
        legalizedTypesForMismatchingSet.add(PhpType.STRING);
        legalizedTypesForMismatchingSet.add(PhpType.FLOAT);
        legalizedTypesForMismatchingSet.add(PhpType.INT);
        legalizedTypesForMismatchingSet.add(PhpType.BOOLEAN);
        legalizedTypesForMismatchingSet.add(PhpType.NULL);
        legalizedTypesForMismatchingSet.add(PhpType._OBJECT);
        legalizedTypesForMismatchingSet.add(new PhpType().add("\\Traversable"));

        /* ref-unsafe nature */
        legalizedMismatchingFunctions.add("is_array");
        legalizedMismatchingFunctions.add("count");
        legalizedMismatchingFunctions.add("is_object");
        /* documentation issue */
        legalizedMismatchingFunctions.add("property_exists");
        legalizedMismatchingFunctions.add("method_exists");
    }

    @NotNull
    public String getShortName() {
        return "ReferenceMismatchInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /**
             * TODO: checkReferenceReturnedByCallable - ternary operator, argument usages ?
             */

            /* parameters by reference */
            public void visitPhpMethod(Method method) {
                this.checkParameters(method.getParameters(), method);
            }
            public void visitPhpFunction(Function function) {
                this.checkParameters(function.getParameters(), function);
            }
            private void checkParameters(Parameter[] arrParameters, PhpScopeHolder objScopeHolder) {
                PhpEntryPointInstruction objEntryPoint = objScopeHolder.getControlFlow().getEntryPoint();

                for (Parameter parameter : arrParameters) {
                    /* skip un-discoverable and non-reference parameters */
                    String strParameterName = parameter.getName();
                    if (!parameter.isPassByRef() || StringUtil.isEmpty(strParameterName)) {
                        continue;
                    }

                    inspectScopeForReferenceMissUsages(objEntryPoint, strParameterName);
                }
            }

            /* = & variable/property patterns */
            public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {
                PsiElement value    = assignmentExpression.getValue();
                PsiElement variable = assignmentExpression.getVariable();
                if (
                    variable instanceof Variable && (
                        value instanceof Variable ||
                        value instanceof FieldReference ||
                        value instanceof FunctionReference
                )) {
                    String strVariable   = ((Variable) variable).getName();
                    PsiElement operation = value.getPrevSibling();
                    if (operation instanceof PsiWhiteSpace) {
                        operation = operation.getPrevSibling();
                    }
                    if (!StringUtil.isEmpty(strVariable) && null != operation && operation.getText().replaceAll("\\s+","").equals("=&")) {
                        /* the case, scan for miss-usages assuming variable is unique */
                        Function scope = ExpressionSemanticUtil.getScope(assignmentExpression);
                        if (null != scope) {
                            inspectScopeForReferenceMissUsages(scope.getControlFlow().getEntryPoint(), strVariable);
                        }
                    }
                }
            }

            /* assign reference from function */
            public void visitPhpMethodReference(MethodReference reference) {
                this.checkReferenceReturnedByCallable(reference);
            }
            public void visitPhpFunctionCall(FunctionReference reference) {
                this.checkReferenceReturnedByCallable(reference);
            }


            /* aggressive foreach optimization when value is reference */
            public void visitPhpForeach(ForeachStatement foreach) {
                /* lookup for reference preceding value */
                Variable objForeachValue = foreach.getValue();
                if (null != objForeachValue) {
                    String strVariable     = objForeachValue.getName();
                    PsiElement prevElement = objForeachValue.getPrevSibling();
                    if (prevElement instanceof PsiWhiteSpace) {
                        prevElement = prevElement.getPrevSibling();
                    }
                    if (!StringUtil.isEmpty(strVariable) && null != prevElement && PhpTokenTypes.opBIT_AND == prevElement.getNode().getElementType()) {
                        /* the case, scan for miss-usages assuming value is unique */
                        Function scope = ExpressionSemanticUtil.getScope(foreach);
                        if (null != scope) {
                            inspectScopeForReferenceMissUsages(scope.getControlFlow().getEntryPoint(), strVariable);
                        }
                    }
                }
            }


            private void inspectScopeForReferenceMissUsages(PhpEntryPointInstruction objEntryPoint, String strParameterName) {
                /* find usage inside scope */
                PhpAccessVariableInstruction[] arrUsages = PhpControlFlowUtil.getFollowingVariableAccessInstructions(objEntryPoint, strParameterName, false);
                for (PhpAccessVariableInstruction objInstruction : arrUsages) {
                    PsiElement objExpression = objInstruction.getAnchor().getParent();

                    /* test if provided as non-reference argument (copy dispatched) */
                    if (objExpression instanceof ParameterList && objExpression.getParent() instanceof FunctionReference) {
                        FunctionReference reference = (FunctionReference) objExpression.getParent();
                        /* not resolved or known re-unsafe function */
                        PsiElement callable = reference.resolve();
                        if (!(callable instanceof Function)) {
                            continue;
                        }
                        String strCallableName = ((Function) callable).getName();
                        if (!StringUtil.isEmpty(strCallableName) && legalizedMismatchingFunctions.contains(strCallableName)) {
                            continue;
                        }


                        /* check if call arguments contains our parameter */
                        int indexInArguments       = -1;
                        boolean providedAsArgument = false;
                        for (PsiElement callArgument : reference.getParameters()) {
                            ++indexInArguments;
                            if (callArgument instanceof Variable) {
                                Variable argument   = (Variable) callArgument;
                                String argumentName = argument.getName();
                                if (!StringUtil.isEmpty(argumentName) && argumentName.equals(strParameterName)) {
                                    providedAsArgument = true;
                                    break;
                                }
                            }
                        }
                        /* if not found, keep processing usages */
                        if (!providedAsArgument) {
                            continue;
                        }

                        /* now check what is declared in resolved callable */
                        Parameter[] usageCallableParameters = ((Function) callable).getParameters();
                        if (usageCallableParameters.length >= indexInArguments + 1) {
                            Parameter parameterForAnalysis = usageCallableParameters[indexInArguments];
                            if (!parameterForAnalysis.isPassByRef()) {
                                /* additionally try filtering types for reducing false-positives on scalars */
                                PhpType argumentType = PhpRefactoringUtil.getCompletedType(parameterForAnalysis, holder.getProject());
                                if (!PhpType.isSubType(argumentType, legalizedTypesForMismatchingSet)) {
                                    holder.registerProblem(reference.getParameters()[indexInArguments], "Reference mismatch, copy will be dispatched into function", ProblemHighlightType.WEAK_WARNING);
                                    continue;
                                }
                            }
                        }
                    }

                    /* test is assigned to a variable without stating it's reference (copy stored) */
                    if (objExpression instanceof AssignmentExpression) {
                        /* assignment structure verify */
                        AssignmentExpression assignment = (AssignmentExpression) objExpression;
                        if (assignment.getValue() instanceof Variable) {
                            Variable variable = (Variable) assignment.getValue();
                            String strVariable = variable.getName();
                            /* references parameter */
                            if (!StringUtil.isEmpty(strVariable) && strVariable.equals(strParameterName)) {
                                /* check if assignments states reference usage */
                                PsiElement operation = variable.getPrevSibling();
                                if (operation instanceof PsiWhiteSpace) {
                                    operation = operation.getPrevSibling();
                                }

                                /* report if not */
                                if (null != operation && !operation.getText().replaceAll("\\s+","").equals("=&")) {
                                    holder.registerProblem(objExpression, "Reference mismatch, copy will be stored (for non-objects)", ProblemHighlightType.WEAK_WARNING);
                                }
                            }
                        }
                    }
                }
            }

            private void checkReferenceReturnedByCallable(FunctionReference reference) {
                /* check context before resolving anything  */
                if (reference.getParent() instanceof AssignmentExpression) {
                    /* assignment structure verify */
                    AssignmentExpression assignment = (AssignmentExpression) reference.getParent();
                    if (assignment.getValue() == reference) {
                        /* try resolving now */
                        PsiElement callable = reference.resolve();
                        if (callable instanceof Function) {
                            /* ensure name discoverable */
                            Function function         = (Function) callable;
                            PsiElement nameIdentifier = function.getNameIdentifier();
                            if (null != nameIdentifier) {
                                /* is defined like returning reference */
                                PsiElement prevElement = nameIdentifier.getPrevSibling();
                                if (prevElement instanceof PsiWhiteSpace) {
                                    prevElement = prevElement.getPrevSibling();
                                }
                                if (null != prevElement && PhpTokenTypes.opBIT_AND == prevElement.getNode().getElementType()) {
                                    /* check if assignments states reference usage */
                                    PsiElement operation = reference.getPrevSibling();
                                    if (operation instanceof PsiWhiteSpace) {
                                        operation = operation.getPrevSibling();
                                    }

                                    /* report if not */
                                    if (null != operation && !operation.getText().replaceAll("\\s+","").equals("=&")) {
                                        holder.registerProblem(reference.getParent(), "Reference mismatch, copy will be stored (for non-objects)", ProblemHighlightType.WEAK_WARNING);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
