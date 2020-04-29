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
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

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
    @Override
    public String getShortName() {
        return "MissUsingParentKeywordInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'parent' keyword misused";
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
                        final Method context = (Method) scope;
                        final PhpClass clazz = context.getContainingClass();
                        if (clazz != null && !clazz.isTrait() && !context.isStatic()) {
                            final String methodName    = scope.getName();
                            final String referenceName = reference.getName();
                            if (referenceName != null && !referenceName.equals(methodName)) {
                                final boolean isTarget = clazz.findOwnMethodByName(referenceName) == null &&
                                                         !this.isOverridden(clazz, referenceName);
                                if (isTarget) {
                                    final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                                    if (resolved instanceof Method) {
                                        final PsiElement parameters = reference.getParameterList();
                                        final String replacement    = String.format(
                                                ((Method) resolved).isStatic() ? "self::%s(%s)" : "$this->%s(%s)",
                                                referenceName,
                                                parameters == null ? "" : parameters.getText());
                                        holder.registerProblem(
                                                reference,
                                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                                new NormalizeClassReferenceFix(replacement)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }

            final boolean isOverridden(@NotNull PhpClass clazz, @NotNull String methodName) {
                if (!clazz.isFinal()) {
                    final PhpIndex index                = PhpIndex.getInstance(holder.getProject());
                    final Collection<PhpClass> children = OpenapiResolveUtil.resolveChildClasses(clazz.getFQN(), index);
                    return children.stream().anyMatch(c -> c.findOwnMethodByName(methodName) != null);
                }
                return false;
            }
        };
    }

    private static final class NormalizeClassReferenceFix extends UseSuggestedReplacementFixer {
        private static final String title = "Apply suggested class reference";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        NormalizeClassReferenceFix(@NotNull String expression) {
            super(expression);
        }
    }
}
