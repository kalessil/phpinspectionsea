package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
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

public class IsNullFunctionUsageInspector extends BasePhpInspection {
    // Inspection options.
    public boolean PREFER_YODA_STYLE    = true;
    public boolean PREFER_REGULAR_STYLE = false;

    private static final String messagePattern = "'%e%' construction should be used instead.";

    @NotNull
    public String getShortName() {
        return "IsNullFunctionUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                /* check parameters amount and name */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (1 != params.length || null == functionName || !functionName.equals("is_null")) {
                    return;
                }

                final PsiElement parent = reference.getParent();

                /* check the context */
                boolean checksIsNull = true;
                PsiElement target    = reference;
                if (parent instanceof UnaryExpression) {
                    final PsiElement operation = ((UnaryExpression) parent).getOperation();
                    if (null != operation && PhpTokenTypes.opNOT == operation.getNode().getElementType()) {
                        checksIsNull = false;
                        target       = parent;
                    }
                } else if (parent instanceof BinaryExpression) {
                    /* extract isnulls' expression parts */
                    final BinaryExpression expression = (BinaryExpression) parent;
                    PsiElement secondOperand          = expression.getLeftOperand();
                    if (reference == secondOperand) {
                        secondOperand = expression.getRightOperand();
                    }

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
                final boolean wrapArgument = PREFER_REGULAR_STYLE && params[0] instanceof AssignmentExpression;
                final String replacement   = (PREFER_YODA_STYLE ? "null %o% %a%" : "%a% %o% null")
                        .replace("%o%", checksIsNull ? "===" : "!==")
                        .replace("%a%", wrapArgument ? "(%a%)" : "%a%")
                        .replace("%a%", params[0].getText());
                final String message       = messagePattern.replace("%e%", replacement);
                holder.registerProblem(target, message, new CompareToNullFix(replacement));
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> component.delegateRadioCreation((radioComponent) -> {
            radioComponent.addOption("Regular fix style", PREFER_REGULAR_STYLE, (isSelected) -> PREFER_REGULAR_STYLE = isSelected);
            radioComponent.addOption("Yoda fix style",    PREFER_YODA_STYLE,    (isSelected) -> PREFER_YODA_STYLE = isSelected);
        }));
    }

    private class CompareToNullFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use null comparison instead";
        }

        CompareToNullFix(@NotNull String expression) {
            super(expression);
        }
    }
}