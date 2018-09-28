package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.byReference;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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
    private static final String message = "Emits a notice (only variable references should be returned by reference).";

    @NotNull
    public String getShortName() {
        return "PassingByReferenceCorrectnessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.isContainingFileSkipped(reference)) { return; }

                final String functionName = reference.getName();
                if (functionName != null) {
                    /* workaround for https://youtrack.jetbrains.com/issue/WI-37984 */
                    final boolean shouldSkip =
                            (functionName.equals("current") || functionName.equals("key")) &&
                            this.isFromRootNamespace(reference);
                    if (!shouldSkip) {
                        this.analyze(reference);
                    }
                }
            }

            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                if (this.isContainingFileSkipped(reference)) { return; }

                this.analyze(reference);
            }

            private void analyze(@NotNull FunctionReference reference) {
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length > 0) {
                    /* lazy analysis: if all args are valid, not even bother resolving the reference */
                    boolean doAnalyze = false;
                    for (final PsiElement argument : arguments) {
                        boolean isRefCompatible = argument instanceof Variable || argument instanceof NewExpression;
                        if (!isRefCompatible) {
                            doAnalyze = true;
                            break;
                        }
                    }

                    final PsiElement resolved = doAnalyze ? OpenapiResolveUtil.resolveReference(reference) : null;
                    if (resolved instanceof Function) {
                        final Parameter[] parameters = ((Function) resolved).getParameters();
                        for (int index = 0, max = Math.min(parameters.length, arguments.length); index < max; ++index) {
                            final PsiElement argument = arguments[index];
                            if (
                                argument instanceof FunctionReference &&
                                parameters[index].isPassByRef() &&
                                !this.isByReference(argument)
                            ) {
                                final PsiElement resolvedArgument = OpenapiResolveUtil.resolveReference((FunctionReference) argument);
                                if (resolvedArgument instanceof Function) {
                                    final PsiElement name = NamedElementUtil.getNameIdentifier((Function) resolvedArgument);
                                    if (!this.isByReference(name)) {
                                        holder.registerProblem(argument, message);
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
                    result = OpenapiTypesUtil.is(refCandidate, PhpTokenTypes.opBIT_AND);
                }
                return result;
            }
        };
    }
}
