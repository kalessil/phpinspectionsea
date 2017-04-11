package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ForeachSourceInspector extends BasePhpInspection {
    final private String patternNotRecognized = "Expressions' type was not recognized, please check type hints.";
    final private String patternMixedTypes    = "Expressions' type contains '%t%', please specify possible types instead (best practices).";
    final private String patternScalar        = "Can not iterate '%t%' (re-check type hints).";
    final private String patternObject        = "Can not iterate '%t%' (must implement one of Iterator interfaces).";

    @NotNull
    public String getShortName() {
        return "ForeachSourceInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpForeach(ForeachStatement foreach) {
                final PsiElement container = ExpressionSemanticUtil.getExpressionTroughParenthesis(foreach.getArray());
                if (container instanceof PhpTypedElement) {
                    this.analyseContainer(container);
                }
            }

            private void analyseContainer(@NotNull PsiElement container) {
                final HashSet<String> types = new HashSet<>();
                TypeFromPlatformResolverUtil.resolveExpressionType(container, types);
                if (types.isEmpty()) {
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
                    for (PhpAccessVariableInstruction instruction : uses) {
                        final PhpPsiElement expression = instruction.getAnchor();
                        /* when matched itself, stop processing */
                        if (expression == container) {
                            break;
                        }

                        final PsiElement parent = expression.getParent();
                        if (parent instanceof AssignmentExpression) {
                            final PsiElement matchCandidate = ((AssignmentExpression) parent).getVariable();
                            if (null != matchCandidate && PsiEquivalenceUtil.areElementsEquivalent(matchCandidate, container)) {
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
                        final PsiElement function = ((FunctionReference) container).resolve();
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
                types.remove("\\Traversable");
                types.remove("\\Iterator");
                types.remove("\\IteratorAggregate");
                /* don't process mysterious empty set type */
                types.remove(Types.strEmptySet);

                /* iterate rest of types */
                if (!types.isEmpty()) {
                    final PhpIndex index = PhpIndex.getInstance(holder.getProject());
                    for (String type : types) {
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
                            for (PhpClass clazz : classes) {
                                final Set<PhpClass> interfaces = InterfacesExtractUtil.getCrawlCompleteInheritanceTree(clazz, false);
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
                            for (PhpClass clazz : poolToCheck) {
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
