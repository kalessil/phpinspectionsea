package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateProjectSettings;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class IsNullFunctionUsageInspector extends PhpInspection {
    private static final String messagePattern = "'%s' construction should be used instead.";

    @NotNull
    @Override
    public String getShortName() {
        return "IsNullFunctionUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "!display-name!";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_LANGUAGE_LEVEL_MIGRATION)) { return; }

                final String functionName = reference.getName();
                if (functionName == null || !functionName.equals("is_null")) {
                    return;
                }
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length != 1) {
                    return;
                }

                final PsiElement parent = reference.getParent();

                /* check the context */
                boolean checksIsNull = true;
                PsiElement target    = reference;
                if (parent instanceof UnaryExpression) {
                    if (OpenapiTypesUtil.is(((UnaryExpression) parent).getOperation(), PhpTokenTypes.opNOT)) {
                        checksIsNull = false;
                        target       = parent;
                    }
                } else if (parent instanceof BinaryExpression) {
                    /* extract isnulls' expression parts */
                    final BinaryExpression expression = (BinaryExpression) parent;
                    final PsiElement secondOperand    = OpenapiElementsUtil.getSecondOperand(expression, reference);
                    if (PhpLanguageUtil.isBoolean(secondOperand)) {
                        final IElementType operation = expression.getOperationType();
                        if (PhpTokenTypes.opEQUAL == operation || PhpTokenTypes.opIDENTICAL == operation) {
                            target       = parent;
                            checksIsNull = PhpLanguageUtil.isTrue(secondOperand);
                        } else if (operation == PhpTokenTypes.opNOT_EQUAL || operation == PhpTokenTypes.opNOT_IDENTICAL) {
                            target       = parent;
                            checksIsNull = !PhpLanguageUtil.isTrue(secondOperand);
                        } else {
                            target = reference;
                        }
                    }
                }

                /* report the issue */
                final boolean isRegular      = !holder.getProject().getComponent(EAUltimateProjectSettings.class).isPreferringYodaComparisonStyle();
                final String wrappedArgument = isRegular && arguments[0] instanceof AssignmentExpression
                                               ? String.format("(%s)", arguments[0].getText())
                                               : arguments[0].getText();
                final String checkIsNull     = checksIsNull ? "===" : "!==";
                final String replacement     = isRegular
                                               ? String.format("%s %s null", wrappedArgument, checkIsNull)
                                               : String.format("null %s %s", checkIsNull, wrappedArgument);
                final String message         = String.format(messagePattern, replacement);
                holder.registerProblem(target, message, new CompareToNullFix(replacement));
            }
        };
    }

    private static final class CompareToNullFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use null comparison instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        CompareToNullFix(@NotNull String expression) {
            super(expression);
        }
    }
}