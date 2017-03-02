package com.kalessil.phpStorm.phpInspectionsEA.inspectors.exceptions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpThrow;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NotThrownExceptionInspector extends BasePhpInspection {
    private static final String message = "It's probably intended to throw an exception here.";

    @NotNull
    public String getShortName() {
        return "NotThrownExceptionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpNewExpression(NewExpression expression) {
                final PsiElement parent = expression.getParent();
                final ClassReference argument = expression.getClassReference();
                if (null == argument || null == parent || parent.getClass() != StatementImpl.class) {
                    return;
                }

                final PsiElement reference = argument.resolve();
                if (reference instanceof PhpClass) {
                    final Set<PhpClass> inheritanceChain = InterfacesExtractUtil.getCrawlCompleteInheritanceTree((PhpClass) reference, true);
                    for (PhpClass clazz : inheritanceChain) {
                        if (clazz.getFQN().equals("\\Exception")) {
                            holder.registerProblem(expression, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix());
                            break;
                        }
                    }
                    inheritanceChain.clear();
                }
            }
        };
    }

    class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Add missing throw keyword";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (null != expression) {
                final PhpThrow throwExpression = PhpPsiElementFactory.createFromText(project, PhpThrow.class, "throw $x;");
                if (null != throwExpression) {
                    //noinspection ConstantConditions as we dealing with hard-coded expressions here
                    throwExpression.getArgument().replace(expression.copy());
                    expression.getParent().replace(throwExpression);
                }
            }
        }
    }
}
