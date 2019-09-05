package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class LowPerformingDirectoryOperationsInspector extends PhpInspection {
    private static final String messageSortsByDefaultPattern = "'%s(...)' sorts results by default, please provide second argument for specifying the intention.";
    private static final String messageUnboxGlobPattern      = "'%s' would be more performing here (reduces amount of file system interactions).";

    @NotNull
    @Override
    public String getShortName() {
        return "LowPerformingDirectoryOperationsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Low performing directory operations";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && (functionName.equals("scandir") || functionName.equals("glob"))) {
                    final PsiElement[] arguments = reference.getParameters();

                    /* case: glob call results passed thru is_dir */
                    if (functionName.equals("glob")) {
                        final PsiElement parent  = reference.getParent();
                        final PsiElement context = parent instanceof ParameterList ? parent.getParent() : parent;
                        if (OpenapiTypesUtil.isFunctionReference(context)) {
                            final FunctionReference outerCall = (FunctionReference) context;
                            final String outerFunctionName    = outerCall.getName();
                            if (outerFunctionName != null && outerFunctionName.equals("array_filter")) {
                                final PsiElement[] outerArguments = outerCall.getParameters();
                                if (outerArguments.length == 2 && outerArguments[1] instanceof StringLiteralExpression) {
                                    final String callback = ((StringLiteralExpression) outerArguments[1]).getContents().replace("\\", "");
                                    if (callback.equals("is_dir")) {
                                        final boolean needsFilter = PsiTreeUtil.findChildrenOfType(reference, ConstantReference.class).stream().noneMatch(c -> "GLOB_ONLYDIR".equals(c.getName()));
                                        final String replacement  = String.format(
                                                "%sglob(%s, %s)",
                                                reference.getImmediateNamespaceName(),
                                                arguments[0].getText(),
                                                needsFilter ? (arguments.length == 2 ? arguments[1].getText() + " | GLOB_ONLYDIR" : "GLOB_ONLYDIR") : arguments[1].getText()
                                        );
                                        holder.registerProblem(outerCall, String.format(messageUnboxGlobPattern, replacement), new OptimizeDirectoriesFilteringFix(replacement));
                                        return;
                                    }
                                }
                            }
                        }
                    }

                    /* case: sorting expectations are not clarified */
                    if (arguments.length == 1 && this.isFromRootNamespace(reference)) {
                        final String replacement = String.format(
                                "%s%s(%s, %s)",
                                reference.getImmediateNamespaceName(),
                                functionName,
                                arguments[0].getText(),
                                functionName.equals("scandir") ? "SCANDIR_SORT_NONE" : "GLOB_NOSORT"
                        );
                        holder.registerProblem(reference, String.format(messageSortsByDefaultPattern, functionName), new NoSortFix(replacement));
                    }
                }
            }
        };
    }

    private static final class NoSortFix extends UseSuggestedReplacementFixer {
        private static final String title = "Disable sorting by default";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        NoSortFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class OptimizeDirectoriesFilteringFix extends UseSuggestedReplacementFixer {
        private static final String title = "Optimize directories filtering";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        OptimizeDirectoriesFilteringFix(@NotNull String expression) {
            super(expression);
        }
    }
}
