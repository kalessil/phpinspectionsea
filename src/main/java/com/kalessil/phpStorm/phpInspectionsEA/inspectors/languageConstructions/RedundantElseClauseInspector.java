package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UnnecessaryElseFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
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
    private static final String messagePattern = "'%kw%' is not needed here (because of the last statement in if-branch).";

    @NotNull
    public String getShortName() {
        return "RedundantElseClauseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpIf(If ifStatement) {
                /* context expectations: not if-else-if-* constructs */
                if (ifStatement.getParent() instanceof Else) {
                    return;
                }
                /* general construct expectations: body wrapped into {} and has alternative branches */
                final GroupStatement ifBody = ExpressionSemanticUtil.getGroupStatement(ifStatement);
                if (null == ifBody || !ExpressionSemanticUtil.hasAlternativeBranches(ifStatement)) {
                    return;
                }
                if (PhpTokenTypes.chLBRACE != ifBody.getFirstChild().getNode().getElementType()) {
                    return;
                }

                /* collect alternative branches for reporting and QF binding */
                final List<PhpPsiElement> alternativeBranches = new ArrayList<>();
                final Else elseBranch                         = ifStatement.getElseBranch();
                alternativeBranches.addAll(Arrays.asList(ifStatement.getElseIfBranches()));
                if (null != elseBranch) {
                    alternativeBranches.add(elseBranch);
                }

                /* alternative branch expectations */
                final PhpPsiElement alternative      = alternativeBranches.get(0);
                final GroupStatement alternativeBody = ExpressionSemanticUtil.getGroupStatement(alternative);
                if (alternative instanceof ElseIf && null == alternativeBody) {
                    alternativeBranches.clear();
                    return;
                }
                if (alternative instanceof Else && null == alternativeBody && !(alternative.getFirstPsiChild() instanceof If)) {
                    alternativeBranches.clear();
                    return;
                }

                /* analyze last statement in if and report if matched inspection pattern */
                final PsiElement lastStatement = ExpressionSemanticUtil.getLastStatement(ifBody);
                if (null != lastStatement) {
                    final boolean isExitStatement = lastStatement.getFirstChild() instanceof PhpExit;
                    final boolean isReturnPoint   = isExitStatement ||
                               lastStatement instanceof PhpReturn   || lastStatement instanceof PhpThrow ||
                               lastStatement instanceof PhpContinue || lastStatement instanceof PhpBreak;

                    if (isReturnPoint) {
                        final PsiElement target = alternative.getFirstChild();
                        final String message    = messagePattern.replace("%kw%", target.getText());
                        holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new UnnecessaryElseFixer());
                    }
                }
                alternativeBranches.clear();
            }
        };
    }
}