package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

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
    private static final String patternNeedsDeprecation = "'%s' overrides/implements a deprecated method. Consider refactoring or deprecate it as well.";
    private static final String patternDeprecateParent  = "Parent '%s' probably needs to be deprecated as well.";

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
                final PhpClass clazz        = method.getContainingClass();
                final PsiElement methodName = NamedElementUtil.getNameIdentifier(method);
                if (methodName != null && clazz != null) {
                    /* search for deprecated parent methods */
                    final String searchMethodName = method.getName();
                    final PhpClass parent         = OpenapiResolveUtil.resolveSuperClass(clazz);
                    final Method parentMethod     = parent == null ? null : OpenapiResolveUtil.resolveMethod(parent, searchMethodName);
                    if (parentMethod != null) {
                        final boolean isDeprecated = method.isDeprecated();
                        if (!isDeprecated && parentMethod.isDeprecated()) {
                            holder.registerProblem(methodName, String.format(patternNeedsDeprecation, searchMethodName));
                        } else if (isDeprecated && !parentMethod.isDeprecated()) {
                            holder.registerProblem(methodName, String.format(patternDeprecateParent, searchMethodName));
                        }
                    }
                }
            }
        };
    }
}
