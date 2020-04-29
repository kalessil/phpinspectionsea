package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class CascadingDirnameCallsInspector extends PhpInspection {
    private static final String messagePattern = "'%e%' can be used instead (reduces amount of calls).";

    @NotNull
    @Override
    public String getShortName() {
        return "CascadingDirnameCallsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Cascading dirname(...) calls";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_LANGUAGE_LEVEL_MIGRATION)) { return; }

                final String functionName = reference.getName();
                if (functionName == null || !functionName.equals("dirname")) {
                    return;
                }
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length != 1 && arguments.length != 2) {
                    return;
                }

                /* require PHP7+, where 2nd parameter has been added */
                if (PhpLanguageLevel.get(holder.getProject()).below(PhpLanguageLevel.PHP700)) {
                    return;
                }

                /* don't report nested calls, we want to report the one on top level */
                final PsiElement parent = reference.getParent();
                if (parent instanceof ParameterList && parent.getParent() instanceof FunctionReference) {
                    final FunctionReference parentReference = (FunctionReference) parent.getParent();
                    final String parentName                 = parentReference.getName();
                    if (parentName != null && parentName.equals("dirname")) {
                        final PsiElement[] parentArguments = parentReference.getParameters();
                        if (parentArguments.length == 1 || parentArguments.length == 2) {
                            return;
                        }
                    }
                }


                int directoryLevel            = 0;
                PsiElement argument           = null;
                final List<PsiElement> levels = new ArrayList<>();

                FunctionReference current = reference;
                while (current instanceof FunctionReference) {
                    final String currentName = current.getName();
                    if (currentName == null || !currentName.equals("dirname")) {
                        break;
                    }

                    final PsiElement[] currentArguments = current.getParameters();
                    if (currentArguments.length == 1) {
                        argument = currentArguments[0];
                        ++directoryLevel;
                    } else if (currentArguments.length == 2) {
                        argument = currentArguments[0];
                        levels.add(currentArguments[1]);
                    } else {
                        break;
                    }

                    if (!(currentArguments[0] instanceof FunctionReference)) {
                        break;
                    }
                    current = (FunctionReference) currentArguments[0];
                }
                /* if no nested dirname calls, stop analysis */
                if (current == reference) {
                    levels.clear();
                    return;
                }

                /* if we have 1+ nested call (top-level one is not considered) */
                if (argument != null && arguments[0] != argument) {
                    /* process extracted level expressions: numbers to sum-up, expressions to stay */
                    final List<String> reported = new ArrayList<>();
                    for (PsiElement levelEntry : levels) {
                        try {
                            directoryLevel += Integer.parseInt(levelEntry.getText());
                        } catch (NumberFormatException fail) {
                            reported.add(levelEntry.getText());
                        }
                    }
                    levels.clear();

                    /* do not report cases with one level extraction */
                    if (1 == directoryLevel && reported.isEmpty()){
                        return;
                    }

                    /* generate the replacement expression */
                    reported.add(0, String.valueOf(directoryLevel));
                    final String replacement = "dirname(%a%, %l%)"
                            .replace("%a%", argument.getText())
                            .replace("%l%", String.join(" + ", reported));
                    reported.clear();

                    holder.registerProblem(
                            reference,
                            ReportingUtil.wrapReportedMessage(messagePattern.replace("%e%", replacement)),
                            new TheLocalFix(replacement)
                    );
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Collapse dirname(...) calls";

        final private String expression;

        @NotNull
        @Override
        public String getName() {
            return ReportingUtil.wrapReportedMessage(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        TheLocalFix(@NotNull String expression) {
            super();
            this.expression = expression;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference && !project.isDisposed()) {
                final ParenthesizedExpression replacement
                    = PhpPsiElementFactory.createFromText(project, ParenthesizedExpression.class, '(' + this.expression + ')');
                if (null != replacement) {
                    expression.replace(replacement.getArgument());
                }
            }
        }
    }
}
