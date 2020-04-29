package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpExitPointInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpReturnInstruction;
import com.jetbrains.php.lang.psi.elements.Method;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

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
    // Inspection options.
    public int COMPLAIN_THRESHOLD = 3;
    public int SCREAM_THRESHOLD   = 5;

    private static final String messagePattern = "Method has %s return points, try to introduce just one to uncover complexity behind.";

    @NotNull
    @Override
    public String getShortName() {
        return "MultipleReturnStatementsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Multiple return statements usage";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                final PsiElement nameIdentifier = NamedElementUtil.getNameIdentifier(method);
                if (nameIdentifier != null && !method.isAbstract()) {
                    final PhpExitPointInstruction exitPoint = method.getControlFlow().getExitPoint();

                    int returnsCount = 0;
                    for (final PhpInstruction instruction : OpenapiElementsUtil.getPredecessors(exitPoint)) {
                        if (instruction instanceof PhpReturnInstruction) {
                            ++returnsCount;
                        }
                    }

                    if (returnsCount >= SCREAM_THRESHOLD) {
                        holder.registerProblem(
                                nameIdentifier,
                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), returnsCount),
                                ProblemHighlightType.GENERIC_ERROR
                        );
                    } else if (returnsCount >= COMPLAIN_THRESHOLD) {
                        holder.registerProblem(
                                nameIdentifier,
                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), returnsCount),
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                        );
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addSpinner("Complain threshold:", COMPLAIN_THRESHOLD, (input) -> COMPLAIN_THRESHOLD = input);
            component.addSpinner("Scream threshold:", SCREAM_THRESHOLD, (input) -> SCREAM_THRESHOLD = input);
        });
    }
}
