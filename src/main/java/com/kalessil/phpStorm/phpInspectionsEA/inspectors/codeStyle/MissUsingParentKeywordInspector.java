package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
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

public class MissUsingParentKeywordInspector extends BasePhpInspection {
    private static final String message = "It was probably intended to use '$this' here.";

    @NotNull
    public String getShortName() {
        return "MissUsingParentKeywordInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                final PsiElement base = reference.getClassReference();
                if (base instanceof ClassReference && base.getText().equals("parent")) {
                    final Function scope = ExpressionSemanticUtil.getScope(reference);
                    if (scope instanceof Method) {
                        final String methodName = scope.getName();
                        final PhpClass clazz    = ((Method) scope).getContainingClass();
                        if (clazz != null && clazz.findOwnMethodByName(methodName) == null) {
                            final PhpIndex index           = PhpIndex.getInstance(reference.getProject());
                            final boolean blockedUsingThis = OpenapiResolveUtil.resolveChildClasses(clazz.getFQN(), index).stream()
                                    .anyMatch(c -> c.findOwnMethodByName(methodName) != null);
                            if (!blockedUsingThis) {
                                holder.registerProblem(base, message);
                            }
                        }
                    }
                }
            }
        };
    }
}
