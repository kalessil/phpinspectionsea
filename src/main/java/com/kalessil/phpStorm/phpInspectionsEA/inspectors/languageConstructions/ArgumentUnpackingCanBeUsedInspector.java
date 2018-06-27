package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ArgumentUnpackingCanBeUsedInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' should be used instead (3x+ faster)";

    @NotNull
    public String getShortName() {
        return "ArgumentUnpackingCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP560) >= 0) {
                    final String functionName = reference.getName();
                    if (functionName != null && functionName.equals("call_user_func_array")) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 2) {
                            final boolean isContainerValid =
                                    arguments[1] instanceof Variable ||
                                    arguments[1] instanceof FieldReference ||
                                    arguments[1] instanceof ArrayCreationExpression ||
                                    arguments[1] instanceof FunctionReference;
                            if (isContainerValid && arguments[0] instanceof StringLiteralExpression) {
                                /* do not process strings with injections */
                                final StringLiteralExpression targetFunction = (StringLiteralExpression) arguments[0];
                                if (targetFunction.getFirstPsiChild() == null) {
                                    final String replacement
                                            = String.format("%s(...%s)", targetFunction.getContents(), arguments[1].getText());
                                    holder.registerProblem(
                                            reference,
                                            String.format(messagePattern, replacement),
                                            new UnpackFix(replacement)
                                    );
                                }
                            }
                        }
                    }
                }
                // TODO: if (isContainerValid && params[0] instanceof ArrayCreationExpression) {
                // TODO: call_user_func_array([...], ...); string method name must not contain ::
            }
        };
    }

    private static final class UnpackFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use unpack argument syntax instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UnpackFix(@NotNull String expression) {
            super(expression);
        }
    }
}