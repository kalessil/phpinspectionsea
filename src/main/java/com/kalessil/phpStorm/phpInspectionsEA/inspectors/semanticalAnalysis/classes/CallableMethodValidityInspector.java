package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.PhpCallbackFunctionUtil;
import com.jetbrains.php.lang.PhpCallbackReferenceBase;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
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

public class CallableMethodValidityInspector extends BasePhpInspection {
    private static final String messagePattern = "'%m%' should be public and static (e.g. $this usage in static context provokes fatal errors).";

    @NotNull
    public String getShortName() {
        return "CallableMethodValidityInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (params.length == 1 && functionName != null && functionName.equals("is_callable")) {
                    final Set<PsiElement> processed = new HashSet<>();
                    final Set<PsiElement> values    = PossibleValuesDiscoveryUtil.discover(params[0], processed);
                    processed.clear();

                    final PsiElement callable = values.size() == 1 ? values.iterator().next() : null;
                    if (callable != null && this.isTarget(callable)) {
                        final PsiReference resolver = this.buildResolver(callable);
                        if (resolver != null) {
                            this.analyzeValidity(resolver.resolve(), params[0]);
                        }
                    }
                    values.clear();
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

            private void analyzeValidity(@Nullable PsiElement element, @NotNull PsiElement target) {
                if (element instanceof Method) {
                    final Method method = (Method) element;
                    if (!method.isStatic() || !method.getAccess().isPublic()) {
                        final String message = messagePattern.replace("%m%", method.getName());
                        holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
        };
    }
}
