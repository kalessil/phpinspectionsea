package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.Try;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class BadExceptionsProcessingInspector extends BasePhpInspection {
    private static final String messagePattern = "Consider moving non-related statements (%c% in total) outside the try-block or refactoring the try-body into a function/method.";

    @NotNull
    public String getShortName() {
        return "BadExceptionsProcessingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpTry(Try tryStatement) {
                final GroupStatement body  = ExpressionSemanticUtil.getGroupStatement(tryStatement);
                final int expressionsCount = null == body ? 0 : ExpressionSemanticUtil.countExpressionsInGroup(body);
                if (expressionsCount > 3) {
                    final String message = messagePattern.replace("%c%", String.valueOf(expressionsCount));
                    holder.registerProblem(tryStatement.getFirstChild(), message, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}
