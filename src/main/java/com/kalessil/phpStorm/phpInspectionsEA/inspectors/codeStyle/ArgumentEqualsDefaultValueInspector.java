package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.kalessil.phpStorm.phpInspectionsEA.indexers.NamedCallableParametersMetaIndexer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ArgumentEqualsDefaultValueInspector extends BasePhpInspection {
    private static final String message = "The argument can be safely dropped, as it's identical to the default value.";

    private static final Set<String> specialFunctions = new HashSet<>();
    private static final Set<String> specialConstants = new HashSet<>();
    static {
        /* in exceptions die to conflict with strict types inspection, which requires argument specification */
        specialFunctions.add("array_search");
        specialFunctions.add("in_array");
        specialFunctions.add("json_decode");
        specialFunctions.add("is_subclass_of");
        specialFunctions.add("is_a");
        specialFunctions.add("iterator_to_array");
        specialFunctions.add("uniqid");
        specialFunctions.add("glob");

        specialConstants.add("__LINE__");
        specialConstants.add("__FILE__");
        specialConstants.add("__DIR__");
        specialConstants.add("__FUNCTION__");
        specialConstants.add("__CLASS__");
        specialConstants.add("__TRAIT__");
        specialConstants.add("__METHOD__");
        specialConstants.add("__NAMESPACE__");
    }

    @NotNull
    public final String getShortName() {
        return "ArgumentEqualsDefaultValueInspection";
    }

    @NotNull
    @Override
    public final PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean onTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                if (this.isContainingFileSkipped(reference)) { return; }

                this.analyze(reference);
            }

            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.isContainingFileSkipped(reference)) { return; }

                this.analyze(reference);
            }

            private void analyze(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && !specialFunctions.contains(functionName)) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > 0) {
                        PsiElement reportFrom = null;
                        PsiElement reportTo   = null;

                        final IElementType valueType = arguments[arguments.length - 1].getNode().getElementType();
                        if (OpenapiTypesUtil.DEFAULT_VALUES.contains(valueType)) {
                            final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                            if (resolved instanceof Function) {
                                final Function function      = (Function) resolved;
                                final Parameter[] parameters = function.getParameters();
                                if (arguments.length <= parameters.length) {
                                    final FileBasedIndex projectIndex   = FileBasedIndex.getInstance();
                                    final GlobalSearchScope searchScope = GlobalSearchScope.allScope(function.getProject());
                                    for (int index = Math.min(parameters.length, arguments.length) - 1; index >= 0; --index) {
                                        final Parameter parameter = parameters[index];
                                        final PsiElement argument = arguments[index];
                                        final String defaultValue = this.getDefaultValue(function, parameter.getName(), projectIndex, searchScope);
                                        /* false-positives: magic constants, unmatched values */
                                        if (defaultValue == null || specialConstants.contains(defaultValue) || !defaultValue.equals(argument.getText())) {
                                            break;
                                        }

                                        reportFrom = argument;
                                        reportTo   = reportTo == null ? argument : reportTo;
                                    }
                                }
                            }
                        }

                        if (reportFrom != null) {
                            problemsHolder.registerProblem(
                                    problemsHolder.getManager().createProblemDescriptor(
                                            reportFrom,
                                            reportTo,
                                            message,
                                            ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                            onTheFly,
                                            new TheLocalFix(reportFrom, reportTo)
                                    )
                            );
                        }
                    }
                }
            }

            @Nullable
            private String getDefaultValue(
                    @NotNull Function function,
                    @NotNull String parameterName,
                    @NotNull FileBasedIndex projectIndex,
                    @NotNull GlobalSearchScope searchScope
            ) {
                String result              = null;
                final List<String> details = projectIndex.getValues(
                        NamedCallableParametersMetaIndexer.identity,
                        String.format("%s.%s", function.getFQN(), parameterName),
                        searchScope
                );
                if (details.size() == 1) {
                    final String[] meta = details.get(0).split(";", 3); // the data format "ref:%s;var:%s;def:%s"
                    if (meta.length == 3) {
                        final String[] defaultMeta = meta[2].split(":", 2);
                        if (defaultMeta.length == 2 && !defaultMeta[1].isEmpty()) {
                            result = defaultMeta[1];
                        }
                    }
                }
                details.clear();

                return result;
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Drop unneeded arguments";

        private final SmartPsiElementPointer<PsiElement> dropFrom;
        private final SmartPsiElementPointer<PsiElement> dropTo;

        private TheLocalFix(@NotNull PsiElement dropFrom, @NotNull PsiElement dropTo) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(dropFrom.getProject());

            this.dropFrom = factory.createSmartPsiElementPointer(dropFrom);
            this.dropTo   = factory.createSmartPsiElementPointer(dropTo);
        }

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
            PsiElement dropFrom     = this.dropFrom.getElement();
            final PsiElement dropTo = this.dropTo.getElement();
            if (dropFrom != null && dropTo != null && !project.isDisposed()) {
                PsiElement previous = dropFrom.getPrevSibling();
                while (
                    previous instanceof PsiWhiteSpace || previous instanceof PsiComment ||
                    OpenapiTypesUtil.is(previous, PhpTokenTypes.opCOMMA)
                ) {
                    dropFrom = previous;
                    previous = previous.getPrevSibling();
                }
                dropFrom.getParent().deleteChildRange(dropFrom, dropTo);
            }
        }
    }
}
