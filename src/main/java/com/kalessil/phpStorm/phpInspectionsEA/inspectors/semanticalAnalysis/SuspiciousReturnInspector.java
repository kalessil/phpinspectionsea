package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Finally;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiPlatformUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SuspiciousReturnInspector extends BasePhpInspection {
    private static final String message = "Voids all return and throw statements from the try-block (returned values and exceptions are lost)";

    @NotNull
    @Override
    public String getShortName() {
        return "SuspiciousReturnInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Suspicious returns";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpReturn(@NotNull PhpReturn statement) {
                PsiElement context = statement.getParent();
                while (context != null && !(context instanceof Finally) && !(context instanceof Function) && !(context instanceof PsiFile)) {
                    context = context.getParent();
                }
                if (context instanceof Finally) {
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(context.getParent());
                    if (body != null && PsiTreeUtil.findChildOfAnyType(body, PhpReturn.class, OpenapiPlatformUtil.classes.get("PhpThrow")) != null) {
                        holder.registerProblem(
                                statement,
                                MessagesPresentationUtil.prefixWithEa(message)
                        );
                    }
                }
            }
        };
    }
}
