package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils.ExpressionCostEstimateUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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

public class ForeachSourceInspector extends BasePhpInspection {
    // Inspection options.
    public boolean REPORT_MIXED_TYPES        = true;
    public boolean REPORT_UNRECOGNIZED_TYPES = true;

    private static final String patternNotRecognized = "Expressions' type was not recognized, please check type hints.";
    private static final String patternMixedTypes    = "Expressions' type contains '%s', please specify possible types instead (best practices).";
    private static final String patternScalar        = "Can not iterate '%s' (re-check type hints).";
    private static final String patternObject        = "Iterates over '%s' properties (probably should implement one of Iterator interfaces).";

    @NotNull
    public String getShortName() {
        return "ForeachSourceInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement foreach) {
                final PsiElement source = ExpressionSemanticUtil.getExpressionTroughParenthesis(foreach.getArray());
                if (source instanceof PhpTypedElement && !isEnsuredByPyParentIf(foreach, source)) {
                    analyseContainer(source);
                }
            }

            /* should cover is_array/is_iterable in direct parent if of the loop, while PS types resolving gets improved */
            private boolean isEnsuredByPyParentIf(@NotNull ForeachStatement foreach, @NotNull PsiElement source) {
                boolean result = false;
                if (foreach.getPrevPsiSibling() == null) {
                    final PsiElement ifCandidate = foreach.getParent() instanceof GroupStatement ? foreach.getParent().getParent() : null;
                    final PsiElement conditions;
                    if (ifCandidate instanceof If) {
                        conditions = ((If) ifCandidate).getCondition();
                    } else if (ifCandidate instanceof ElseIf) {
                        conditions = ((ElseIf) ifCandidate).getCondition();
                    } else {
                        conditions = null;
                    }
                    if (conditions != null) {
                        for (final PsiElement candidate : PsiTreeUtil.findChildrenOfType(conditions, source.getClass())) {
                            if (OpeanapiEquivalenceUtil.areEqual(candidate, source)) {
                                final PsiElement call = candidate.getParent() instanceof ParameterList ? candidate.getParent().getParent() : null;
                                if (OpenapiTypesUtil.isFunctionReference(call)) {
                                    final String functionName = ((FunctionReference) call).getName();
                                    if (functionName != null && (functionName.equals("is_array") || functionName.equals("is_iterable"))) {
                                        result = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                return result;
            }

            private void analyseContainer(@NotNull PsiElement container) {
                final PhpType resolvedType = OpenapiResolveUtil.resolveType((PhpTypedElement) container, container.getProject());
                if (resolvedType == null) {
                    return;
                }
                final Set<String> types = resolvedType.filterUnknown().getTypes().stream()
                                                      .map(Types::getType).collect(Collectors.toSet());
                if (types.isEmpty()) {
                    /* false-positives: pre-defined variables */
                    if (container instanceof Variable) {
                        final String variableName = ((Variable) container).getName();
                        if (ExpressionCostEstimateUtil.predefinedVars.contains(variableName)) {
                            return;
                        }
                    }
                    if (REPORT_UNRECOGNIZED_TYPES) {
                        holder.registerProblem(container, patternNotRecognized, ProblemHighlightType.WEAK_WARNING);
                    }
                    return;
                }

                /* false-positives: multiple return types checked only in function/method; no global context */
                final PsiElement scope = ExpressionSemanticUtil.getBlockScope(container);
                if (types.size() > 1 && !(scope instanceof Function)) {
                    types.clear();
                    return;
                }
                /* false-positives: mixed parameter type, parameter overridden before foreach */
                if (types.size() > 1 && scope instanceof Function && container instanceof Variable) {
                    final String                   parameter = ((Variable) container).getName();
                    final PhpEntryPointInstruction start     = ((Function) scope).getControlFlow().getEntryPoint();
                    final PhpAccessVariableInstruction[] uses
                        = PhpControlFlowUtil.getFollowingVariableAccessInstructions(start, parameter, false);
                    for (final PhpAccessVariableInstruction instruction : uses) {
                        final PhpPsiElement expression = instruction.getAnchor();
                        /* when matched itself, stop processing */
                        if (expression == container) {
                            break;
                        }

                        final PsiElement parent = expression.getParent();
                        if (parent instanceof AssignmentExpression) {
                            final PsiElement matchCandidate = ((AssignmentExpression) parent).getVariable();
                            if (matchCandidate != null && OpeanapiEquivalenceUtil.areEqual(matchCandidate, container)) {
                                types.clear();
                                return;
                            }
                        }
                    }
                }
                /* false-positives: array type parameter declaration adds mixed */
                if (types.size() > 1 && scope instanceof Function && container instanceof ArrayAccessExpression) {
                    final PsiElement candidate = ((ArrayAccessExpression) container).getValue();
                    if (candidate instanceof Variable && types.contains(Types.strMixed) && types.contains(Types.strArray)) {
                        types.remove(Types.strMixed);
                    }
                }

                /* gracefully request to specify exact types which can appear (mixed, object) */
                if (types.contains(Types.strMixed)) {
                    /* false-positive: mixed definitions from stub functions */
                    boolean isStubFunction = false;
                    if (OpenapiTypesUtil.isFunctionReference(container)) {
                        final PsiElement function = OpenapiResolveUtil.resolveReference((FunctionReference) container);
                        final String     filePath = function == null ? null : function.getContainingFile().getVirtualFile().getCanonicalPath();
                        isStubFunction = filePath != null && filePath.contains(".jar!") && filePath.contains("/stubs/");
                    }
                    /* false-positive: mixed definition from array type */
                    if (!isStubFunction && !types.contains(Types.strArray) && REPORT_MIXED_TYPES) {
                        final String message = String.format(patternMixedTypes, Types.strMixed);
                        holder.registerProblem(container, message, ProblemHighlightType.WEAK_WARNING);
                    }
                    types.remove(Types.strMixed);
                }
                if (types.contains(Types.strObject)) {
                    if (REPORT_MIXED_TYPES) {
                        final String message = String.format(patternMixedTypes, Types.strObject);
                        holder.registerProblem(container, message, ProblemHighlightType.WEAK_WARNING);
                    }
                    types.remove(Types.strObject);
                }

                /* respect patter when returned array and bool|null for indicating failures*/
                if (types.size() == 2 && types.contains(Types.strArray)) {
                    types.remove(Types.strBoolean);
                    types.remove(Types.strNull);
                }

                /* if contains an array, then it is iterable */
                if (types.contains(Types.strArray)) {
                    return;
                }

                /* do not process foreach-compatible types */
                types.remove(Types.strIterable);
                types.remove("\\Traversable");
                types.remove("\\Iterator");
                types.remove("\\IteratorAggregate");
                /* don't process mysterious empty set type */
                types.remove(Types.strEmptySet);

                /* iterate rest of types */
                if (!types.isEmpty()) {
                    final PhpIndex index = PhpIndex.getInstance(holder.getProject());
                    for (final String type : types) {
                        /* analyze scalar types */
                        final boolean isClassType = type.startsWith("\\");
                        if (!isClassType) {
                            final String message = String.format(patternScalar, type);
                            holder.registerProblem(container, message, ProblemHighlightType.GENERIC_ERROR);

                            continue;
                        }

                        /* check classes: collect hierarchy of possible classes */
                        final Set<PhpClass>        poolToCheck = new HashSet<>();
                        final Collection<PhpClass> classes     = PhpIndexUtil.getObjectInterfaces(type, index, true);
                        final boolean              foundClass  = !classes.isEmpty();
                        if (!classes.isEmpty()) {
                            /* collect all interfaces*/
                            for (final PhpClass clazz : classes) {
                                final Set<PhpClass> interfaces = InterfacesExtractUtil.getCrawlInheritanceTree(clazz, false);
                                if (!interfaces.isEmpty()) {
                                    poolToCheck.addAll(interfaces);
                                    interfaces.clear();
                                }
                            }
                            classes.clear();
                        }

                        if (foundClass && poolToCheck.stream().noneMatch(c -> c.getFQN().equals("\\Traversable"))) {
                            holder.registerProblem(container, String.format(patternObject, type));
                        }
                        poolToCheck.clear();
                    }

                    types.clear();
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Request better specification for 'mixed'", REPORT_MIXED_TYPES, (isSelected) -> REPORT_MIXED_TYPES = isSelected);
            component.addCheckbox("Request missing types specification", REPORT_UNRECOGNIZED_TYPES, (isSelected) -> REPORT_UNRECOGNIZED_TYPES = isSelected);
        });
    }
}
