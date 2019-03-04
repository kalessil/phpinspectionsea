package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
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

public class StaticLambdaBindingInspector extends BasePhpInspection {
    private static final String message = "'$this' can not be used in static closures.";

    @NotNull
    public String getShortName() {
        return "StaticLambdaBindingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(function))               { return; }

                if (OpenapiTypesUtil.isLambda(function) && OpenapiTypesUtil.is(function.getFirstChild(), PhpTokenTypes.kwSTATIC)) {
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(function);
                    if (body != null) {
                        for (final Variable variable : PsiTreeUtil.findChildrenOfType(body, Variable.class)) {
                            if (variable.getName().equals("this")) {
                                holder.registerProblem(variable, message);
                                return;
                            }
                        }
                    }
                }
            }
        };
    }
}
