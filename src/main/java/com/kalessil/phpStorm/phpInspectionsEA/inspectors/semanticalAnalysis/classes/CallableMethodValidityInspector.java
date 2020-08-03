package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.PhpCallbackFunctionUtil;
import com.jetbrains.php.lang.PhpCallbackReferenceBase;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
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
    private static final String patternNotPublic = "'%m%' should be public (e.g. $this usage in static context provokes fatal errors).";
    private static final String patternNotStatic = "'%m%' should be static (e.g. $this usage in static context provokes fatal errors).";

    @NotNull
    @Override
    public String getShortName() {
        return "CallableMethodValidityInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Callable methods validity";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("is_callable")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1) {
                        final Set<PsiElement> values = PossibleValuesDiscoveryUtil.discover(arguments[0]);
                        final PsiElement callable    = values.size() == 1 ? values.iterator().next() : null;
                        if (callable != null && this.isTarget(callable)) {
                            final PsiReference resolver = this.buildResolver(callable);
                            if (resolver != null) {
                                this.analyzeValidity(resolver.resolve(), arguments[0], callable);
                            }
                        }
                        values.clear();
                    }
                }
            }

            @Nullable
            private PsiReference buildResolver(@NotNull PsiElement callable) {
                PsiReference result = null;
                final PhpCallbackFunctionUtil.PhpCallbackInfoHolder callback = PhpCallbackFunctionUtil.createCallback(callable);
                if (callback instanceof PhpCallbackFunctionUtil.PhpMemberCallbackInfoHolder) {
                    PsiElement classReference = ((PhpCallbackFunctionUtil.PhpMemberCallbackInfoHolder) callback).getClassElement();
                    result = PhpCallbackReferenceBase.createMemberReference(classReference, callback.getCallbackElement(), true);
                }
                return result;
            }

            private boolean isTarget(@NotNull PsiElement callback) {
                boolean result = callback instanceof StringLiteralExpression;
                if (!result && callback instanceof ArrayCreationExpression) {
                    result = callback.getChildren().length == 2;
                }
                return result;
            }

            private void analyzeValidity(
                    @Nullable PsiElement element,
                    @NotNull PsiElement target,
                    @NotNull PsiElement callable
            ) {
                if (element instanceof Method) {
                    final Method method = (Method) element;
                    if (!method.getAccess().isPublic()) {
                        holder.registerProblem(
                                target,
                                MessagesPresentationUtil.prefixWithEa(patternNotPublic.replace("%m%", method.getName()))
                        );
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
                            final Project project           = holder.getProject();
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
                            needStatic                = null != constantName && constantName.equals("class");
                        }
                    }
                    if (needStatic) {
                        holder.registerProblem(
                                target,
                                MessagesPresentationUtil.prefixWithEa(patternNotStatic.replace("%m%", method.getName()))
                        );
                    }
                }
            }
        };
    }
}
