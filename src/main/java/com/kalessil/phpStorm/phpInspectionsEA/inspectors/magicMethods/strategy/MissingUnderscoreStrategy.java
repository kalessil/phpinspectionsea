package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Method;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class MissingUnderscoreStrategy {
    private static final String messagePattern = "'%s' is not a magic method. Did you mean '_%s'?";

    private static final List<String> invalidNames = Arrays.asList(
            "_construct",
            "_destruct",
            "_call",
            "_callStatic",
            "_get",
            "_set",
            "_isset",
            "_unset",
            "_sleep",
            "_wakeup",
            "_toString",
            "_invoke",
            "_set_state",
            "_clone",
            "_debugInfo"
    );

    static public void apply(@NotNull Method method, @NotNull ProblemsHolder holder) {
        final String methodName = method.getName();
        if (invalidNames.contains(methodName)) {
            final PsiElement target = NamedElementUtil.getNameIdentifier(method);
            if (target != null) {
                final String message = String.format(messagePattern, methodName, methodName);
                holder.registerProblem(target, message, ProblemHighlightType.WEAK_WARNING, new NameFix());
            }
        }
    }

    private static final class NameFix implements LocalQuickFix {
        private static final String title = "Correct mistyped method name";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null && !project.isDisposed()) {
                final PsiElement implant = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, '_' + target.getText());
                if (implant != null) {
                    target.replace(implant);
                }
            }
        }
    }
}
