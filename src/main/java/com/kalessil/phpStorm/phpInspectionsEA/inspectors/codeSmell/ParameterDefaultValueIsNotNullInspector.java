package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
            public void visitPhpMethod(Method method) {
                visitPhpFunction(method);
            }

            public void visitPhpFunction(Function function) {
                final Parameter[] params = function.getParameters();
                if (0 == params.length) {
                    return;
                }

                /* collect violations */
                final ArrayList<Parameter> violations = new ArrayList<>();
                for (Parameter param : params) {
                    final PsiElement defaultValue = param.getDefaultValue();
                    if (null == defaultValue || PhpLanguageUtil.isNull(defaultValue)) {
                        continue;
                    }
                    violations.add(param);
                }

                if (!violations.isEmpty()) {
                    if (function instanceof Method) {
                        final PhpClass clazz      = ((Method) function).getContainingClass();
                        final PhpClass parent     = null == clazz ? null : clazz.getSuperClass();
                        final Method parentMethod = null == parent ? null : parent.findMethodByName(function.getName());
                        if (null != parentMethod && !parentMethod.getAccess().isPrivate()) {
                            violations.clear();
                            return;
                        }
                    }

                    /* report violations */
                    for (Parameter param : violations) {
                        holder.registerProblem(param, message, ProblemHighlightType.WEAK_WARNING);
                    }
                    violations.clear();
                }
            }
        };
    }
}
