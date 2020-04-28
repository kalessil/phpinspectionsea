package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
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
    private static final String message = "This closure can be declared as static (better scoping; in some cases can improve performance).";

    @NotNull
    @Override
    public String getShortName() {
        return "StaticClosureCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Static closure can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP540) && OpenapiTypesUtil.isLambda(function)) {
                    final boolean isTarget = !OpenapiTypesUtil.is(function.getFirstChild(), PhpTokenTypes.kwSTATIC);
                    if (isTarget && this.canBeStatic(function)) {
                        holder.registerProblem(
                                function.getFirstChild(),
                                ReportingUtil.wrapReportedMessage(message),
                                new MakeClosureStaticFix()
                        );
                    }
                }
            }

            private boolean canBeStatic(@NotNull Function function) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(function);
                if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) > 0) {
                    for (final PsiElement element : PsiTreeUtil.findChildrenOfAnyType(body, Variable.class, MethodReference.class)) {
                        if (element instanceof Variable) {
                            final Variable variable = (Variable) element;
                            if (variable.getName().equals("this")) {
                                return false;
                            }
                        } else {
                            final MethodReference reference = (MethodReference) element;
                            final PsiElement base           = reference.getFirstChild();
                            if (base instanceof ClassReference && base.getText().equals("parent")) {
                                final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                                if (resolved instanceof Method && !((Method) resolved).isStatic()) {
                                    return false;
                                }
                            }
                        }
                    }
                }
                return body != null;
            }
        };
    }

    private static final class MakeClosureStaticFix implements LocalQuickFix {
        private static final String title = "Declare the closure static";

        @NotNull
        @Override
        public String getName() {
            return ReportingUtil.wrapReportedMessage(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
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
