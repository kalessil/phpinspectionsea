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
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SideEffectAnalysisInspector extends BasePhpInspection {
    private static final String                      message            = "This call can be removed because it have no side-effect.";
    private static       HashMap<String, SideEffect> mappedSideEffects  = new HashMap<>();
    private static       HashMap<String, Integer>    mappedRefPositions = new HashMap<>();

    private enum SideEffect {NONE, POSSIBLE, UNKNOW, INTERNAL, EXTERNAL}

    static {
        mappedSideEffects.put("\\abort", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\apache_setenv", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\assert", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\call_user_func", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\call_user_func_array", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\chdir", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\chmod", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\class_exists", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\clearstatcache", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\closelog", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\copy", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\date_default_timezone_set", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\error_reporting", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\exec", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\extract", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\file_put_contents", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\forward_static_call", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\header", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\ini_set", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\json_decode", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\mkdir", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\mt_srand", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\ob_end_clean", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\ob_start", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\passthru", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\pcntl_alarm", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\pcntl_async_signals", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\pcntl_signal", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\posix_kill", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\putenv", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\register_shutdown_function", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\rename", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\restore_error_handler", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\set_error_handler", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\set_exception_handler", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\set_time_limit", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\sleep", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\spl_autoload_register", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\spl_autoload_unregister", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\sqlsrv_fetch", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\srand", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\syslog", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\touch", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\trigger_error", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\unlink", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\usleep", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\var_dump", SideEffect.EXTERNAL);
        mappedSideEffects.put("\\xcache_clear_cache", SideEffect.EXTERNAL);
    }

    @NotNull
    private static SideEffect identifySideEffect(@NotNull final FunctionReference functionReference) {
        final Function function = (Function) functionReference.resolve();
        if (null == function) {
            return SideEffect.UNKNOW;
        }

        final Parameter[] functionParameters = function.getParameters();
        for (Parameter functionParameter : functionParameters) {
            if (functionParameter.getType().equals(PhpType.RESOURCE)) {
                return SideEffect.EXTERNAL;
            }
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
                final String functionSimplifiedName = functionReference.getName();
                if (null != functionSimplifiedName && functionSimplifiedName.isEmpty()) {
                    return;
                }

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
