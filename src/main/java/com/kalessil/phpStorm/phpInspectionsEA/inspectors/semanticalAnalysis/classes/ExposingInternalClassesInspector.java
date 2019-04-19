package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ExposingInternalClassesInspector extends BasePhpInspection {
    private static final String message = "Exposes an @internal class, which should not be exposed via public methods.";

    @NotNull
    public String getShortName() {
        return "ExposingInternalClassesInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(clazz))                  { return; }

                if (!clazz.isInterface() && !clazz.isTrait() && !clazz.isInternal()) {
                    methods: for (final Method method : clazz.getOwnMethods()) {
                        if (method.getAccess().isPublic()) {
                            /* case: parameter type declaration */
                            for (final Parameter parameter: method.getParameters()) {
                                final PhpType type = parameter.getDeclaredType();
                                if (!type.isEmpty() && this.isReferencingInternal(type)) {
                                    holder.registerProblem(parameter, message);
                                    continue methods;
                                }
                            }
                            /* case: return type declaration */
                            final PsiElement returnTypeHint = OpenapiElementsUtil.getReturnType(method);
                            if (returnTypeHint != null) {
                                final PhpType type = method.getType();
                                if (!type.isEmpty() && this.isReferencingInternal(type)) {
                                    holder.registerProblem(returnTypeHint, message);
                                }
                            }
                        }
                    }
                }
            }

            private boolean isReferencingInternal(@NotNull PhpType type) {
                return false;
            }
        };
    }
}
