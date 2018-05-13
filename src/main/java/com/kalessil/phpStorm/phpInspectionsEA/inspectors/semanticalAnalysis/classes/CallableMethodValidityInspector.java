package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.PhpCallbackFunctionUtil;
import com.jetbrains.php.lang.PhpCallbackReferenceBase;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class CallableMethodValidityInspector extends BasePhpInspection {
    private static final String patternNotPublic    = "'%s' should be public (e.g. $this usage in static context provokes fatal errors).";
    private static final String patternNotStatic    = "'%s' should be static (e.g. $this usage in static context provokes fatal errors).";
    private static final String messageUseThrowable = "\\Throwable instead of \\Exception should be used in the handler (BC break introduced in PHP 7).";

    @NotNull
    public String getShortName() {
        return "CallableMethodValidityInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null) {
                    final boolean isExceptionHandler = functionName.equals("set_exception_handler");
                    if (isExceptionHandler || functionName.equals("is_callable")) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 1) {
                            final Set<PsiElement> values = PossibleValuesDiscoveryUtil.discover(arguments[0]);
                            final PsiElement variant     = values.size() == 1 ? values.iterator().next() : null;
                            if (variant != null) {
                                final PsiElement resolved = this.resolve(variant);
                                if (resolved instanceof Function) {
                                    /* case 1: method accessibility */
                                    this.analyzeValidity((Function) resolved, arguments[0], variant);
                                    /* case 2: exception handler */
                                    if (isExceptionHandler) {
                                        this.analyzeExceptionsHandler((Function) resolved, arguments[0]);
                                    }
                                }
                            }
                            values.clear();
                        }
                    }
                }
            }

            @Nullable
            private PsiElement resolve(@NotNull PsiElement callable) {
                PsiElement result = null;
                if (
                    callable instanceof StringLiteralExpression ||
                    (callable instanceof ArrayCreationExpression && callable.getChildren().length == 2)
                ) {
                    final PhpCallbackFunctionUtil.PhpCallbackInfoHolder callback = PhpCallbackFunctionUtil.createCallback(callable);
                    if (callback != null) {
                        if (callback instanceof PhpCallbackFunctionUtil.PhpMemberCallbackInfoHolder) {
                            final PsiElement classReference = ((PhpCallbackFunctionUtil.PhpMemberCallbackInfoHolder) callback).getClassElement();
                            final PsiReference resolver     = PhpCallbackReferenceBase.createMemberReference(classReference, callback.getCallbackElement(), true);
                            result                          = resolver == null ? null : resolver.resolve();
                        } else {
                            final PsiReference resolver = PhpCallbackReferenceBase.createFunctionReference(callback.getCallbackElement());
                            result                      = resolver == null ? null : resolver.resolve();
                        }
                    }
                } else if (OpenapiTypesUtil.isLambda(callable)) {
                    result = callable instanceof Function ? callable : callable.getFirstChild();
                }
                return result;
            }

            private void analyzeExceptionsHandler(@NotNull Function resolved, @NotNull PsiElement argument) {
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP700) >= 0) {
                    final Parameter[] parameters = resolved.getParameters();
                    if (parameters.length > 0) {
                        final boolean isTarget = parameters[0].getDeclaredType().equals(new PhpType().add("\\Exception"));
                        if (isTarget) {
                            final PsiElement target = OpenapiTypesUtil.isLambda(argument) ? parameters[0] : argument;
                            holder.registerProblem(target, messageUseThrowable);
                        }
                    }
                }
            }

            private void analyzeValidity(@NotNull Function resolved, @NotNull PsiElement target, @NotNull PsiElement callable) {
                if (resolved instanceof Method) {
                    final Method method = (Method) resolved;
                    if (!method.getAccess().isPublic()) {
                        holder.registerProblem(target, String.format(patternNotPublic, method.getName()));
                    }

                    boolean needStatic = false;
                    if (callable instanceof StringLiteralExpression && !method.isStatic()) {
                        needStatic = true;
                    }
                    if (callable instanceof ArrayCreationExpression && !method.isStatic()) {
                        final PsiElement classCandidate = callable.getChildren()[0].getFirstChild();
                        /* try resolving the expression */
                        if (classCandidate instanceof PhpTypedElement) {
                            final PhpTypedElement candidate = (PhpTypedElement) classCandidate;
                            final Project project           = classCandidate.getProject();
                            for (final String type : candidate.getType().global(project).filterUnknown().getTypes()) {
                                final String resolvedType = Types.getType(type);
                                if (resolvedType.equals(Types.strString) || resolvedType.equals(Types.strCallable)) {
                                    needStatic = true;
                                    break;
                                }
                            }
                        }
                        /* older PS compatibility: recognize ::class properly */
                        if (!needStatic && classCandidate instanceof ClassConstantReference) {
                            final String constantName = ((ClassConstantReference) classCandidate).getName();
                            needStatic                = constantName != null && constantName.equals("class");
                        }
                    }
                    if (needStatic) {
                        holder.registerProblem(target, String.format(patternNotStatic, method.getName()));
                    }
                }
            }
        };
    }
}
