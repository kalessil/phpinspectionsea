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
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

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
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.isContainingFileSkipped(method)) { return; }

                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP560) <= 0) {
                    this.checkParameters(method.getParameters(), method);
                }
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (this.isContainingFileSkipped(function)) { return; }

                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP560) <= 0) {
                    this.checkParameters(function.getParameters(), function);
                }
            }

            private void checkParameters(@NotNull Parameter[] parameters, @NotNull Function function) {
                final PhpEntryPointInstruction start    = function.getControlFlow().getEntryPoint();
                final Set<PsiElement> reportingRegistry = getFunctionReportingRegistry(function);
                for (final Parameter parameter : parameters) {
                    final String parameterName = parameter.getName();
                    if (!parameterName.isEmpty() && parameter.isPassByRef()) {
                        inspectScopeForReferenceMissUsages(start, parameterName, reportingRegistry);
                    }
                }
                reportingRegistry.clear();
            }

            /* = & variable/property patterns */
            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression assignmentExpression) {
                if (this.isContainingFileSkipped(assignmentExpression)) { return; }

                /* PHP7 seems to be ref mismatch free */
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) { // PHP7 and newer
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
                if (this.isContainingFileSkipped(reference)) { return; }

                /* PHP7 seems to be ref mismatch free */
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) { // PHP7 and newer
                    return;
                }

                /* older versions are still affected */
                this.checkReferenceReturnedByCallable(reference);
            }

            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.isContainingFileSkipped(reference)) { return; }

                /* PHP7 seems to be ref mismatch free */
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (!php.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) {
                    this.checkReferenceReturnedByCallable(reference);
                }
            }

            /* aggressive foreach optimization when value is reference */
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement foreach) {
                if (this.isContainingFileSkipped(foreach)) { return; }

                /* PHP7 seems to be ref mismatch free */
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) { // PHP7 and newer
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
                    if (!StringUtils.isEmpty(strVariable) && OpenapiTypesUtil.is(prevElement, PhpTokenTypes.opBIT_AND)) {
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
                        final PsiElement callable = OpenapiResolveUtil.resolveReference(reference);
                        if (!(callable instanceof Function)) {
                            continue;
                        }
                        final String strCallableName = ((Function) callable).getName();
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
                        final Parameter[] usageCallableParameters = ((Function) callable).getParameters();
                        if (usageCallableParameters.length >= indexInArguments + 1) {
                            final Parameter parameter = usageCallableParameters[indexInArguments];
                            if (!parameter.isPassByRef()) {
                                /* additionally try filtering types for reducing false-positives on scalars */
                                final PhpType type = OpenapiResolveUtil.resolveType(parameter, holder.getProject());
                                if (type != null && !PhpType.isSubType(type, legalizedTypesForMismatchingSet)) {
                                    final PsiElement target = reference.getParameters()[indexInArguments];
                                    if (!reportedItemsRegistry.contains(target)) {
                                        holder.registerProblem(target, "Reference mismatch, copy will be dispatched into function", ProblemHighlightType.WEAK_WARNING);
                                        reportedItemsRegistry.add(target);
                                    }
                                    continue;
                                }
                            }
                        }
                    }

                    /* test is assigned to a variable without stating its reference (copy stored) */
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

            private void checkReferenceReturnedByCallable(@NotNull FunctionReference reference) {
                /* check context before resolving anything  */
                final PsiElement parent = reference.getParent();
                if (parent instanceof AssignmentExpression) {
                    /* assignment structure verify */
                    final AssignmentExpression assignment = (AssignmentExpression) parent;
                    if (assignment.getValue() == reference) {
                        /* try resolving now */
                        final PsiElement callable = OpenapiResolveUtil.resolveReference(reference);
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
                                if (OpenapiTypesUtil.is(prevElement, PhpTokenTypes.opBIT_AND)) {
                                    /* check if assignments states reference usage */
                                    PsiElement operation = reference.getPrevSibling();
                                    if (operation instanceof PsiWhiteSpace) {
                                        operation = operation.getPrevSibling();
                                    }

                                    /* report if not */
                                    if (null != operation && !operation.getText().replaceAll("\\s+","").equals("=&")) {
                                        holder.registerProblem(parent, "Reference mismatch, copy will be stored (for non-objects)", ProblemHighlightType.WEAK_WARNING);
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
