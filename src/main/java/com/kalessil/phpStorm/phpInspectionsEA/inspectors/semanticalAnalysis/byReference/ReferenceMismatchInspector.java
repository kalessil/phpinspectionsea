package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.byReference;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ReferenceMismatchInspector extends BasePhpInspection {
    final static private String strErrorForeachIntoReference = "Probable bug: variable should be renamed to prevent writing into already existing reference.";

    private static final PhpType legalizedTypesForMismatchingSet       = new PhpType();
    private static final HashSet<String> legalizedMismatchingFunctions = new HashSet<>();
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
        legalizedMismatchingFunctions.add("call_user_func");
        legalizedMismatchingFunctions.add("call_user_func_array");
        /* documentation issue */
        legalizedMismatchingFunctions.add("property_exists");
        legalizedMismatchingFunctions.add("method_exists");
    }

    private final static ConcurrentHashMap<Function, HashSet<PsiElement>> reportedIssues = new ConcurrentHashMap<>();
    private static HashSet<PsiElement> getFunctionReportingRegistry(Function key) {
        reportedIssues.putIfAbsent(key, new HashSet<>());
        return reportedIssues.get(key);
    }

    @NotNull
    public String getShortName() {
        return "ReferenceMismatchInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /**
             * TODO: checkReferenceReturnedByCallable - ternary operator, argument usages ?
             */

            /* parameters by reference */
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                /* PHP7 seems to be ref mismatch free */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (phpVersion.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) { // PHP7 and newer
                    return;
                }

                /* older versions are still affected */
                this.checkParameters(method.getParameters(), method);
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                /* PHP7 seems to be ref mismatch free */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (phpVersion.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) { // PHP7 and newer
                    return;
                }

                /* older versions are still affected */
                this.checkParameters(function.getParameters(), function);
            }
            private void checkParameters(Parameter[] arrParameters, Function objScopeHolder) {
                PhpEntryPointInstruction objEntryPoint = objScopeHolder.getControlFlow().getEntryPoint();

                HashSet<PsiElement> emptyReportedItemsRegistry =
                        ReferenceMismatchInspector.getFunctionReportingRegistry(objScopeHolder);

                for (Parameter parameter : arrParameters) {
                    /* skip un-discoverable and non-reference parameters */
                    String strParameterName = parameter.getName();
                    if (!parameter.isPassByRef() || StringUtils.isEmpty(strParameterName)) {
                        continue;
                    }

                    inspectScopeForReferenceMissUsages(objEntryPoint, strParameterName, emptyReportedItemsRegistry);
                }

                emptyReportedItemsRegistry.clear();
            }

            /* = & variable/property patterns */
            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression assignmentExpression) {
                /* PHP7 seems to be ref mismatch free */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (phpVersion.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) { // PHP7 and newer
                    return;
                }

                /* older versions are still affected */
                PsiElement value    = assignmentExpression.getValue();
                PsiElement variable = assignmentExpression.getVariable();
                if (
                    variable instanceof Variable && (
                        value instanceof Variable ||
                        value instanceof FieldReference ||
                        value instanceof ArrayAccessExpression ||
                        value instanceof FunctionReference
                )) {
                    String strVariable   = ((Variable) variable).getName();
                    PsiElement operation = value.getPrevSibling();
                    if (operation instanceof PsiWhiteSpace) {
                        operation = operation.getPrevSibling();
                    }
                    if (!StringUtils.isEmpty(strVariable) && null != operation && operation.getText().replaceAll("\\s+","").equals("=&")) {
                        /* the case, scan for miss-usages assuming variable is unique */
                        Function scope = ExpressionSemanticUtil.getScope(assignmentExpression);
                        if (null != scope) {
                            // report items, but ensure no duplicated messages
                            HashSet<PsiElement> reportedItemsRegistry = ReferenceMismatchInspector.getFunctionReportingRegistry(scope);
                            inspectScopeForReferenceMissUsages(scope.getControlFlow().getEntryPoint(), strVariable, reportedItemsRegistry);
                        }
                    }
                }
            }

            /* assign reference from function */
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                /* PHP7 seems to be ref mismatch free */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (phpVersion.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) { // PHP7 and newer
                    return;
                }

                /* older versions are still affected */
                this.checkReferenceReturnedByCallable(reference);
            }

            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                /* PHP7 seems to be ref mismatch free */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (phpVersion.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) { // PHP7 and newer
                    return;
                }

                /* older versions are still affected */
                this.checkReferenceReturnedByCallable(reference);
            }

            /* aggressive foreach optimization when value is reference */
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement foreach) {
                /* PHP7 seems to be ref mismatch free */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (phpVersion.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) { // PHP7 and newer
                    return;
                }

                /* older versions are still affected */
                /* lookup for reference preceding value */
                Variable objForeachValue = foreach.getValue();
                if (null != objForeachValue) {
                    String strVariable     = objForeachValue.getName();
                    PsiElement prevElement = objForeachValue.getPrevSibling();
                    if (prevElement instanceof PsiWhiteSpace) {
                        prevElement = prevElement.getPrevSibling();
                    }
                    if (!StringUtils.isEmpty(strVariable) && null != prevElement && PhpTokenTypes.opBIT_AND == prevElement.getNode().getElementType()) {
                        /* the case, scan for miss-usages assuming value is unique */
                        Function scope = ExpressionSemanticUtil.getScope(foreach);
                        if (null != scope) {
                            // report items, but ensure no duplicated messages
                            HashSet<PsiElement> reportedItemsRegistry = ReferenceMismatchInspector.getFunctionReportingRegistry(scope);
                            reportedItemsRegistry.add(objForeachValue);
                            inspectScopeForReferenceMissUsages(scope.getControlFlow().getEntryPoint(), strVariable, reportedItemsRegistry);
                        }
                    }
                }
            }


            private void inspectScopeForReferenceMissUsages(
                @NotNull PhpEntryPointInstruction objEntryPoint,
                @NotNull String strParameterName,
                @NotNull Set<PsiElement> reportedItemsRegistry
            ) {
                PsiElement previous;
                PsiElement objExpression = null;

                /* find usage inside scope */
                PhpAccessVariableInstruction[] arrUsages = PhpControlFlowUtil.getFollowingVariableAccessInstructions(objEntryPoint, strParameterName, false);
                for (final PhpAccessVariableInstruction objInstruction : arrUsages) {
                    previous      = objExpression;
                    objExpression = objInstruction.getAnchor().getParent();

                    /* collided with foreach index/value => bug */
                    if (objExpression instanceof ForeachStatement) {
                        final ForeachStatement foreach = (ForeachStatement) objExpression;
                        if (previous instanceof PhpUnset) {
                            break;
                        }

                        final Variable foreachValue = foreach.getValue();
                        if (null != foreachValue && !StringUtils.isEmpty(foreachValue.getName()) && foreachValue.getName().equals(strParameterName)) {
                            if (!reportedItemsRegistry.contains(foreachValue)) {
                                reportedItemsRegistry.add(foreachValue);
                                holder.registerProblem(foreachValue, strErrorForeachIntoReference, ProblemHighlightType.ERROR);
                            }
                            continue;
                        }

                        final Variable foreachKey = foreach.getKey();
                        if (null != foreachKey && !StringUtils.isEmpty(foreachKey.getName()) && foreachKey.getName().equals(strParameterName)) {
                            if (!reportedItemsRegistry.contains(foreachKey)) {
                                reportedItemsRegistry.add(foreachKey);
                                holder.registerProblem(foreachKey, strErrorForeachIntoReference, ProblemHighlightType.ERROR);
                            }
                            continue;
                        }
                    }

                    /* test if provided as non-reference argument (copy dispatched) */
                    if (objExpression instanceof ParameterList && objExpression.getParent() instanceof FunctionReference) {
                        FunctionReference reference = (FunctionReference) objExpression.getParent();
                        /* not resolved or known re-unsafe function */
                        PsiElement callable = reference.resolve();
                        if (!(callable instanceof Function)) {
                            continue;
                        }
                        String strCallableName = ((Function) callable).getName();
                        if (!StringUtils.isEmpty(strCallableName) && legalizedMismatchingFunctions.contains(strCallableName)) {
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
                                if (!StringUtils.isEmpty(argumentName) && argumentName.equals(strParameterName)) {
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
                                PhpType argumentType = parameterForAnalysis.getType().global(holder.getProject());
                                if (!PhpType.isSubType(argumentType, legalizedTypesForMismatchingSet)) {
                                    PsiElement itemToBeReported = reference.getParameters()[indexInArguments];
                                    if (!reportedItemsRegistry.contains(itemToBeReported)) {
                                        holder.registerProblem(itemToBeReported, "Reference mismatch, copy will be dispatched into function", ProblemHighlightType.WEAK_WARNING);
                                        reportedItemsRegistry.add(itemToBeReported);
                                    }

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
                            if (!StringUtils.isEmpty(strVariable) && strVariable.equals(strParameterName)) {
                                /* check if assignments states reference usage */
                                PsiElement operation = variable.getPrevSibling();
                                if (operation instanceof PsiWhiteSpace) {
                                    operation = operation.getPrevSibling();
                                }

                                /* report if not */
                                if (null != operation && !operation.getText().replaceAll("\\s+","").equals("=&")) {
                                    if (!reportedItemsRegistry.contains(objExpression)) {
                                        holder.registerProblem(objExpression, "Reference mismatch, copy will be stored (for non-objects)", ProblemHighlightType.WEAK_WARNING);
                                        reportedItemsRegistry.add(objExpression);
                                    }
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
                            final Function function   = (Function) callable;
                            final PsiElement nameNode = NamedElementUtil.getNameIdentifier(function);
                            if (null != nameNode) {
                                /* is defined like returning reference */
                                PsiElement prevElement = nameNode.getPrevSibling();
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
