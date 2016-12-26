package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class UnusedGotoLabelInspector extends BasePhpInspection {
    private static final String message = "The label is not used";

    @NotNull
    public String getShortName() {
        return "UnusedGotoLabelInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpGotoLabel(PhpGotoLabel label) {
                final String labelName    = label.getName();
                final Function function   = ExpressionSemanticUtil.getScope(label);
                final GroupStatement body = null == function ? null : ExpressionSemanticUtil.getGroupStatement(function);
                if (null == body || 0 == ExpressionSemanticUtil.countExpressionsInGroup(body)) {
                    return;
                }

                /* process goto statements and drop used from existing */
                final Collection<PhpGoto> refs = PsiTreeUtil.findChildrenOfType(body, PhpGoto.class);
                if (refs.size() > 0) {
                    for (PhpGoto gotoExpression : refs) {
                        final String labelUsed = gotoExpression.getName();
                        if (null != labelUsed && labelUsed.length() > 0 && labelUsed.equals(labelName)) {
                            refs.clear();
                            return;
                        }
                    }
                    refs.clear();
                }

                /* report unused labels */
                // TODO: marks as unused instead, see https://youtrack.jetbrains.com/issue/WI-34508
                holder.registerProblem(label, message, ProblemHighlightType.LIKE_DEPRECATED, new TheLocalFix());
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Remove unused label";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target instanceof PhpGotoLabel) {
                target.delete();
            }
        }
    }
}
