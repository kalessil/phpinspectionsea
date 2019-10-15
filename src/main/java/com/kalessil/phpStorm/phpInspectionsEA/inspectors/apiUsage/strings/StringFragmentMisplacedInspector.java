package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class StringFragmentMisplacedInspector extends PhpInspection {
    private static final String messagePattern  = "'%s' should probably be the second argument (replace with a constant if intended).";

    private static final Set<String> functions = new HashSet<>();

    static {
        functions.add("strpos");
        functions.add("stripos");
        functions.add("mb_strpos");
        functions.add("mb_stripos");
        functions.add("strrpos");
        functions.add("strripos");
        functions.add("mb_strrpos");
        functions.add("mb_strripos");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "StringFragmentMisplacedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Incorrectly placed string fragment";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functions.contains(functionName)) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length >= 2 && arguments[0] instanceof StringLiteralExpression) {
                        final StringLiteralExpression fragment = (StringLiteralExpression) arguments[0];
                        if (fragment.getFirstPsiChild() == null) {
                            final boolean isTarget = arguments[1] != null && !(arguments[1] instanceof StringLiteralExpression);
                            if (isTarget) {
                                holder.registerProblem(
                                        fragment,
                                        String.format(ReportingUtil.wrapReportedMessage(messagePattern), fragment.getText()),
                                        new ReorderArgumentsFix()
                                );
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class ReorderArgumentsFix implements LocalQuickFix {
        private static final String title = "Fix the fragment position";

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
            final PsiElement expression = descriptor.getPsiElement();
            if (expression != null && !project.isDisposed()) {
                final PsiElement grandParent = expression.getParent().getParent();
                if (grandParent instanceof FunctionReference) {
                    final PsiElement[] arguments = ((FunctionReference) grandParent).getParameters();
                    final PsiElement fragment    = arguments[0].copy();
                    arguments[0].replace(arguments[1]);
                    arguments[1].replace(fragment);
                }
            }
        }
    }
}
