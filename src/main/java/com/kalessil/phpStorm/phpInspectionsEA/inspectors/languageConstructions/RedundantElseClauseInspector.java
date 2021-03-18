package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UnnecessaryElseFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Funivan <alotofall@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class RedundantElseClauseInspector extends BasePhpInspection {
    private static final String messageElse   = "Child instructions can be extracted here (clearer intention, lower complexity numbers).";
    private static final String messageElseif = "Can be converted into if-branch (clearer intention, lower complexity numbers).";

    @NotNull
    @Override
    public String getShortName() {
        return "RedundantElseClauseInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Redundant 'else' keyword";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpIf(@NotNull If expression) {
                /* context expectations: not if-else-if-* constructs */
                if (expression.getParent() instanceof Else) {
                    return;
                }
                /* general construct expectations: body wrapped into {} and has alternative branches */
                final GroupStatement ifBody = ExpressionSemanticUtil.getGroupStatement(expression);
                if (ifBody == null || !ExpressionSemanticUtil.hasAlternativeBranches(expression)) {
                    return;
                }
                if (!OpenapiTypesUtil.is(ifBody.getFirstChild(), PhpTokenTypes.chLBRACE)) {
                    return;
                }

                /* collect alternative branches for reporting and QF binding */
                final List<PhpPsiElement> alternativeBranches = new ArrayList<>(Arrays.asList(expression.getElseIfBranches()));
                final Else elseBranch                         = expression.getElseBranch();
                if (elseBranch != null) {
                    alternativeBranches.add(elseBranch);
                }

                /* alternative branch expectations */
                final PhpPsiElement alternative      = alternativeBranches.get(0);
                final GroupStatement alternativeBody = ExpressionSemanticUtil.getGroupStatement(alternative);
                if (alternative instanceof ElseIf && alternativeBody == null) {
                    alternativeBranches.clear();
                    return;
                }
                if (alternative instanceof Else && alternativeBody == null && !(alternative.getFirstPsiChild() instanceof If)) {
                    alternativeBranches.clear();
                    return;
                }

                /* analyze last statement in if and report if matched inspection pattern */
                final PsiElement lastStatement = ExpressionSemanticUtil.getLastStatement(ifBody);
                if (lastStatement != null) {
                    final boolean isExitStatement = lastStatement.getFirstChild() instanceof PhpExit;
                    final boolean isReturnPoint   = isExitStatement ||
                               lastStatement instanceof PhpReturn   ||
                               lastStatement instanceof PhpContinue ||
                               lastStatement instanceof PhpBreak    ||
                               OpenapiTypesUtil.isThrowExpression(lastStatement);

                    if (isReturnPoint) {
                        holder.registerProblem(
                                alternative.getFirstChild(),
                                MessagesPresentationUtil.prefixWithEa(alternative instanceof Else ? messageElse : messageElseif),
                                new UnnecessaryElseFixer()
                        );
                    }
                }
                alternativeBranches.clear();
            }
        };
    }
}