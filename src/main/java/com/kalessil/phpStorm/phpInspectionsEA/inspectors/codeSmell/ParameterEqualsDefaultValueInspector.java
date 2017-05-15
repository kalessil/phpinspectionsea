package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;

/**
 * This file is part of the Php Inspections (EA Extended) package.
 *
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
                    problemsHolder.registerProblem(problemsHolder.getManager().createProblemDescriptor(
                        referenceParameterLower,
                        referenceParameters[referenceParameters.length - 1],
                        message,
                        ProblemHighlightType.WEAK_WARNING,
                        onTheFly
                    ));
                }
            }
        };
    }
}
