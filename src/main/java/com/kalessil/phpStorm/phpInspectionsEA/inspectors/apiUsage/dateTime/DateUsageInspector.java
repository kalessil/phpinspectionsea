package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.dateTime;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class DateUsageInspector extends BasePhpInspection {
    private static final String messageDropTime = "'time()' is default valued already, it can safely be removed.";

    @NotNull
    public String getShortName() {
        return "DateUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("date")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 2) {
                        final PsiElement candidate = arguments[1];
                        if (OpenapiTypesUtil.isFunctionReference(candidate)) {
                            final FunctionReference inner = (FunctionReference) candidate;
                            final String innerName        = inner.getName();
                            if (innerName != null && innerName.equals("time") && inner.getParameters().length == 0) {
                                holder.registerProblem(
                                    inner,
                                    messageDropTime,
                                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                    new DropTimeFunctionCallLocalFix(arguments[0], arguments[1])
                                );
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class DropTimeFunctionCallLocalFix implements LocalQuickFix {
        private static final String title = "Drop unnecessary time() call";

        private final SmartPsiElementPointer<PsiElement> from;
        private final SmartPsiElementPointer<PsiElement> to;

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        DropTimeFunctionCallLocalFix(@NotNull PsiElement from, @NotNull PsiElement to) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(from.getProject());

            this.from = factory.createSmartPsiElementPointer(from);
            this.to   = factory.createSmartPsiElementPointer(to);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement from = this.from.getElement();
            final PsiElement to   = this.to.getElement();
            if (from != null && to != null && !project.isDisposed()) {
                from.getParent().deleteChildRange(from.getNextSibling(), to);
            }
        }
    }
}
