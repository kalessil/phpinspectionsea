package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

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
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Funivan <alotofall@gmail.com>
 * (c) Artem Khvastunov <contact@artspb.me>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class MultipleReturnStatementsInspector extends BasePhpInspection {
    private static final String message = "Method has multiple return points, try to introduce just one to uncover complexity behind.";

    @NotNull
    public String getShortName() {
        return "MultipleReturnStatementsInspection";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                final PhpClass clazz            = method.getContainingClass();
                final PsiElement nameIdentifier = NamedElementUtil.getNameIdentifier(method);
                if (null != nameIdentifier && null != clazz && !clazz.isTrait()) {
                    final PhpControlFlow controlFlow        = method.getControlFlow();
                    final PhpExitPointInstruction exitPoint = controlFlow.getExitPoint();

                    int returnsCount = 0;
                    for (final PhpInstruction instruction : exitPoint.getPredecessors()) {
                        if (instruction instanceof PhpReturnInstruction && ++returnsCount > 3) {
                            holder.registerProblem(nameIdentifier, message, ProblemHighlightType.GENERIC_ERROR);
                            return;
                        }
                    }

                    if (returnsCount > 1) {
                        holder.registerProblem(nameIdentifier, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
        };
    }

}
