package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class FopenBinaryUnsafeUsageInspector extends BasePhpInspection {
    // Inspection options.
    public boolean ENFORCE_BINARY_MODIFIER_USAGE = true;

    private static final String messageMisplacedBinaryMode   = "The 'b' modifier needs to be the last one (e.g 'wb', 'wb+').";
    private static final String messageUseBinaryMode         = "The mode is not binary-safe ('b' is missing, as documentation recommends).";
    private static final String messageReplaceWithBinaryMode = "The mode is not binary-safe (replace 't' with 'b', as documentation recommends).";

    @NotNull
    @Override
    public String getShortName() {
        return "FopenBinaryUnsafeUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Binary-unsafe 'fopen(...)' usage";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("fopen")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length >= 2) {
                        /* verify if mode provided and has no 'b' already */
                        final StringLiteralExpression mode = ExpressionSemanticUtil.resolveAsStringLiteral(arguments[1]);
                        final String modeText              = mode == null ? null : mode.getContents();
                        if (modeText != null && !modeText.isEmpty()) {
                            if (modeText.indexOf('b') != -1) {
                                final boolean isCorrectlyPlaced = modeText.endsWith("b") || modeText.endsWith("b+");
                                if (!isCorrectlyPlaced) {
                                    holder.registerProblem(
                                            arguments[1],
                                            MessagesPresentationUtil.prefixWithEa(messageMisplacedBinaryMode),
                                            ProblemHighlightType.GENERIC_ERROR,
                                            new TheLocalFix(holder.getProject(), mode)
                                    );
                                }
                            } else if (modeText.indexOf('t') != -1) {
                                if (ENFORCE_BINARY_MODIFIER_USAGE) {
                                    holder.registerProblem(
                                            arguments[1],
                                            MessagesPresentationUtil.prefixWithEa(messageReplaceWithBinaryMode),
                                            new TheLocalFix(holder.getProject(), mode)
                                    );
                                }
                            } else {
                                if (ENFORCE_BINARY_MODIFIER_USAGE) {
                                    holder.registerProblem(
                                            arguments[1],
                                            MessagesPresentationUtil.prefixWithEa(messageUseBinaryMode),
                                            new TheLocalFix(holder.getProject(), mode)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addCheckbox("Promote b-modifier (as per documentation)", ENFORCE_BINARY_MODIFIER_USAGE, (isSelected) -> ENFORCE_BINARY_MODIFIER_USAGE = isSelected)
        );
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Make mode binary-safe";

        private final SmartPsiElementPointer<StringLiteralExpression> mode;

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        TheLocalFix(@NotNull Project project, @NotNull StringLiteralExpression mode) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(project);

            this.mode = factory.createSmartPsiElementPointer(mode);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression != null && !project.isDisposed()) {
                final StringLiteralExpression mode = this.mode.getElement();
                if (mode != null) {
                    String modeFlags = mode.getContents();
                    if (!modeFlags.isEmpty()) {
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

                        mode.replace(PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, String.format("'%s'", modeFlags)));
                    }
                }
            }
        }
    }
}
