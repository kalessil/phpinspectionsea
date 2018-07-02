package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpExitPointInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpReturnInstruction;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
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
    private static final String messagePattern = "Method has %s return points, try to introduce just one to uncover complexity behind.";

    @NotNull
    public String getShortName() {
        return "MultipleReturnStatementsInspection";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                final PhpClass clazz            = method.getContainingClass();
                final PsiElement nameIdentifier = NamedElementUtil.getNameIdentifier(method);
                if (nameIdentifier != null && clazz != null && !clazz.isTrait()) {
                    final PhpExitPointInstruction exitPoint = method.getControlFlow().getExitPoint();

                    int returnsCount = 0;
                    for (final PhpInstruction instruction : OpenapiElementsUtil.getPredecessors(exitPoint)) {
                        if (instruction instanceof PhpReturnInstruction) {
                            ++returnsCount;
                        }
                    }

                    if (returnsCount > 1) {
                        final ProblemHighlightType level
                            = returnsCount > 3 ? ProblemHighlightType.GENERIC_ERROR : ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
                        final String message = String.format(messagePattern, returnsCount);
                        holder.registerProblem(nameIdentifier, message, level);
                    }
                }
            }
        };
    }

}
