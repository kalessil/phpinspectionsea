package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ForeachStatement;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.strategy.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class MissUsingForeachInspector extends PhpInspection {
    private static final String messagePattern = "Perhaps can be replaced with '%s(...)' call (reduces cognitive load).";

    @NotNull
    @Override
    public String getShortName() {
        return "MissUsingForeachInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Missused foreach constructs";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement foreach) {
                if (this.shouldSkipAnalysis(foreach, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(foreach);
                if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) == 1) {
                    final PsiElement expression = ExpressionSemanticUtil.getLastStatement(body);
                    if (expression != null) {
                        if (CanBeReplacedWithImplodeStrategy.apply(foreach, expression, holder.getProject())) {
                            holder.registerProblem(
                                    foreach.getFirstChild(),
                                    MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, "implode"))
                            );
                        } else if (CanBeReplacedWithArrayProductStrategy.apply(foreach, expression, holder.getProject())) {
                            holder.registerProblem(
                                    foreach.getFirstChild(),
                                    MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, "array_product"))
                            );
                        } else if (CanBeReplacedWithArraySumStrategy.apply(foreach, expression, holder.getProject())) {
                            holder.registerProblem(
                                    foreach.getFirstChild(),
                                    MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, "array_sum"))
                            );
                        } else if (CanBeReplacedWithArrayFlipStrategy.apply(foreach, expression, holder.getProject())) {
                            holder.registerProblem(
                                    foreach.getFirstChild(),
                                    MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, "array_flip"))
                            );
                        } else if (CanBeReplacedWithArrayMapStrategy.apply(foreach, expression, holder.getProject())) {
                            holder.registerProblem(
                                    foreach.getFirstChild(),
                                    MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, "array_map"))
                            );
                        } else if (CanBeReplacedWithArrayColumnStrategy.apply(foreach, expression, holder.getProject())) {
                            holder.registerProblem(
                                    foreach.getFirstChild(),
                                    MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, "array_column"))
                            );
                        } else if (CanBeReplacedWithArrayFilterStrategy.apply(foreach, expression)) {
                            holder.registerProblem(
                                    foreach.getFirstChild(),
                                    MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, "array_filter"))
                            );
                        } else if (CanBeReplacedWithArraySearchStrategy.apply(foreach, expression, holder.getProject())) {
                            holder.registerProblem(
                                    foreach.getFirstChild(),
                                    MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, "array_search"))
                            );
                        } else if (CanBeReplacedWithInArrayStrategy.apply(foreach, expression, holder.getProject())) {
                            holder.registerProblem(
                                    foreach.getFirstChild(),
                                    MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, "in_array"))
                            );
                        }
                    }
                }
            }
        };
    }
}
