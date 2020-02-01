package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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

public class MissingHashElementArrowInspector extends PhpInspection {
    private static final String message = "It's probably was intended to use ' => ' here (tweak formatting if not).";

    @NotNull
    @Override
    public String getShortName() {
        return "MissingHashElementArrowInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Missing hash element arrow";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpArrayCreationExpression(@NotNull ArrayCreationExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final ArrayHashElement element = expression.getHashElements().iterator().next();
                if (element != null && element.getKey() instanceof StringLiteralExpression) {
                    final PsiElement[] children = expression.getChildren();
                    for (int position = 0; position < children.length; ++position) {
                        final PsiElement current = children[position];
                        if (! (current instanceof ArrayHashElement) && current.getFirstChild() instanceof StringLiteralExpression) {
                            final PsiElement comma = current.getNextSibling();
                            if (OpenapiTypesUtil.is(comma, PhpTokenTypes.opCOMMA)) {
                                final PsiElement space = comma.getNextSibling();
                                if (space instanceof PsiWhiteSpace && space.getText().equals(" ")) {
                                    holder.registerProblem(
                                            comma,
                                            ReportingUtil.wrapReportedMessage(message)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
