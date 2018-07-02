package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.ArrayIndex;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NonSecureArrayRandUsageInspector extends BasePhpInspection {
    private static final String message = "Insufficient entropy, random_int/random_bytes based solution would be more secure.";

    @NotNull
    public String getShortName() {
        return "NonSecureArrayRandUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("array_rand")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1) {
                        final PsiElement parent = reference.getParent();
                        if (parent instanceof ArrayIndex) {
                            final PsiElement container = ((ArrayAccessExpression) parent.getParent()).getValue();
                            if (container != null && OpenapiEquivalenceUtil.areEqual(container, arguments[0])) {
                                holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);
                            }
                        }
                    }
                }
                /* TODO: call_user_func, array_walk - dynamic contexts */
            }
        };
    }
}
