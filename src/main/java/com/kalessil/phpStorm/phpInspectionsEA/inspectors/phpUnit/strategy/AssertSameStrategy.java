package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class AssertSameStrategy {
    private final static String message = "This check is type-unsafe, consider using assertSame instead.";

    static public boolean apply(
        @NotNull String functionName,
        @NotNull MethodReference reference,
        @NotNull ProblemsHolder holder
    ) {
        final PsiElement[] params = reference.getParameters();
        if (
            params.length > 1 && functionName.equals("assertEquals") &&
            isPrimitiveScalar(params[0]) && isPrimitiveScalar(params[1])
        ) {
            final TheLocalFix fixer = new TheLocalFix(params[0], params[1]);
            holder.registerProblem(reference, message, fixer);

            return true;
        }

        return false;
    }

    static private boolean isPrimitiveScalar(@NotNull PsiElement expression) {
        boolean result = false;
        if (expression instanceof PhpTypedElement) {
            final PhpType resolvedType = ((PhpTypedElement) expression).getType().global(expression.getProject());
            final Set<String> types    = resolvedType.getTypes();
            if (!resolvedType.hasUnknown() && !types.isEmpty()) {
                boolean isPrimitive = true;
                for (final String type : types) {
                    final String normalizedType = Types.getType(type);
                    if (normalizedType.startsWith("\\") || normalizedType.equals(Types.strArray)) {
                        isPrimitive = false;
                        break;
                    }
                }
                result = isPrimitive;
            }
        }
        return result;
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private SmartPsiElementPointer<PsiElement> first;
        final private SmartPsiElementPointer<PsiElement> second;

        TheLocalFix(@NotNull PsiElement first, @NotNull PsiElement second) {
            super();
            SmartPointerManager manager =  SmartPointerManager.getInstance(first.getProject());

            this.first  = manager.createSmartPsiElementPointer(first);
            this.second = manager.createSmartPsiElementPointer(second);
        }

        @NotNull
        @Override
        public String getName() {
            return "Use ::assertSame";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            final PsiElement first      = this.first.getElement();
            final PsiElement second     = this.second.getElement();
            if (first != null && second != null && expression instanceof FunctionReference) {
                final PsiElement[] params      = ((FunctionReference) expression).getParameters();
                final boolean hasCustomMessage = 3 == params.length;

                final String pattern                = hasCustomMessage ? "pattern(null, null, null)" : "pattern(null, null)";
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, pattern);
                final PsiElement[] replaceParams    = replacement.getParameters();
                replaceParams[0].replace(first);
                replaceParams[1].replace(second);
                if (hasCustomMessage) {
                    replaceParams[2].replace(params[2]);
                }

                final FunctionReference call = (FunctionReference) expression;
                //noinspection ConstantConditions I'm really sure NPE will not happen
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename("assertSame");
            }
        }
    }
}
