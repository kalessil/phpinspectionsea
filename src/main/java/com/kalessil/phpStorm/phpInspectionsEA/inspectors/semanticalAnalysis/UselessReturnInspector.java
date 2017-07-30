package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
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

public class UselessReturnInspector extends BasePhpInspection {
    private static final String messageSenseless = "Senseless statement: return null implicitly or safely remove it.";
    private static final String messageConfusing = "Confusing statement: consider re-factoring.";

    @NotNull
    public String getShortName() {
        return "UselessReturnInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpReturn(@NotNull PhpReturn returnStatement) {
                final PhpExpression returnValue = ExpressionSemanticUtil.getReturnValue(returnStatement);
                if (returnValue instanceof AssignmentExpression) {
                    final AssignmentExpression assignment = (AssignmentExpression) returnValue;
                    if (assignment.getVariable() instanceof Variable) {
                        /* TODO: not static, not param/use variable  */
                        holder.registerProblem(returnStatement, messageConfusing);
                    }
                }
            }

            @Override
            public void visitPhpMethod(@NotNull Method method) {
                this.inspectForSenselessReturn(method);
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                this.inspectForSenselessReturn(function);
            }

            private void inspectForSenselessReturn(@NotNull Function callable) {
                final GroupStatement body      = ExpressionSemanticUtil.getGroupStatement(callable);
                final PsiElement lastStatement = body == null ? null : ExpressionSemanticUtil.getLastStatement(body);
                if (lastStatement instanceof PhpReturn) {
                    final PhpExpression returnValue = ExpressionSemanticUtil.getReturnValue((PhpReturn) lastStatement);
                    if (null == returnValue) {
                        holder.registerProblem(lastStatement, messageSenseless, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                    }
                }
            }
        };
    }
}

