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
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.AssignmentExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class SideEffectAnalysisInspector extends BasePhpInspection {
    private static final String messageNoSideEffect        = "This call can be removed because it have no side-effect.";
    private static final String messageMultipleAnnotations = "Multiple declarations of @side-effect is not allowed.";
    private static final String messageInvalidAnnotation   = "Unsupported value on property @side-effect.";

    private static final Key<Integer>         ReferenceIndex = Key.create("SideEffect.ReferenceIndex");
    private static final Key<SideEffect.Type> SideEffectType = Key.create("SideEffect.Type");

    private static final HashMap<String, SideEffect.Type> mappedPhpFunctions        = new HashMap<>();
    private static final HashMap<String, SideEffect.Type> sideEffectAnnotationTypes = new HashMap<>();

    static {
        sideEffectAnnotationTypes.put("none", SideEffect.Type.NONE);
        sideEffectAnnotationTypes.put("possible", SideEffect.Type.POSSIBLE);
        sideEffectAnnotationTypes.put("unknow", SideEffect.Type.UNKNOW);
        sideEffectAnnotationTypes.put("internal", SideEffect.Type.INTERNAL);
        sideEffectAnnotationTypes.put("external", SideEffect.Type.EXTERNAL);

        mappedPhpFunctions.put("\\abort", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\apache_setenv", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\assert", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\call_user_func", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\call_user_func_array", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\chdir", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\chmod", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\class_exists", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\clearstatcache", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\closelog", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\copy", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\date_default_timezone_set", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\error_reporting", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\exec", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\extract", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\file_put_contents", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\forward_static_call", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\header", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\ini_set", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\json_decode", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\mkdir", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\mt_srand", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\ob_end_clean", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\ob_start", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\passthru", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\pcntl_alarm", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\pcntl_async_signals", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\pcntl_signal", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\posix_kill", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\putenv", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\register_shutdown_function", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\rename", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\restore_error_handler", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\set_error_handler", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\set_exception_handler", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\set_time_limit", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\sleep", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\spl_autoload_register", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\spl_autoload_unregister", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\sqlsrv_fetch", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\srand", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\syslog", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\touch", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\trigger_error", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\unlink", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\usleep", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\var_dump", SideEffect.Type.EXTERNAL);
        mappedPhpFunctions.put("\\xcache_clear_cache", SideEffect.Type.EXTERNAL);
    }

    @NotNull
    private static SideEffect.Type identifySideEffect(@Nullable final Function function) {
        if (null == function) {
            return SideEffect.Type.UNKNOW;
        }

        final Parameter[] functionParameters = function.getParameters();
        for (final Parameter functionParameter : functionParameters) {
            if (functionParameter.getType().equals(PhpType.RESOURCE)) {
                return SideEffect.Type.EXTERNAL;
            }
        }

        if (function.hasRefParams()) {
            mapRefIndex(function);
            return SideEffect.Type.POSSIBLE;
        }

        SideEffect.Type functionSideEffect = SideEffect.Type.NONE;

        final Collection<FunctionReference> functionReferencesCall = PsiTreeUtil.findChildrenOfType(function, FunctionReference.class);
        for (final FunctionReference functionReferenceCall : functionReferencesCall) {
            final Function functionReferenceCallResolved = (Function) functionReferenceCall.resolve();
            if (functionReferenceCallResolved == function) {
                continue;
            }

            final SideEffect.Type functionReferenceSideEffect = identifySideEffect(functionReferenceCallResolved);

            if (SideEffect.isPrecedenceHigherThan(functionReferenceSideEffect, functionSideEffect)) {
                functionSideEffect = functionReferenceSideEffect;

                if (SideEffect.isPrecedenceIsMax(functionSideEffect)) {
                    return functionSideEffect;
                }
            }
        }

        return functionSideEffect;
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
    private static SideEffect.Type getIdentifiedSideEffect(@NotNull final FunctionReference functionReference) {
        final Function function = (Function) functionReference.resolve();
        if (null == function) {
            return SideEffect.Type.UNKNOW;
        }

        SideEffect.Type sideEffect = function.getUserData(SideEffectType);
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

            private void parseSideEffectAnnotation(@NotNull final Function function) {
                final PhpDocComment functionDocComment = function.getDocComment();
                if (null != functionDocComment) {
                    final Collection<PhpDocTag> functionDocTags = PsiTreeUtil.findChildrenOfType(functionDocComment, PhpDocTag.class);
                    if (functionDocTags.size() == 0) {
                        return;
                    }

                    final ArrayList<PhpDocTag> functionDocSideEffectTags = new ArrayList<>();
                    for (final PhpDocTag functionDocTag : functionDocTags) {
                        if (functionDocTag.getName().toLowerCase().equals("@side-effect")) {
                            functionDocSideEffectTags.add(functionDocTag);
                        }
                    }

                    if (functionDocSideEffectTags.size() == 0) {
                        return;
                    }
                    else if (functionDocSideEffectTags.size() > 1) {
                        holder.registerProblem(functionDocComment, messageMultipleAnnotations, ProblemHighlightType.WEAK_WARNING);
                        return;
                    }

                    final PhpDocTag functionDocSideEffectTag      = functionDocSideEffectTags.get(0);
                    final String    functionDocSideEffectTagValue = functionDocSideEffectTag.getTagValue().toLowerCase();

                    if (!sideEffectAnnotationTypes.containsKey(functionDocSideEffectTagValue)) {
                        holder.registerProblem(functionDocComment, messageInvalidAnnotation, ProblemHighlightType.WEAK_WARNING);
                        return;
                    }

                    function.putUserData(SideEffectType, sideEffectAnnotationTypes.get(functionDocSideEffectTagValue));
                }
            }

            @Override
            public void visitPhpNewExpression(@NotNull final NewExpression expression) {
                final PsiElement expressionParentClass = expression.getParent();
                final Boolean    isAssignment          = expressionParentClass instanceof AssignmentExpressionImpl;

                if (isAssignment) {
                    final PsiElement expressionParentVariable = ((AssignmentExpression) expressionParentClass).getVariable();
                    if (expressionParentVariable instanceof Variable) {
                        final Variable       variable              = (Variable) expressionParentVariable;
                        final PhpScopeHolder expressionScopeHolder = PsiTreeUtil.getParentOfType(expression, PhpScopeHolder.class);
                        if (null != expressionScopeHolder) {
                            final PhpEntryPointInstruction scopeEntryPoint    = expressionScopeHolder.getControlFlow().getEntryPoint();
                            SideEffect.Type                variableSideEffect = SideEffect.Type.NONE;

                            final PhpAccessVariableInstruction[] variableInstructions =
                                PhpControlFlowUtil.getFollowingVariableAccessInstructions(scopeEntryPoint, variable.getName(), false);

                            for (final PhpAccessVariableInstruction variableInstruction : variableInstructions) {
                                if (variableInstruction.getAccess().equals(PhpAccessInstruction.Access.READ_ACCESS)) {
                                    final Variable   anchor       = (Variable) variableInstruction.getAnchor();
                                    final PsiElement anchorParent = anchor.getParent();

                                    if (anchorParent instanceof MethodReference) {
                                        if (!anchorParent.getParent().getClass().equals(StatementImpl.class)) {
                                            variableSideEffect = SideEffect.Type.EXTERNAL;
                                            break;
                                        }

                                        final MethodReference anchorMethodReference  = (MethodReference) anchorParent;
                                        final Method          anchorMethod           = (Method) anchorMethodReference.resolve();
                                        final SideEffect.Type anchorMethodSideEffect = identifySideEffect(anchorMethod);

                                        if (SideEffect.isPrecedenceHigherThan(anchorMethodSideEffect, variableSideEffect)) {
                                            variableSideEffect = anchorMethodSideEffect;

                                            if (SideEffect.isPrecedenceIsMax(variableSideEffect)) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            if (variableSideEffect.equals(SideEffect.Type.NONE)) {
                                registerSideEffectProblem(expressionParentClass);
                            }

                            return;
                        }
                    }
                }

                if (!expressionParentClass.getClass().equals(StatementImpl.class)) {
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
                final SideEffect.Type classConstructorSideEffect = identifySideEffect(classConstructor);

                if (classConstructorSideEffect.equals(SideEffect.Type.NONE) ||
                    classConstructorSideEffect.equals(SideEffect.Type.UNKNOW)) {
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
                    final SideEffect.Type functionSideEffect = getIdentifiedSideEffect(functionReference);

                    if (functionSideEffect.equals(SideEffect.Type.NONE)) {
                        registerSideEffectProblem(functionReference);
                    }
                    else if (functionSideEffect.equals(SideEffect.Type.POSSIBLE)) {
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

    private static class SideEffect {
        final static Integer MAX_PRECEDENCE = 4;

        final private static HashMap<Type, Integer> typePrecedence = new HashMap<>();

        enum Type {NONE, POSSIBLE, UNKNOW, INTERNAL, EXTERNAL}

        static {
            typePrecedence.put(Type.NONE, MAX_PRECEDENCE - 4);
            typePrecedence.put(Type.POSSIBLE, MAX_PRECEDENCE - 3);
            typePrecedence.put(Type.INTERNAL, MAX_PRECEDENCE - 2);
            typePrecedence.put(Type.EXTERNAL, MAX_PRECEDENCE - 1);
            typePrecedence.put(Type.UNKNOW, MAX_PRECEDENCE);
        }

        static Integer getPrecedence(final Type type) {
            return typePrecedence.get(type);
        }

        static Boolean isPrecedenceHigherThan(final Type newType, final Type currentType) {
            return getPrecedence(newType) > getPrecedence(currentType);
        }

        static Boolean isPrecedenceIsMax(final Type type) {
            return getPrecedence(type).equals(MAX_PRECEDENCE);
        }
    }
}
