package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class FopenBinaryUnsafeUsageInspector extends BasePhpInspection {
    private static final String messageMisplacedBinaryMode   = "The 'b' modifier needs to be the last one (e.g 'wb', 'wb+').";
    private static final String messageUseBinaryMode         = "The mode is not binary-safe ('b' is missing).";
    private static final String messageReplaceWithBinaryMode = "The mode is not binary-safe (replace 't' with 'b').";

    @NotNull
    public String getShortName() {
        return "FopenBinaryUnsafeUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName == null || !functionName.equals("fopen")) {
                    return;
                }
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length < 2) {
                    return;
                }

                /* verify if mode provided and has no 'b' already */
                final StringLiteralExpression mode = ExpressionSemanticUtil.resolveAsStringLiteral(arguments[1]);
                final String modeText              = mode == null ? null : mode.getContents();
                if (!StringUtils.isEmpty(modeText)) {
                    if (modeText.indexOf('b') != -1) {
                        final boolean isCorrectlyPlaced = modeText.endsWith("b") || modeText.endsWith("b+");
                        if (!isCorrectlyPlaced) {
                            holder.registerProblem(
                                arguments[1],
                                messageMisplacedBinaryMode,
                                ProblemHighlightType.GENERIC_ERROR,
                                new TheLocalFix()
                            );
                        }
                    } else if (modeText.indexOf('t') != -1) {
                        holder.registerProblem(arguments[1], messageReplaceWithBinaryMode, new TheLocalFix());
                    } else {
                        holder.registerProblem(arguments[1], messageUseBinaryMode, new TheLocalFix());
                    }
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Make mode binary-safe";

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
            final PsiElement expression        = descriptor.getPsiElement();
            if (expression == null || project.isDisposed()) {
                return;
            }

            final StringLiteralExpression mode = ExpressionSemanticUtil.resolveAsStringLiteral(expression);
            if (null != mode) {
                String modeFlags = mode.getContents();
                if (StringUtils.isEmpty(modeFlags)) {
                    return;
                }

                /* get rid of mis-placed b-flag, to apply QF */
                modeFlags = modeFlags.replace("b", "");

                /* get rid of 't' modifier */
                if (modeFlags.indexOf('t') != -1) {
                    modeFlags = modeFlags.replace('t', 'b');
                }

                /* inject binary flag */
                final boolean hasBinaryFlag = modeFlags.indexOf('b') != -1;
                if (!hasBinaryFlag) {
                    if (modeFlags.indexOf('+') != -1) {
                        modeFlags = modeFlags.replace("+", "b+");
                    } else {
                        modeFlags += "b";
                    }
                }

                final String pattern = "'" + modeFlags + "'";
                mode.replace(PhpPsiElementFactory.createFromText(project, StringLiteralExpression.class, pattern));
            }
        }
    }

}
