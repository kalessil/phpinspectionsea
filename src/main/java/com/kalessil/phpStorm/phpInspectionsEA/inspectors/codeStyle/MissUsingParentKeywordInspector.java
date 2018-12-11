package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
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
    private static final String messagePattern = "It was probably intended to use '%s' here.";

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
                        final Method method = (Method) scope;
                        if (!method.isStatic()) {
                            final String methodName    = scope.getName();
                            final String referenceName = reference.getName();
                            if (!methodName.isEmpty() && !methodName.equals(referenceName)) {
                                final PhpClass clazz = method.getContainingClass();
                                if (clazz != null && clazz.findOwnMethodByName(referenceName) == null) {
                                    final boolean blockedUsingThis =
                                            !clazz.isFinal() &&
                                            OpenapiResolveUtil.resolveChildClasses(clazz.getFQN(), PhpIndex.getInstance(reference.getProject())).stream()
                                                    .anyMatch(c -> c.findOwnMethodByName(referenceName) != null);
                                    if (!blockedUsingThis) {
                                        final PsiElement parameters = reference.getParameterList();
                                        final String replacement    = String.format("$this->%s(%s)", referenceName, parameters == null ? "" : parameters.getText());
                                        holder.registerProblem(
                                                reference,
                                                String.format(messagePattern, replacement),
                                                new UseThisVariableFix(replacement)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseThisVariableFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use '$this' instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseThisVariableFix(@NotNull String expression) {
            super(expression);
        }
    }
}
