package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.byReference;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class PassingByReferenceCorrectnessInspector extends BasePhpInspection {
    private static final String message = "Emits a notice (only variable references should be returned by reference)";

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
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length != 0) {
                    /* lazy analysis: if all args are valid, not even bother resolving the reference */
                    boolean doAnalyze = false;
                    for (PsiElement parameter : arguments) {
                        boolean isRefCompatible = parameter instanceof Variable || parameter instanceof NewExpression;
                        if (!isRefCompatible) {
                            doAnalyze = true;
                            break;
                        }
                    }
                    final PsiElement resolved = doAnalyze ? reference.resolve() : null;
                    if (resolved instanceof Function && ((Function) resolved).hasRefParams()) {
                        /* iterate parameters and match references against provided arguments */
                        final Parameter[] parameters = ((Function) resolved).getParameters();
                        for (int index = 0, max = Math.min(parameters.length, arguments.length); index < max; ++index) {
                            if (parameters[index].isPassByRef()) {
                                final PsiElement argument = arguments[index];
                                if (argument instanceof FunctionReference && !this.isByReference(argument)) {
                                    final PsiElement resolvedArgument = ((FunctionReference) argument).resolve();
                                    if (resolvedArgument instanceof Function) {
                                        final PsiElement name = NamedElementUtil.getNameIdentifier((Function) resolvedArgument);
                                        if (!this.isByReference(name)) {
                                            holder.registerProblem(argument, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                                            // continue;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private boolean isByReference(@Nullable PsiElement element) {
                boolean result = false;
                if (element != null) {
                    PsiElement refCandidate = element.getPrevSibling();
                    if (refCandidate instanceof PsiWhiteSpace) {
                        refCandidate = refCandidate.getPrevSibling();
                    }
                    result = null != refCandidate && PhpTokenTypes.opBIT_AND == refCandidate.getNode().getElementType();
                }
                return result;
            }
        };
    }
}
