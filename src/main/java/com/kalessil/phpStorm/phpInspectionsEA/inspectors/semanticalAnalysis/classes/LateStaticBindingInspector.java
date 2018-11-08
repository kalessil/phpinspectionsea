package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
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

public class LateStaticBindingInspector extends BasePhpInspection {
    private static final String messagePrivateMethod = "It's better to use 'self' here (identically named private method in child classes will cause an error).";

    @NotNull
    public String getShortName() {
        return "LateStaticBindingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(reference))              { return; }

                final String methodName = reference.getName();
                if (methodName != null && !methodName.isEmpty()) {
                    final PsiElement base = reference.getClassReference();
                    if (base instanceof ClassReference && base.getText().equals("static")) {
                        final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                        if (resolved instanceof Method && ((Method) resolved).getAccess().isPrivate()) {
                            final Function scope = ExpressionSemanticUtil.getScope(reference);
                            if (scope instanceof Method) {
                                final PhpClass clazz = ((Method) scope).getContainingClass();
                                if (clazz != null && !clazz.isFinal()) {
                                    holder.registerProblem(base, messagePrivateMethod);
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
