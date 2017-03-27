package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Funivan <alotofall@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class MultipleReturnPointsInspector extends BasePhpInspection {
    private static final String message = "Method with multiple return points.";


    @NotNull
    public String getShortName() {
        return "MultipleReturnPointsInspection";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpElementVisitor() {

            public void visitPhpMethod(Method method) {
                final PhpClass containingClass = method.getContainingClass();
                if (containingClass != null && !containingClass.isTrait()) {

                    ReturnPointCountVisitor countVisitor = new ReturnPointCountVisitor(method);
                    method.accept(countVisitor);

                    if (countVisitor.getCount() > 1) {
                        PsiElement nameIdentifier = method.getNameIdentifier();
                        if (nameIdentifier != null) {
                            holder.registerProblem(nameIdentifier, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }

                }
            }

        };
    }


    private class ReturnPointCountVisitor extends PsiRecursiveElementWalkingVisitor {
        private final Method method;
        private int count = 0;

        ReturnPointCountVisitor(Method method) {
            this.method = method;
        }

        public void visitElement(PsiElement element) {
            if (element instanceof PhpReturn) {

                Function scope = ExpressionSemanticUtil.getScope(element);
                if (scope != null && scope.equals(this.method)) {
                    count++;
                }
            }

            super.visitElement(element);
        }

        public int getCount() {
            return count;
        }
    }
}
