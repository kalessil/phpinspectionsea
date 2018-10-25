package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.byReference;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

public class PassingByReferenceCorrectnessInspector extends BasePhpInspection {
    private static final String message = "Emits a notice (only variable references should be dispatched by reference).";

    private static final Set<String> skippedFunctions = new HashSet<>();
    static {
        /* workaround for https://youtrack.jetbrains.com/issue/WI-37984 */
        skippedFunctions.add("current");
        skippedFunctions.add("key");
    }

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
                final String functionName = reference.getName();
                if (functionName != null) {
                    final boolean skip = skippedFunctions.contains(functionName) && this.isFromRootNamespace(reference);
                    if (!skip) {
                        this.analyze(
                                reference,
                                PhpProjectConfigurationFacade.getInstance(reference.getProject()).getLanguageLevel()
                        );
                    }
                }
            }

            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                this.analyze(
                        reference,
                        PhpProjectConfigurationFacade.getInstance(reference.getProject()).getLanguageLevel()
                );
            }

            private void analyze(@NotNull FunctionReference reference, @NotNull PhpLanguageLevel php) {
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length > 0) {
                    /* lazy analysis: if all args are valid, not even bother resolving the reference */
                    boolean doAnalyze = false;
                    for (final PsiElement argument : arguments) {
                        boolean isRefCompatible = argument instanceof Variable ||
                                                  (argument instanceof NewExpression && php.compareTo(PhpLanguageLevel.PHP700) >= 0);
                        if (!isRefCompatible) {
                            doAnalyze = true;
                            break;
                        }
                    }

                    final PsiElement resolved = doAnalyze ? OpenapiResolveUtil.resolveReference(reference) : null;
                    if (resolved instanceof Function) {
                        final Parameter[] parameters = ((Function) resolved).getParameters();
                        for (int index = 0, max = Math.min(parameters.length, arguments.length); index < max; ++index) {
                            if (parameters[index].isPassByRef()) {
                                final PsiElement argument = arguments[index];
                                if (argument instanceof FunctionReference && !this.isByReference(argument)) {
                                    final PsiElement function = OpenapiResolveUtil.resolveReference((FunctionReference) argument);
                                    if (function instanceof Function) {
                                        final PsiElement name = NamedElementUtil.getNameIdentifier((Function) function);
                                        if (!this.isByReference(name)) {
                                            holder.registerProblem(argument, message);
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
                    PsiElement ampersandCandidate = element.getPrevSibling();
                    if (ampersandCandidate instanceof PsiWhiteSpace) {
                        ampersandCandidate = ampersandCandidate.getPrevSibling();
                    }
                    result = OpenapiTypesUtil.is(ampersandCandidate, PhpTokenTypes.opBIT_AND);
                }
                return result;
            }
        };
    }
}
