package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
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
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (this.shouldSkipAnalysis(function, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                if (OpenapiTypesUtil.isLambda(function) && OpenapiTypesUtil.is(function.getFirstChild(), PhpTokenTypes.kwSTATIC)) {
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(function);
                    if (body != null) {
                        for (final Variable variable : PsiTreeUtil.findChildrenOfType(body, Variable.class)) {
                            if (variable.getName().equals("this")) {
                                holder.registerProblem(variable, message, new HardenConditionFix(function.getFirstChild()));
                                return;
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class HardenConditionFix implements LocalQuickFix {
        private static final String title = "Make the closure non-static";

        private final SmartPsiElementPointer<PsiElement> staticKeyword;

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

        HardenConditionFix(@NotNull PsiElement staticKeyword) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(staticKeyword.getProject());
            this.staticKeyword                = factory.createSmartPsiElementPointer(staticKeyword);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null && !project.isDisposed()) {
                final PsiElement staticKeyword = this.staticKeyword.getElement();
                if (staticKeyword != null) {
                    staticKeyword.delete();
                }
            }
        }
    }
}
