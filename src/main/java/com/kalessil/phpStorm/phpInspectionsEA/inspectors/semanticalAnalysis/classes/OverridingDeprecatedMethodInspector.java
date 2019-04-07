package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class OverridingDeprecatedMethodInspector extends BasePhpInspection {
    private static final String patternNeedsDeprecation = "'%m%' overrides/implements a deprecated method. Consider refactoring or deprecate it as well.";
    private static final String patternDeprecateParent  = "Parent '%m%' probably needs to be deprecated as well.";

    @NotNull
    public String getShortName() {
        return "OverridingDeprecatedMethodInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.isContainingFileSkipped(method)) { return; }

                /* do not process un-reportable classes and interfaces - we are searching real tech. debt here */
                final PhpClass clazz      = method.getContainingClass();
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
                if (null == nameNode || null == clazz || method.isDeprecated()) {
                    return;
                }

                final String methodName = method.getName();

                /* search for deprecated parent methods */
                final PhpClass parent = OpenapiResolveUtil.resolveSuperClass(clazz);
                if (parent != null) {
                    final Method parentMethod = OpenapiResolveUtil.resolveMethod(parent, methodName);
                    if (parentMethod != null) {
                        if (!method.isDeprecated() && parentMethod.isDeprecated()) {
                            holder.registerProblem(
                                    nameNode,
                                    patternNeedsDeprecation.replace("%m%", methodName),
                                    ProblemHighlightType.LIKE_DEPRECATED
                            );
                            return;
                        }
                        if (method.isDeprecated() && !parentMethod.isDeprecated()) {
                            holder.registerProblem(
                                    nameNode,
                                    patternDeprecateParent.replace("%m%", methodName),
                                    ProblemHighlightType.WEAK_WARNING
                            );
                            return;
                        }
                    }
                }

                if (!method.isDeprecated()) {
                    /* search for deprecated interface methods */
                    for (final PhpClass contract : OpenapiResolveUtil.resolveImplementedInterfaces(clazz)) {
                        final Method contractMethod = OpenapiResolveUtil.resolveMethod(contract, methodName);
                        if (contractMethod != null && contractMethod.isDeprecated()) {
                            holder.registerProblem(
                                    nameNode,
                                    patternNeedsDeprecation.replace("%m%", methodName),
                                    ProblemHighlightType.LIKE_DEPRECATED
                            );
                            return;
                        }
                    }
                    /* search for deprecated trait methods */
                    for (final PhpClass trait : OpenapiResolveUtil.resolveImplementedTraits(clazz)) {
                        final Method traitMethod = OpenapiResolveUtil.resolveMethod(trait, methodName);
                        if (traitMethod != null && traitMethod.isDeprecated()) {
                            holder.registerProblem(
                                    nameNode,
                                    patternNeedsDeprecation.replace("%m%", methodName),
                                    ProblemHighlightType.LIKE_DEPRECATED
                            );
                            return;
                        }
                    }
                }
            }
        };
    }
}
