package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpEchoStatement;
import com.jetbrains.php.lang.psi.elements.PhpPrintExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ShortEchoTagCanBeUsedInspector extends BasePhpInspection {
    private static final String message = "'<?= ... ?>' could be used instead.";

    @NotNull
    @Override
    public String getShortName() {
        return "ShortEchoTagCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Short echo tag can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpEchoStatement(@NotNull PhpEchoStatement echo) {
                this.analyze(echo, echo);
            }

            @Override
            public void visitPhpPrint(@NotNull PhpPrintExpression print) {
                final PsiElement parent = print.getParent();
                this.analyze(print, OpenapiTypesUtil.isStatementImpl(parent) ? parent : print);
            }

            private void analyze(@NotNull PsiElement target, @NotNull PsiElement context) {
                PsiElement openingTag = context.getPrevSibling();
                if (openingTag instanceof PsiWhiteSpace) {
                    openingTag = openingTag.getPrevSibling();
                }
                if (OpenapiTypesUtil.is(openingTag, PhpTokenTypes.PHP_OPENING_TAG)) {
                    PsiElement closingTag = context.getNextSibling();
                    if (closingTag instanceof PsiWhiteSpace) {
                        closingTag = closingTag.getNextSibling();
                    }
                    if (OpenapiTypesUtil.is(closingTag, PhpTokenTypes.PHP_CLOSING_TAG)) {
                        holder.registerProblem(
                                target.getFirstChild(),
                                MessagesPresentationUtil.prefixWithEa(message),
                                new UseShortEchoTagInspector(holder.getProject(), openingTag, context)
                        );
                    }
                }
            }
        };
    }

    private static final class UseShortEchoTagInspector implements LocalQuickFix {
        private static final String title = "Use '<?= ... ?>' instead";

        private final SmartPsiElementPointer<PsiElement> expression;
        private final SmartPsiElementPointer<PsiElement> tag;

        private UseShortEchoTagInspector(@NotNull Project project, @NotNull PsiElement tag, @NotNull PsiElement expression) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(project);
            this.tag        = factory.createSmartPsiElementPointer(tag);
            this.expression = factory.createSmartPsiElementPointer(expression);
        }

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null && !project.isDisposed()) {
                final PsiElement tag        = this.tag.getElement();
                final PsiElement expression = this.expression.getElement();
                if (tag != null && expression != null) {
                    final PsiElement newTag = PhpPsiElementFactory.createFromText(project, PhpTokenTypes.PHP_ECHO_OPENING_TAG, "?><?=?>");
                    final String arguments  = Stream.of(target.getParent().getChildren()).map(PsiElement::getText).collect(Collectors.joining(", "));
                    final PsiElement echo   = PhpPsiElementFactory.createFromText(project, PhpEchoStatement.class, String.format("echo %s", arguments));
                    if (newTag != null && echo != null) {
                        echo.getFirstChild().delete();
                        expression.replace(echo);
                        tag.replace(newTag);
                    }
                }
            }
        }
    }
}
