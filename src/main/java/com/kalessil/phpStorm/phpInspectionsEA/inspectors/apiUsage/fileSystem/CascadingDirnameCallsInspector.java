package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class CascadingDirnameCallsInspector extends BasePhpInspection {
    private static final String messagePattern = "'%e%' can be used instead (reduces amount of calls).";

    @NotNull
    public String getShortName() {
        return "CascadingDirnameCallsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName == null || !functionName.equals("dirname")) {
                    return;
                }
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length != 1 && arguments.length != 2) {
                    return;
                }

                /* require PHP7+, where 2nd parameter has been added */
                PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP700) < 0) {
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
                    if (1 == directoryLevel && 0 == reported.size()){
                        reported.clear();
                        return;
                    }

                    /* generate the replacement expression */
                    reported.add(0, String.valueOf(directoryLevel));
                    final String replacement = "dirname(%a%, %l%)"
                            .replace("%a%", argument.getText())
                            .replace("%l%", String.join(" + ", reported));
                    reported.clear();

                    final String message = messagePattern.replace("%e%", replacement);
                    holder.registerProblem(reference, message, new TheLocalFix(replacement));
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
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        TheLocalFix(@NotNull String expression) {
            super();
            this.expression = expression;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference) {
                final ParenthesizedExpression replacement
                    = PhpPsiElementFactory.createFromText(project, ParenthesizedExpression.class, '(' + this.expression + ')');
                if (null != replacement) {
                    expression.replace(replacement.getArgument());
                }
            }
        }
    }
}
