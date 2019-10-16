package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NonSecureParseStrUsageInspector  extends PhpInspection {
    private static final String message = "Please provide second parameter to not influence globals.";

    @NotNull
    @Override
    public String getShortName() {
        return "NonSecureParseStrUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Insecure 'parse_str(...)' usage (Variable extract Vulnerability)";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_SECURITY)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && (functionName.equals("parse_str") || functionName.equals("mb_parse_str"))) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1) {
                        holder.registerProblem(
                                reference,
                                ReportingUtil.wrapReportedMessage(message),
                                ProblemHighlightType.GENERIC_ERROR
                        );
                    }
                }
                /* TODO: call_user_func, array_walk - dynamic contexts */
            }
        };
    }
}
