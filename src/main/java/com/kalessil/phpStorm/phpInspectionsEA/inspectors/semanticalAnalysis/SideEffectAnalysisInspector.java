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
import com.intellij.openapi.util.Key;
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
    private static final String message = "This call can be removed because it have no side-effect.";

    private static final Key<Integer>    ReferenceIndex = Key.create("SideEffect.ReferenceIndex");
    private static final Key<SideEffect> SideEffectType = Key.create("SideEffect.Type");

    private static HashMap<String, SideEffect> mappedPhpFunctions = new HashMap<>();

    private enum SideEffect {NONE, POSSIBLE, UNKNOW, INTERNAL, EXTERNAL}

    static {
        mappedPhpFunctions.put("\\abort", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\apache_setenv", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\assert", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\call_user_func", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\call_user_func_array", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\chdir", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\chmod", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\class_exists", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\clearstatcache", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\closelog", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\copy", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\date_default_timezone_set", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\error_reporting", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\exec", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\extract", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\file_put_contents", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\forward_static_call", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\header", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\ini_set", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\json_decode", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\mkdir", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\mt_srand", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\ob_end_clean", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\ob_start", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\passthru", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\pcntl_alarm", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\pcntl_async_signals", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\pcntl_signal", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\posix_kill", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\putenv", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\register_shutdown_function", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\rename", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\restore_error_handler", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\set_error_handler", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\set_exception_handler", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\set_time_limit", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\sleep", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\spl_autoload_register", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\spl_autoload_unregister", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\sqlsrv_fetch", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\srand", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\syslog", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\touch", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\trigger_error", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\unlink", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\usleep", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\var_dump", SideEffect.EXTERNAL);
        mappedPhpFunctions.put("\\xcache_clear_cache", SideEffect.EXTERNAL);
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
            mapRefIndex(function);
            return SideEffect.POSSIBLE;
        }

        return SideEffect.NONE;
    }

    private static void mapRefIndex(@NotNull final Function function) {
        final Parameter[] functionParameters = function.getParameters();

        for (int functionParametersIndex = 0; functionParametersIndex < functionParameters.length; functionParametersIndex++) {
            if (functionParameters[functionParametersIndex].isPassByRef()) {
                function.putUserData(ReferenceIndex, functionParametersIndex + 1);
                break;
            }
        }
    }

    @NotNull
    private static SideEffect getIdentifiedSideEffect(@NotNull final FunctionReference functionReference) {
        final Function function = (Function) functionReference.resolve();
        if (null == function) {
            return SideEffect.UNKNOW;
        }

        SideEffect sideEffect = function.getUserData(SideEffectType);
        if (null == sideEffect) {
            final String functionQualifiedName = function.getFQN();

            sideEffect = mappedPhpFunctions.containsKey(functionQualifiedName)
                ? mappedPhpFunctions.get(functionQualifiedName)
                : identifySideEffect(functionReference);

            function.putUserData(SideEffectType, sideEffect);
        }

        return sideEffect;
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
            public void visitPhpFunctionCall(@NotNull final FunctionReference functionReference) {
                final String functionSimplifiedName = functionReference.getName();
                if (null != functionSimplifiedName && functionSimplifiedName.isEmpty()) {
                    return;
                }

                final Function function = (Function) functionReference.resolve();
                if (null != function && functionReference.getParent().getClass().equals(StatementImpl.class)) {
                    final SideEffect functionSideEffect = getIdentifiedSideEffect(functionReference);

                    if (functionSideEffect.equals(SideEffect.NONE)) {
                        registerProblem(functionReference);
                    }
                    else if (functionSideEffect.equals(SideEffect.POSSIBLE)) {
                        final Integer functionParameterReferencePosition = function.getUserData(ReferenceIndex);
                        if (null != functionParameterReferencePosition &&
                            functionReference.getParameters().length < functionParameterReferencePosition) {
                            registerProblem(functionReference);
                        }
                    }
                }
            }

            private void registerProblem(@NotNull final FunctionReference functionReference) {
                holder.registerProblem(functionReference.getParent(), message, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
