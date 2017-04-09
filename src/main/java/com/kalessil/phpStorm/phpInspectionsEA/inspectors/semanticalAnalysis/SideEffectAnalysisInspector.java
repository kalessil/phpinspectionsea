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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SideEffectAnalysisInspector extends BasePhpInspection {
    private static final String messageNoSideEffect      = "This call can be removed because it have no side-effect.";
    private static final String messageInvalidAnnotation = "Unsupported value on property @side-effect.";

    private static final Key<Integer>    ReferenceIndex = Key.create("SideEffect.ReferenceIndex");
    private static final Key<SideEffect> SideEffectType = Key.create("SideEffect.Type");

    private static final HashMap<String, SideEffect> mappedPhpFunctions        = new HashMap<>();
    private static final HashMap<String, SideEffect> sideEffectAnnotationTypes = new HashMap<>();

    private static final Pattern sideEffectAnnotationPattern =
        Pattern.compile("(?:^\\s*\\*|^/\\*{2})\\s*@side-effect\\s*(?<type>\\w+)\\s*(?:$|\\*/)", Pattern.MULTILINE);

    private enum SideEffect {NONE, POSSIBLE, UNKNOW, INTERNAL, EXTERNAL}

    static {
        sideEffectAnnotationTypes.put("none", SideEffect.NONE);
        sideEffectAnnotationTypes.put("possible", SideEffect.POSSIBLE);
        sideEffectAnnotationTypes.put("unknow", SideEffect.UNKNOW);
        sideEffectAnnotationTypes.put("internal", SideEffect.INTERNAL);
        sideEffectAnnotationTypes.put("external", SideEffect.EXTERNAL);

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
    private static SideEffect identifySideEffect(@Nullable final Function function) {
        if (null == function) {
            return SideEffect.UNKNOW;
        }

        final Parameter[] functionParameters = function.getParameters();
        for (final Parameter functionParameter : functionParameters) {
            if (functionParameter.getType().equals(PhpType.RESOURCE)) {
                return SideEffect.EXTERNAL;
            }
        }

        if (function.hasRefParams()) {
            mapRefIndex(function);
            return SideEffect.POSSIBLE;
        }

        final Collection<FunctionReference> functionReferencesCall = PsiTreeUtil.findChildrenOfType(function, FunctionReference.class);
        for (final FunctionReference functionReferenceCall : functionReferencesCall) {
            final Function functionReferenceCallResolved = (Function) functionReferenceCall.resolve();
            if (functionReferenceCallResolved == function) {
                continue;
            }

            final SideEffect functionReferenceSideEffect = identifySideEffect(functionReferenceCallResolved);

            if (functionReferenceSideEffect == SideEffect.EXTERNAL ||
                functionReferenceSideEffect == SideEffect.UNKNOW) {
                return functionReferenceSideEffect;
            }
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
                : identifySideEffect((Function) functionReference.resolve());

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
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunction(@NotNull final Function function) {
                function.putUserData(SideEffectType, identifySideEffect(function));
                mapRefIndex(function);
                parseSideEffectAnnotation(function);
            }

            // TODO: Should have a way to improve this part with some OpenAPI method for read DocComment @side-effect.
            private void parseSideEffectAnnotation(@NotNull final Function function) {
                final PhpDocComment functionDocComment = function.getDocComment();
                if (null != functionDocComment) {
                    final String  functionDocCommentText    = functionDocComment.getText();
                    final Matcher functionDocCommentMatcher = sideEffectAnnotationPattern.matcher(functionDocCommentText);
                    if (functionDocCommentMatcher.find() &&
                        functionDocCommentMatcher.groupCount() == 1) {
                        final String sideEffectAnnotationValue = functionDocCommentMatcher.group("type").toLowerCase();
                        if (!sideEffectAnnotationTypes.containsKey(sideEffectAnnotationValue)) {
                            holder.registerProblem(functionDocComment, messageInvalidAnnotation, ProblemHighlightType.WEAK_WARNING);
                        }
                        else {
                            function.putUserData(SideEffectType, sideEffectAnnotationTypes.get(sideEffectAnnotationValue));
                        }
                    }
                }
            }

            @Override
            public void visitPhpNewExpression(@NotNull final NewExpression expression) {
                if (!expression.getParent().getClass().equals(StatementImpl.class)) {
                    return;
                }

                final ClassReference classReference = expression.getClassReference();
                if (null == classReference) {
                    return;
                }

                final PsiElement classReferenceResolved = classReference.resolve();
                if (null == classReferenceResolved) {
                    return;
                }

                final Method classConstructor = classReferenceResolved instanceof Method
                    ? (Method) classReferenceResolved
                    : ((PhpClass) classReferenceResolved).getConstructor();
                final SideEffect classConstructorSideEffect = identifySideEffect(classConstructor);

                if (classConstructorSideEffect.equals(SideEffect.NONE) ||
                    classConstructorSideEffect.equals(SideEffect.UNKNOW)) {
                    registerSideEffectProblem(expression);
                }
            }

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
                        registerSideEffectProblem(functionReference);
                    }
                    else if (functionSideEffect.equals(SideEffect.POSSIBLE)) {
                        final Integer functionParameterReferencePosition = function.getUserData(ReferenceIndex);
                        if (null != functionParameterReferencePosition &&
                            functionReference.getParameters().length < functionParameterReferencePosition) {
                            registerSideEffectProblem(functionReference);
                        }
                    }
                }
            }

            private void registerSideEffectProblem(@NotNull final PsiElement functionReference) {
                holder.registerProblem(functionReference.getParent(), messageNoSideEffect, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
