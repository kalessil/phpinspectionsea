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
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlow;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpExitPointInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpReturnInstruction;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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
                final PsiElement nameIdentifier = method.getNameIdentifier();
                if (containingClass != null && nameIdentifier != null && !containingClass.isTrait()) {

                    final PhpControlFlow controlFlow = method.getControlFlow();
                    final PhpExitPointInstruction exitPoint = controlFlow.getExitPoint();
                    int count = 0;
                    for (final PhpInstruction instruction : exitPoint.getPredecessors()) {
                        if (instruction instanceof PhpReturnInstruction) {
                            count++;
                        }
                        if (count > 1) {
                            holder.registerProblem(nameIdentifier, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            return;
                        }
                    }

                }
            }

        };
    }

}
