package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class BasenameCallsContextInspector extends BasePhpInspection {
    private static final String messagePattern = "'%e%' can be used instead (reduces amount of calls).";

    @NotNull
    public String getShortName() {
        return "BasenameCallsContextInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(reference))              { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("str_replace")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length >= 3 && arguments[1] instanceof StringLiteralExpression) {
                        final String replacedWith = ((StringLiteralExpression) arguments[1]).getContents();
                        if (replacedWith.isEmpty() && OpenapiTypesUtil.isFunctionReference(arguments[2])) {
                            final FunctionReference candidate = (FunctionReference) arguments[2];
                            final String candidateName        = candidate.getName();
                            if (candidateName != null && candidateName.equals("basename")) {
                                final PsiElement[] candidateArguments = candidate.getParameters();
                                if (candidateArguments.length == 1) {

                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
