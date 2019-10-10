package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class MissingLoopTerminationInspector extends PhpInspection {
    private static final String messageBreak  = "It seems the loop termination is missing, please place 'break;' at a proper place.";
    private static final String messageReturn = "It seems the loop termination is missing, please place 'return ...;' at a proper place.";

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
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
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

            private void analyze(@NotNull PhpPsiElement loop) {
                final GroupStatement loopBody = ExpressionSemanticUtil.getGroupStatement(loop);
                if (loopBody != null && ExpressionSemanticUtil.countExpressionsInGroup(loopBody) == 1) {
                    final PsiElement ifCandidate = ExpressionSemanticUtil.getLastStatement(loopBody);
                    if (ifCandidate instanceof If) {
                        final GroupStatement ifBody = ExpressionSemanticUtil.getGroupStatement(ifCandidate);
                        if (ifBody != null) {
                            final List<PsiElement> containers = new ArrayList<>();
                            final boolean isTarget            = Arrays.stream(ifBody.getChildren())
                                    .filter(statement   -> !(statement instanceof PhpDocType) && !(statement instanceof PhpDocComment))
                                    .allMatch(statement -> {
                                        final PsiElement assignmentCandidate = statement.getFirstChild();
                                        if (OpenapiTypesUtil.isAssignment(assignmentCandidate)) {
                                            final AssignmentExpression assignment = (AssignmentExpression) assignmentCandidate;
                                            final PsiElement variable             = assignment.getVariable();
                                            if (variable instanceof Variable && assignment.getValue() instanceof ConstantReference) {
                                                containers.add(variable);
                                                return true;
                                            }
                                        }
                                        return false;
                                    });
                            if (isTarget) {
                                final PsiElement next = loop.getNextPsiSibling();
                                if (next instanceof PhpReturn) {
                                    final PsiElement value = ExpressionSemanticUtil.getReturnValue((PhpReturn) next);
                                    if (value != null && containers.stream().anyMatch(v -> OpenapiEquivalenceUtil.areEqual(v, value))) {
                                        holder.registerProblem(
                                                loop.getFirstChild(),
                                                messageReturn,
                                                new AddReturnFix(holder.getProject(), ExpressionSemanticUtil.getLastStatement(ifBody), next)
                                        );
                                    }
                                }

                                /* default reporting */
                                holder.registerProblem(
                                        loop.getFirstChild(),
                                        messageBreak,
                                        new AddBreakFix(holder.getProject(), ExpressionSemanticUtil.getLastStatement(ifBody))
                                );
                            }
                            containers.clear();
                        }
                    }
                }
            }
        };
    }

    private static final class AddBreakFix implements LocalQuickFix {
        private static final String title = "Add missing 'break;'";
        private final SmartPsiElementPointer<PsiElement> after;

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        AddBreakFix(@NotNull Project project, @NotNull PsiElement after) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(project);
            this.after                        = factory.createSmartPsiElementPointer(after);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement after = this.after.getElement();
            if (after != null && !project.isDisposed()) {
                after.getParent().addAfter(PhpPsiElementFactory.createFromText(project, PhpBreak.class, "break;"), after);
            }
        }
    }

    private static final class AddReturnFix implements LocalQuickFix {
        private static final String title = "Add missing 'return ...;'";
        private final SmartPsiElementPointer<PsiElement> after;
        private final SmartPsiElementPointer<PsiElement> what;

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        AddReturnFix(@NotNull Project project, @NotNull PsiElement after, @NotNull PsiElement what) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(project);
            this.after                        = factory.createSmartPsiElementPointer(after);
            this.what                         = factory.createSmartPsiElementPointer(what);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement after = this.after.getElement();
            final PsiElement what  = this.what.getElement();
            if (after != null && what != null && !project.isDisposed()) {
                after.getParent().addAfter(what.copy(), after);
            }
        }
    }
}
