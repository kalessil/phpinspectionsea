package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class MissingLoopTerminationInspector extends PhpInspection {
    private static final String message = "It seems the loop termination is missing, please place 'break;' at a proper place.";

    @NotNull
    @Override
    public String getShortName() {
        return "MissingLoopTerminationInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Missing loop termination";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement loop) {
                if (this.shouldSkipAnalysis(loop, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }
                this.analyze(loop);
            }

            @Override
            public void visitPhpFor(@NotNull For loop) {
                if (this.shouldSkipAnalysis(loop, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }
                this.analyze(loop);
            }

            @Override
            public void visitPhpWhile(@NotNull While loop) {
                if (this.shouldSkipAnalysis(loop, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }
                this.analyze(loop);
            }

            @Override
            public void visitPhpDoWhile(@NotNull DoWhile loop) {
                if (this.shouldSkipAnalysis(loop, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }
                this.analyze(loop);
            }

            private void analyze(@NotNull PsiElement loop) {
                final GroupStatement loopBody = ExpressionSemanticUtil.getGroupStatement(loop);
                if (loopBody != null && ExpressionSemanticUtil.countExpressionsInGroup(loopBody) == 1) {
                    final PsiElement ifCandidate = ExpressionSemanticUtil.getLastStatement(loopBody);
                    if (ifCandidate instanceof If) {
                        final GroupStatement ifBody = ExpressionSemanticUtil.getGroupStatement(ifCandidate);
                        if (ifBody != null) {
                            final boolean isTarget = Arrays.stream(ifBody.getChildren())
                                    .filter(statement   -> !(statement instanceof PhpDocType) && !(statement instanceof PhpDocComment))
                                    .allMatch(statement -> {
                                        final PsiElement assignmentCandidate = statement.getFirstChild();
                                        if (OpenapiTypesUtil.isAssignment(assignmentCandidate)) {
                                            final PsiElement value = ((AssignmentExpression) assignmentCandidate).getValue();
                                            if (PhpLanguageUtil.isBoolean(value)) {
                                                return true;
                                            }
                                        }
                                        return false;
                                    });
                            if (isTarget) {
                                problemsHolder.registerProblem(loop.getFirstChild(), message);
                            }
                        }
                    }
                }
            }
        };
    }
}
