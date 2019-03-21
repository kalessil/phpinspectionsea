package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
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

public class StaticClosureCanBeUsedInspector extends BasePhpInspection {
    private static final String message = "This closure can be declared as static (a micro-optimization).";

    @NotNull
    public String getShortName() {
        return "StaticClosureCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (this.isContainingFileSkipped(function)) { return; }

                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP540) >= 0 && OpenapiTypesUtil.isLambda(function)) {
                    final boolean isTarget = !OpenapiTypesUtil.is(function.getFirstChild(), PhpTokenTypes.kwSTATIC);
                    if (isTarget) {
                        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(function);
                        if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) > 0) {
                            final boolean usesThis = PsiTreeUtil.findChildrenOfType(body, Variable.class).stream()
                                    .anyMatch(variable -> variable.getName().equals("this"));
                            if (!usesThis) {
                                holder.registerProblem(function.getFirstChild(), message, new MakeClosureStaticFix());
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class MakeClosureStaticFix implements LocalQuickFix {
        private static final String title = "Declare the closure static";

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

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement functionKeyword = descriptor.getPsiElement();
            if (functionKeyword != null && !project.isDisposed()) {
                final PsiElement implant = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "static");
                if (implant != null) {
                    functionKeyword.getParent().addBefore(implant, functionKeyword);
                }
            }
        }
    }
}
