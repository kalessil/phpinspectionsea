package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;

/**
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ParameterEqualsDefaultValueInspector extends BasePhpInspection {
    private static final String message = "This parameter could be dropped, because the value is the same from default value.";

    @NotNull
    public final String getShortName() {
        return "ParameterEqualsDefaultValueInspection";
    }

    @NotNull
    @Override
    public final PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean onTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(final FunctionReference reference) {
                final PsiElement[] referenceParameters = reference.getParameters();

                if (referenceParameters.length == 0) {
                    return;
                }

                final Function function = (Function) reference.resolve();

                if (function == null) {
                    return;
                }

                final Parameter[] functionParameters = function.getParameters();

                if (functionParameters.length == 0) {
                    return;
                }

                PsiElement referenceParameterLower  = null;
                final int  referenceParametersLimit = Math.min(referenceParameters.length, functionParameters.length) - 1;

                for (int parameterIndex = referenceParametersLimit; parameterIndex >= 0; parameterIndex--) {
                    final PhpExpression referenceParameter = (PhpExpression) referenceParameters[parameterIndex];
                    final Parameter     functionParameter  = functionParameters[parameterIndex];

                    if (referenceParameter.getType().equals(functionParameter.getType())) {
                        final PsiElement functionParameterDefaultValue = functionParameter.getDefaultValue();

                        if ((functionParameterDefaultValue != null) &&
                            referenceParameter.getText().equals(functionParameterDefaultValue.getText())) {
                            referenceParameterLower = referenceParameter;
                            continue;
                        }
                    }

                    break;
                }

                if (referenceParameterLower != null) {
                    final PsiElement referenceParameterLowerPrev = ((PhpPsiElement) referenceParameterLower).getPrevPsiSibling();
                    final PsiElement referenceParameterStart = (referenceParameterLowerPrev == null)
                                                               ? referenceParameterLower
                                                               : referenceParameterLowerPrev.getNextSibling();

                    problemsHolder.registerProblem(problemsHolder.getManager().createProblemDescriptor(
                        referenceParameterLower,
                        referenceParameters[referenceParameters.length - 1],
                        message,
                        ProblemHighlightType.WEAK_WARNING,
                        onTheFly,
                        new TheLocalFix(referenceParameterStart, referenceParameters[referenceParameters.length - 1])
                    ));
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private final SmartPsiElementPointer<PsiElement> dropTo;
        private final SmartPsiElementPointer<PsiElement> dropFrom;

        private TheLocalFix(@NotNull final PsiElement dropFromElement, @NotNull final PsiElement dropToElement) {
            dropFrom = SmartPointerManager.getInstance(dropFromElement.getProject()).createSmartPsiElementPointer(dropFromElement);
            dropTo = SmartPointerManager.getInstance(dropToElement.getProject()).createSmartPsiElementPointer(dropToElement);
        }

        @NotNull
        @Override
        public String getName() {
            return "Drop rewritten parameters";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final PsiElement dropFromElement = dropFrom.getElement();
            final PsiElement dropToElement   = dropTo.getElement();

            if ((dropFromElement != null) &&
                (dropToElement != null)) {
                ASTNode       dropFromNode = dropFromElement.getNode();
                final ASTNode dropToNode   = dropToElement.getNode();

                while (dropFromNode != dropToNode) {
                    final ASTNode dropNextNode = dropFromNode.getTreeNext();
                    dropFromNode.getTreeParent().removeChild(dropFromNode);
                    dropFromNode = dropNextNode;
                }

                dropToNode.getTreeParent().removeChild(dropToNode);
            }
        }
    }
}
