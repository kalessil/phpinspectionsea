package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class DynamicInvocationViaScopeResolutionInspector extends BasePhpInspection {
    private static final String patternScopeResolutionUsed = "'$this->%s(...)' should be used instead.";
    private static final String patternExpressionUsed      = "'...->%s(...)' should be used instead.";

    @NotNull
    @Override
    public String getShortName() {
        return "DynamicInvocationViaScopeResolutionInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Dynamic methods invocation via '::'";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                final PsiReference classReference = reference.getReference();
                final String methodName           = reference.getName();
                if (classReference != null && methodName != null && ! methodName.isEmpty()) {
                    final PsiElement operator = OpenapiPsiSearchUtil.findResolutionOperator(reference);
                    if (OpenapiTypesUtil.is(operator, PhpTokenTypes.SCOPE_RESOLUTION)) {
                        final PsiElement resoled = OpenapiResolveUtil.resolveReference(classReference);
                        if (resoled instanceof Method) {
                            final Method resolvedMethod  = (Method) resoled;
                            final PhpClass resolvedClass = resolvedMethod.getContainingClass();
                            if (resolvedClass != null && !resolvedClass.isInterface() && !resolvedMethod.isStatic() && !resolvedMethod.isAbstract()) {
                                /* check first pattern [static|self|Clazz]::dynamic */
                                final PsiElement staticCandidate = reference.getFirstChild();
                                final String candidateContent    = staticCandidate.getText();
                                if (candidateContent.equals("static") || candidateContent.equals("self") || candidateContent.equals(resolvedClass.getName())) {
                                    final Function scope = ExpressionSemanticUtil.getScope(reference);
                                    if (scope instanceof Method && ! methodName.equalsIgnoreCase(scope.getName())) {
                                        final Method currentMethod  = (Method) scope;
                                        final PhpClass currentClass = currentMethod.getContainingClass();
                                        if (currentClass == null || OpenapiResolveUtil.resolveMethod(currentClass, methodName) != null) {
                                            if (currentMethod.isStatic()) {
                                                holder.registerProblem(
                                                        reference,
                                                        String.format(MessagesPresentationUtil.prefixWithEa(patternExpressionUsed), reference.getName())
                                                );
                                            } else {
                                                holder.registerProblem(
                                                        reference,
                                                        String.format(MessagesPresentationUtil.prefixWithEa(patternScopeResolutionUsed), methodName),
                                                        new TheLocalFix(holder.getProject(), operator, staticCandidate)
                                                );
                                            }
                                        }
                                    }
                                } else {
                                    /* check second pattern <expression>::dynamic */
                                    final PsiElement base = reference.getFirstPsiChild();
                                    if (base != null && ! (base instanceof FunctionReference) && ! (staticCandidate instanceof ClassReference)) {
                                        holder.registerProblem(
                                                reference,
                                                String.format(MessagesPresentationUtil.prefixWithEa(patternExpressionUsed), reference.getName()),
                                                new TheLocalFix(holder.getProject(), operator, null)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use -> instead";

        private final SmartPsiElementPointer<PsiElement> base;
        private final SmartPsiElementPointer<PsiElement> operator;

        TheLocalFix(@NotNull Project project, @NotNull PsiElement operator, @Nullable PsiElement base) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(project);

            this.base     = base == null ? null : factory.createSmartPsiElementPointer(base);
            this.operator = factory.createSmartPsiElementPointer(operator);
        }

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement operator = this.operator.getElement();
            if (operator != null && !project.isDisposed()) {
                operator.replace(PhpPsiElementFactory.createArrow(project));

                final PsiElement base = this.base == null ? null : this.base.getElement();
                if (base != null) {
                    base.replace(PhpPsiElementFactory.createVariable(project, "this", true));
                }
            }
        }
    }
}