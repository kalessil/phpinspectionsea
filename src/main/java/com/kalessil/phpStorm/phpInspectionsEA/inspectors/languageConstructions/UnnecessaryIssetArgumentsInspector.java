package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.PhpIsset;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnnecessaryIssetArgumentsInspector extends BasePhpInspection {
    private static final String message = "This argument can be skipped (handled by its array access).";

    @NotNull
    @Override
    public String getShortName() {
        return "UnnecessaryIssetArgumentsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Unnecessary isset arguments specification";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpIsset(@NotNull PhpIsset issetExpression) {
                final PsiElement[] arguments = issetExpression.getVariables();
                if (arguments.length > 1) {
                    final Set<PsiElement> reported = new HashSet<>();
                    for (final PsiElement current : arguments) {
                        if (current instanceof ArrayAccessExpression && !reported.contains(current)) {
                            /* collect current element bases */
                            final List<PsiElement> bases = new ArrayList<>();
                            PsiElement base              = current;
                            while (base instanceof ArrayAccessExpression) {
                                base = ((ArrayAccessExpression) base).getValue();
                                if (base != null) {
                                    bases.add(base);
                                }
                            }
                            /* iterate arguments once more to match */
                            if (!bases.isEmpty()) {
                                for (final PsiElement discoveredBase : bases) {
                                    for (final PsiElement match : arguments) {
                                        if (match != current && !reported.contains(match) && OpenapiEquivalenceUtil.areEqual(discoveredBase, match)) {
                                            holder.registerProblem(
                                                    match,
                                                    MessagesPresentationUtil.prefixWithEa(message),
                                                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                                    new DropArgumentFix()
                                            );
                                            reported.add(match);
                                        }
                                    }
                                }
                                bases.clear();
                            }
                        }
                    }
                    reported.clear();
                }
            }
        };
    }

    private static final class DropArgumentFix implements LocalQuickFix {
        private static final String title = "Drop unnecessary argument";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null && !project.isDisposed()) {
                final PsiElement parent = target.getParent();
                if (parent instanceof PhpIsset) {
                    final PsiElement[] arguments = ((PhpIsset) parent).getVariables();
                    final boolean isLast         = arguments.length > 0 && arguments[arguments.length - 1] == target;
                    if (isLast) {
                        PsiElement from = target.getPrevSibling();
                        while (from != null && !OpenapiTypesUtil.is(from, PhpTokenTypes.opCOMMA)) {
                            from = from.getPrevSibling();
                        }
                        if (from != null) {
                            target.getParent().deleteChildRange(from, target);
                        }
                    } else {
                        PsiElement to = target.getNextSibling();
                        while (to != null && !OpenapiTypesUtil.is(to, PhpTokenTypes.opCOMMA)) {
                            to = to.getNextSibling();
                        }
                        if (to != null) {
                            to = to.getNextSibling() instanceof PsiWhiteSpace ? to.getNextSibling() : to;
                            target.getParent().deleteChildRange(target, to);
                        }
                    }
                }
            }
        }
    }

}
