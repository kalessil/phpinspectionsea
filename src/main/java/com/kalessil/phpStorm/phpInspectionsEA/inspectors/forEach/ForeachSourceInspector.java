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
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils.ExpressionCostEstimateUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
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

public class ForeachSourceInspector extends BasePhpInspection {
    private static final String patternNotRecognized = "Expressions' type was not recognized, please check type hints.";
    private static final String patternMixedTypes    = "Expressions' type contains '%t%', please specify possible types instead (best practices).";
    private static final String patternScalar        = "Can not iterate '%t%' (re-check type hints).";
    private static final String patternObject        = "Can not iterate '%t%' (must implement one of Iterator interfaces).";

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
                    this.analyseContainer(source);
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
                final HashSet<String> types = new HashSet<>();
                TypeFromPlatformResolverUtil.resolveExpressionType(container, types);
                if (types.isEmpty()) {
                    /* false-positives: pre-defined variables */
                    if (container instanceof Variable) {
                        final String variableName = ((Variable) container).getName();
                        if (ExpressionCostEstimateUtil.predefinedVars.contains(variableName)) {
                            return;
                        }
                    }

                    holder.registerProblem(container, patternNotRecognized, ProblemHighlightType.WEAK_WARNING);
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
                    final String parameter               = ((Variable) container).getName();
                    final PhpEntryPointInstruction start = ((Function) scope).getControlFlow().getEntryPoint();
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
                            if (null != matchCandidate && OpeanapiEquivalenceUtil.areEqual(matchCandidate, container)) {
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
                        final String filePath     = null == function ? null : function.getContainingFile().getVirtualFile().getCanonicalPath();
                        isStubFunction            = null != filePath && filePath.contains(".jar!") && filePath.contains("/stubs/");
                    }

                    if (!isStubFunction) {
                        final String message = patternMixedTypes.replace("%t%", Types.strMixed);
                        holder.registerProblem(container, message, ProblemHighlightType.WEAK_WARNING);
                    }

                    types.remove(Types.strMixed);
                }
                if (types.contains(Types.strObject)) {
                    final String message = patternMixedTypes.replace("%t%", Types.strObject);
                    holder.registerProblem(container, message, ProblemHighlightType.WEAK_WARNING);

                    types.remove(Types.strObject);
                }

                /* respect patter when returned array and bool|null for indicating failures*/
                if (2 == types.size() && types.contains(Types.strArray)) {
                    types.remove(Types.strBoolean);
                    types.remove(Types.strNull);
                }

                /* do not process foreach-compatible types */
                types.remove(Types.strArray);
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
                            final String message = patternScalar.replace("%t%", type);
                            holder.registerProblem(container, message, ProblemHighlightType.GENERIC_ERROR);

                            continue;
                        }

                        /* check classes: collect hierarchy of possible classes */
                        final Set<PhpClass> poolToCheck     = new HashSet<>();
                        final Collection<PhpClass> classes  = PhpIndexUtil.getObjectInterfaces(type, index, true);
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

                        /* analyze classes for having \Traversable in parents */
                        boolean hasTraversable = false;
                        if (!poolToCheck.isEmpty()) {
                            for (final PhpClass clazz : poolToCheck) {
                                if (clazz.getFQN().equals("\\Traversable")) {
                                    hasTraversable = true;
                                    break;
                                }
                            }
                            poolToCheck.clear();
                        }
                        if (!hasTraversable) {
                            final String message = patternObject.replace("%t%", type);
                            holder.registerProblem(container, message, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }

                    types.clear();
                }
            }
        };
    }
}
