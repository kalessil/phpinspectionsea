package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SideEffectAnalysisInspector extends BasePhpInspection {
    private static final String                      message            = "This call can be removed because it have no side-effect.";
    private static       HashMap<String, SideEffect> mappedSideEffects  = new HashMap<>();
    private static       HashMap<String, Integer>    mappedRefPositions = new HashMap<>();

    private enum SideEffect {NONE, POSSIBLE, UNKNOW, INTERNAL, EXTERNAL}

    @NotNull
    private static SideEffect identifySideEffect(@NotNull final FunctionReference functionReference) {
        final Function function = (Function) functionReference.resolve();
        if (null == function) {
            return SideEffect.UNKNOW;
        }

        if (function.hasRefParams()) {
            saveRefPosition(function);
            return SideEffect.POSSIBLE;
        }

        return SideEffect.NONE;
    }

    private static void saveRefPosition(@NotNull final Function function) {
        final Parameter[] functionParameters = function.getParameters();

        for (int functionParametersIndex = 0; functionParametersIndex < functionParameters.length; functionParametersIndex++) {
            if (functionParameters[functionParametersIndex].isPassByRef()) {
                mappedRefPositions.put(function.getFQN(), functionParametersIndex + 1);
                break;
            }
        }
    }

    @NotNull
    private static SideEffect getIdentifiedSideEffect(@NotNull final FunctionReference functionReference, @NotNull final String functionQualifiedName) {
        if (!mappedSideEffects.containsKey(functionQualifiedName)) {
            mappedSideEffects.put(functionQualifiedName, identifySideEffect(functionReference));
        }

        return mappedSideEffects.get(functionQualifiedName);
    }

    @NotNull
    public String getShortName() {
        return "SideEffectAnalysisInspector";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(final FunctionReference functionReference) {
                final Function function = (Function) functionReference.resolve();
                if (null != function && functionReference.getParent().getClass().equals(StatementImpl.class)) {
                    final String     functionQualifiedName = function.getFQN();
                    final SideEffect functionSideEffect    = getIdentifiedSideEffect(functionReference, functionQualifiedName);

                    if (functionSideEffect.equals(SideEffect.NONE)) {
                        registerProblem(functionReference);
                    }
                    else if (functionSideEffect.equals(SideEffect.POSSIBLE) &&
                        functionReference.getParameters().length < mappedRefPositions.get(functionQualifiedName)) {
                        registerProblem(functionReference);
                    }
                }
            }

            private void registerProblem(@NotNull final FunctionReference functionReference) {
                holder.registerProblem(functionReference.getParent(), message, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
