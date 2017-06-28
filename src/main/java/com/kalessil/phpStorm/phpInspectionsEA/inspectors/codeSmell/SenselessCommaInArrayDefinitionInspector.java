package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;

public class SenselessCommaInArrayDefinitionInspector extends BasePhpInspection {
    private static final String message = "Can be safely dropped. The comma will be ignored by PHP.";

    @NotNull
    public String getShortName() {
        return "SenselessCommaInArrayDefinitionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(
        @NotNull final ProblemsHolder holder,
        final boolean isOnTheFly
    ) {
        return new BasePhpElementVisitor() {
            public void visitPhpArrayCreationExpression(final ArrayCreationExpression expression) {
                PsiElement subject = expression.getLastChild().getPrevSibling();
                if (subject instanceof PsiWhiteSpace) {
                    subject = subject.getPrevSibling();
                }

                if ((subject != null) &&
                    (PhpTokenTypes.opCOMMA == subject.getNode().getElementType())) {
                    holder.registerProblem(subject, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Safely drop comma";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(
            @NotNull final Project project,
            @NotNull final ProblemDescriptor descriptor
        ) {
            descriptor.getPsiElement().delete();
        }
    }
}
