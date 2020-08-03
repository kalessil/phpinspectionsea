package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.byReference;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class PassingByReferenceCorrectnessInspector extends BasePhpInspection {
    private static final String message = "Emits a notice (only variable references should be returned/passed by reference).";

    private static final Map<String, String> skippedFunctionsCache = new ConcurrentHashMap<>();
    private static final Set<String> skippedFunctions              = new HashSet<>();
    static {
        /* workaround for https://youtrack.jetbrains.com/issue/WI-37984 */
        skippedFunctions.add("current");
        skippedFunctions.add("key");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "PassingByReferenceCorrectnessInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Passing arguments by reference correctness";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && !functionName.isEmpty() && !skippedFunctionsCache.containsKey(functionName)) {
                    final boolean skip = skippedFunctions.contains(functionName) && this.isFromRootNamespace(reference);
                    if (!skip && this.hasIncompatibleArguments(reference)) {
                        this.analyze(reference);
                    }
                }
            }

            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                final String methodName = reference.getName();
                if (methodName != null && !methodName.isEmpty() && this.hasIncompatibleArguments(reference)) {
                    this.analyze(reference);
                }
            }

            private boolean hasIncompatibleArguments(@NotNull FunctionReference reference) {
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length > 0) {
                    final boolean supportsNew = PhpLanguageLevel.get(holder.getProject()).below(PhpLanguageLevel.PHP700);
                    return !Arrays.stream(arguments).allMatch(a -> a instanceof Variable || (supportsNew && a instanceof NewExpression));
                }
                return false;
            }

            private void analyze(@NotNull FunctionReference reference) {
                final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                if (resolved instanceof Function) {
                    final Function function      = (Function) resolved;
                    final Parameter[] parameters = function.getParameters();
                    final PsiElement[] arguments = reference.getParameters();
                    /* search for anomalies */
                    for (int index = 0, max = Math.min(parameters.length, arguments.length); index < max; ++index) {
                        if (parameters[index].isPassByRef()) {
                            final PsiElement argument = arguments[index];
                            if (argument instanceof FunctionReference && !this.isByReference(argument)) {
                                final PsiElement inner = OpenapiResolveUtil.resolveReference((FunctionReference) argument);
                                if (inner instanceof Function) {
                                    final PsiElement name = NamedElementUtil.getNameIdentifier((Function) inner);
                                    if (!this.isByReference(name)) {
                                        holder.registerProblem(
                                                argument,
                                                MessagesPresentationUtil.prefixWithEa(message)
                                        );
                                    }
                                }
                            } else if (argument instanceof NewExpression) {
                                holder.registerProblem(
                                        argument,
                                        MessagesPresentationUtil.prefixWithEa(message)
                                );
                            }
                        }
                    }
                    /* remember global functions without references */
                    if (parameters.length > 0 && OpenapiTypesUtil.isFunctionReference(reference)) {
                        final boolean hasReferences = Arrays.stream(parameters).anyMatch(Parameter::isPassByRef);
                        if (!hasReferences) {
                            final String functionName         = function.getName();
                            final boolean isFromRootNamespace = function.getFQN().equals('\\' + functionName);
                            if (isFromRootNamespace) {
                                skippedFunctionsCache.putIfAbsent(functionName, functionName);
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
