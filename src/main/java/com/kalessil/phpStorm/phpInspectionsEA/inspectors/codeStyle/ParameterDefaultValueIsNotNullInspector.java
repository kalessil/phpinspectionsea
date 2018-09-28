package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ParameterDefaultValueIsNotNullInspector extends BasePhpInspection {
    private static final String message = "Null should be used as the default value (nullable types are the goal, right?)";

    @NotNull
    public String getShortName() {
        return "ParameterDefaultValueIsNotNullInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.isContainingFileSkipped(method)) { return; }

                this.analyzeFunction(method);
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (this.isContainingFileSkipped(function)) { return; }

                this.analyzeFunction(function);
            }

            private void analyzeFunction(@NotNull Function function) {
                final Parameter[] params = function.getParameters();
                if (params.length > 0) {
                    /* collect violations */
                    final List<Parameter> violations = new ArrayList<>();
                    for (final Parameter param : params) {
                        final PsiElement defaultValue = param.getDefaultValue();
                        if (defaultValue != null && !PhpLanguageUtil.isNull(defaultValue)) {
                            violations.add(param);
                        }
                    }

                    if (!violations.isEmpty()) {
                        if (function instanceof Method) {
                            final PhpClass clazz      = ((Method) function).getContainingClass();
                            final PhpClass parent     = null == clazz ? null : OpenapiResolveUtil.resolveSuperClass(clazz);
                            final Method parentMethod = null == parent ? null : OpenapiResolveUtil.resolveMethod(parent, function.getName());
                            if (parentMethod != null && !parentMethod.getAccess().isPrivate()) {
                                violations.clear();
                                return;
                            }
                        }

                        /* report violations */
                        for (final Parameter param : violations) {
                            holder.registerProblem(param, message);
                        }
                        violations.clear();
                    }
                }
            }
        };
    }
}
