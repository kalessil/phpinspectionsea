package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.PhpEchoStatement;
import com.jetbrains.php.lang.psi.elements.PhpPrintExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ShortEchoTagCanBeUsedInspector extends BasePhpInspection {
    private static final String message = "'<?= ... ?>' could be used instead (but ensure that short_open_tag is enabled).";

    @NotNull
    public String getShortName() {
        return "ShortEchoTagCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpEchoStatement(@NotNull PhpEchoStatement echo) {
                final PsiElement parent     = echo.getParent();
                final PsiElement openingTag = parent.getFirstChild();
                if (OpenapiTypesUtil.is(openingTag, PhpTokenTypes.PHP_OPENING_TAG)) {
                    final PsiElement closingTag = parent.getFirstChild();
                    if (OpenapiTypesUtil.is(closingTag, PhpTokenTypes.PHP_CLOSING_TAG)) {
                        holder.registerProblem(echo.getFirstChild(), message);
                    }
                }
            }

            @Override
            public void visitPhpPrint(@NotNull PhpPrintExpression print) {
                final PsiElement parent = print.getParent();
                if (OpenapiTypesUtil.isStatementImpl(parent)) {
                    PsiElement openingTag = parent.getPrevSibling();
                    if (openingTag instanceof PsiWhiteSpace) {
                        openingTag = openingTag.getPrevSibling();
                    }
                    if (OpenapiTypesUtil.is(openingTag, PhpTokenTypes.PHP_OPENING_TAG)) {
                        PsiElement closingTag = parent.getNextSibling();
                        if (closingTag instanceof PsiWhiteSpace) {
                            closingTag = closingTag.getNextSibling();
                        }
                        if (OpenapiTypesUtil.is(closingTag, PhpTokenTypes.PHP_CLOSING_TAG)) {
                            holder.registerProblem(print.getFirstChild(), message);
                        }
                    }
                }
            }
        };
    }
}
