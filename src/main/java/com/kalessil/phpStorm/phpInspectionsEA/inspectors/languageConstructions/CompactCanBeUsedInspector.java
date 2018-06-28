package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompactCanBeUsedInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' can be used instead instead (improves maintainability).";

    @NotNull
    public String getShortName() {
        return "CompactCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpArrayCreationExpression(@NotNull ArrayCreationExpression expression) {
                final List<String> variables = new ArrayList<>();
                for (final PsiElement pairCandidate : expression.getChildren()) {
                    /* match array structure */
                    if (!(pairCandidate instanceof ArrayHashElement)) {
                        return;
                    }
                    /* match pair structure */
                    final ArrayHashElement pair = (ArrayHashElement) pairCandidate;
                    final PhpPsiElement key     = pair.getKey();
                    final PhpPsiElement value   = pair.getValue();
                    if (!(key instanceof StringLiteralExpression) || !(value instanceof Variable)) {
                        return;
                    }
                    /* match index and variable */
                    final String index    = ((StringLiteralExpression) key).getContents();
                    final String variable = value.getName();
                    if (variable == null || !variable.equals(index)) {
                        return;
                    }

                    variables.add(key.getText());
                }

                if (variables.size() > 1) {
                    final String replacement = String.format("compact(%s)", variables.stream().collect(Collectors.joining(", ")));
                    final String message     = String.format(messagePattern, replacement);
                    holder.registerProblem(expression.getFirstChild(), message, new UseCompactFix(replacement));
                    variables.clear();
                }
            }
        };
    }

    private static final class UseCompactFix implements LocalQuickFix {
        private static final String title = "Use 'compact(...)' instead";

        private final String replacement;

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

        UseCompactFix(@NotNull String replacement) {
            this.replacement = replacement;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target     = descriptor.getPsiElement();
            final PsiElement expression = target == null ? null : target.getParent();
            if (expression != null) {
                expression.replace(PhpPsiElementFactory.createPhpPsiFromText(project, FunctionReference.class, replacement));
            }
        }
    }
}
