package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.ForeachStatement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpIndexUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPlatformResolverUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public class ForeachSourceInspector extends BasePhpInspection {
    final private String patternNotRecognized = "Expressions' type was not recognized, please check type hints";
    final private String patternMixedTypes    = "Expressions' type contains '%t%', please specify possible types instead (best practices)";
    final private String patternScalar        = "Can not iterate '%t%' (re-check type hints)";
    final private String patternObject        = "Can not iterate '%t%' (must implement one of Iterator interfaces)";

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
                if (null != container) {
                    this.analyseContainer(container);
                }
            }

            private void analyseContainer(@NotNull PsiElement container) {
                HashSet<String> types = new HashSet<>();
                TypeFromPlatformResolverUtil.resolveExpressionType(container, types);
                if (0 == types.size()) {
                    holder.registerProblem(container, patternNotRecognized, ProblemHighlightType.WEAK_WARNING);
                    return;
                }

                /* gracefully request to specify exact types which can appear (mixed, object) */
                if (types.contains(Types.strMixed)) {
                    final String message = patternMixedTypes.replace("%t%", Types.strMixed);
                    holder.registerProblem(container, message, ProblemHighlightType.WEAK_WARNING);

                    types.remove(Types.strMixed);
                }
                if (types.contains(Types.strObject)) {
                    final String message = patternMixedTypes.replace("%t%", Types.strObject);
                    holder.registerProblem(container, message, ProblemHighlightType.WEAK_WARNING);

                    types.remove(Types.strObject);
                }

                /* do not process foreach-compatible types */
                types.remove(Types.strArray);
                types.remove("\\Traversable");
                types.remove("\\Iterator");
                types.remove("\\IteratorAggregate");

                /* iterate rest of types */
                if (types.size() > 0) {
                    final PhpIndex index = PhpIndex.getInstance(holder.getProject());

                    for (String type : types) {
                        /* analyze scalar types */
                        final boolean isClassType = type.startsWith("\\");
                        if (!isClassType) {
                            final String message = patternScalar.replace("%t%", type);
                            holder.registerProblem(container, message, ProblemHighlightType.ERROR);

                            continue;
                        }

                        /* check classes: collect hierarchy of possible classes */
                        final HashSet<PhpClass> poolToCheck = new HashSet<>();
                        final Collection<PhpClass> classes  = PhpIndexUtil.getObjectInterfaces(type, index, true);
                        if (classes.size() > 0) {
                            /* collect all interfaces*/
                            for (PhpClass clazz : classes) {
                                final HashSet<PhpClass> interfaces = InterfacesExtractUtil.getCrawlCompleteInheritanceTree(clazz, false);
                                if (interfaces.size() > 0) {
                                    poolToCheck.addAll(interfaces);
                                    interfaces.clear();
                                }
                            }

                            classes.clear();
                        }

                        /* analyze classes for having \Traversable in parents */
                        boolean hasTraversable = false;
                        if (poolToCheck.size() > 0) {
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
                            holder.registerProblem(container, message, ProblemHighlightType.ERROR);
                        }
                    }

                    types.clear();
                }
            }
        };
    }
}
