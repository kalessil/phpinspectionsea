package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.Field;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class DateTimeConstantsUsageInspector extends BasePhpInspection {
    private static final String message = "The format is not compatible with ISO-8601. Use DateTime::ATOM/DATE_ATOM for compatibility with ISO-8601 instead.";

    @NotNull
    public String getShortName() {
        return "DateTimeConstantsUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClassConstantReference(ClassConstantReference constantReference) {
                final String constantName = constantReference.getName();
                if (StringUtil.isEmpty(constantName) || !constantName.equals("ISO8601")) {
                    return;
                }

                final PsiElement resolved = constantReference.resolve();
                if (resolved instanceof Field) {
                    final Field constant = (Field) resolved;
                    if (constant.isConstant() && constant.getFQN().equals("\\DateTime.ISO8601")) {
                        holder.registerProblem(constantReference, message, ProblemHighlightType.GENERIC_ERROR, new TheLocalFix());
                    }
                }
            }

            public void visitPhpConstantReference(ConstantReference reference) {
                final String constantName = reference.getName();
                if (!StringUtil.isEmpty(constantName) && constantName.equals("DATE_ISO8601")) {
                    holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR, new TheLocalFix());
                }
            }
        };
    }

    static private class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Use suggested replacement";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement target = descriptor.getPsiElement();
            if (target instanceof ConstantReference) {
                ((ConstantReference) target).handleElementRename("DATE_ATOM");
                return;
            }

            if (target instanceof ClassConstantReference){
                ((ClassConstantReference) target).handleElementRename("ATOM");
            }
        }
    }
}
