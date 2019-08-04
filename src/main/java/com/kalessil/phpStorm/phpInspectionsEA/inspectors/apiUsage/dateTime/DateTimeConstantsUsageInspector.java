package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.dateTime;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.Field;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

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

public class DateTimeConstantsUsageInspector extends PhpInspection {
    private static final String messageClassConstant = "The format is not compatible with ISO-8601. Use DateTime::ATOM for compatibility with ISO-8601 instead.";
    private static final String messageConstant      = "The format is not compatible with ISO-8601. Use DATE_ATOM for compatibility with ISO-8601 instead.";

    private static final Set<String> targetClassConstants = new HashSet<>();
    static {
        targetClassConstants.add("\\DateTime.ISO8601");
        targetClassConstants.add("\\DateTimeInterface.ISO8601");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "DateTimeConstantsUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "DateTime constants usage validity";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpClassConstantReference(@NotNull ClassConstantReference constantReference) {
                if (this.shouldSkipAnalysis(constantReference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String constantName = constantReference.getName();
                if (constantName != null && constantName.equals("ISO8601")) {
                    final PsiElement resolved = OpenapiResolveUtil.resolveReference(constantReference);
                    if (resolved instanceof Field) {
                        final Field constant = (Field) resolved;
                        if (constant.isConstant() && targetClassConstants.contains(constant.getFQN())) {
                            holder.registerProblem(constantReference, messageClassConstant, new TheLocalFix());
                        }
                    }
                }
            }

            @Override
            public void visitPhpConstantReference(@NotNull ConstantReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String constantName = reference.getName();
                if (constantName != null && constantName.equals("DATE_ISO8601")) {
                    holder.registerProblem(reference, messageConstant, new TheLocalFix());
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use ATOM constant instead";

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

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null && !project.isDisposed()) {
                if (target instanceof ConstantReference) {
                    ((ConstantReference) target).handleElementRename("DATE_ATOM");
                } else if (target instanceof ClassConstantReference){
                    ((ClassConstantReference) target).handleElementRename("ATOM");
                }
            }
        }
    }
}
