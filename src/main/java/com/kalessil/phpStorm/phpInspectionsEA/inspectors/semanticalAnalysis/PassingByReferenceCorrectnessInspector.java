package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.Variable;
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

public class PassingByReferenceCorrectnessInspector extends BasePhpInspection {
    private static final String message = "...";

    @NotNull
    public String getShortName() {
        return "PassingByReferenceCorrectnessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                this.analyze(reference);
            }

            public void visitPhpMethodReference(MethodReference reference) {
                this.analyze(reference);
            }

            private void analyze(@NotNull FunctionReference reference) {
                final PsiElement[] parameters = reference.getParameters();
                if (parameters.length != 0) {
                    boolean doAnalyze = false;
                    for (PsiElement parameter : parameters) {
                        boolean isRefCompatible = parameter instanceof Variable || parameter instanceof NewExpression;
                        if (!isRefCompatible) {
                            doAnalyze = true;
                            break;
                        }
                    }
                    if (doAnalyze) {
                        /* TODO: evaluate passing props, array items, method refs */
                        /* resolve and check parameters by reference */
                        /* function ref -> param by ref => resolve, warning if returns non-reference */
                        /* other expressions type => error */
                    }
                }
            }
        };
    }
}
