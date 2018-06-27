package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.PhpGoto;
import com.jetbrains.php.lang.psi.elements.PhpGotoLabel;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnusedGotoLabelInspector extends BasePhpInspection {
    private static final String message = "The label is not used.";

    @NotNull
    public String getShortName() {
        return "UnusedGotoLabelInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpGotoLabel(@NotNull PhpGotoLabel label) {
                final Function function   = ExpressionSemanticUtil.getScope(label);
                final GroupStatement body = null == function ? null : ExpressionSemanticUtil.getGroupStatement(function);
                if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) > 0) {
                    /* process goto statements and drop used from existing */
                    final Collection<PhpGoto> references = PsiTreeUtil.findChildrenOfType(body, PhpGoto.class);
                    if (!references.isEmpty()) {
                        final String labelName = label.getName();
                        for (final PhpGoto gotoExpression : references) {
                            final String labelUsed = gotoExpression.getName();
                            if (null != labelUsed && labelUsed.equals(labelName)) {
                                references.clear();
                                return;
                            }
                        }
                        references.clear();
                    }

                    /* TODO: marks as unused instead, see https://youtrack.jetbrains.com/issue/WI-34508 */
                    holder.registerProblem(label, message, ProblemHighlightType.LIKE_DEPRECATED, new TheLocalFix());
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Remove unused label";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
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
