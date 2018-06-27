package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.jetbrains.php.lang.psi.elements.PhpIsset;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

public class IssetConstructsCanBeMergedInspector extends BasePhpInspection {
    private static final String messageIsset        = "This can be merged into the previous 'isset(..., ...[, ...])'.";
    private static final String messageIvertedIsset = "This can be merged into the previous '!isset(..., ...[, ...])'.";

    @NotNull
    public String getShortName() {
        return "IssetConstructsCanBeMergedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                final IElementType operator = expression.getOperationType();
                if (operator != null && (operator == PhpTokenTypes.opAND || operator == PhpTokenTypes.opOR)) {
                    /* false-positives: part of another condition */
                    final PsiElement parent  = expression.getParent();
                    final PsiElement context = parent instanceof ParenthesizedExpression ? parent.getParent() : parent;
                    if (context instanceof BinaryExpression && ((BinaryExpression) context).getOperationType() == operator) {
                        return;
                    }

                    final List<PsiElement> fragments = this.extract(expression, operator);
                    if (fragments.size() > 1) {
                        if (operator == PhpTokenTypes.opAND) {
                            /* handle isset && isset ... */
                            PsiElement firstHit = null;
                            int hitsCount       = 0;
                            for (final PsiElement fragment : fragments) {
                                if (fragment instanceof PhpIsset) {
                                    if (++hitsCount > 1 && firstHit != null) {
                                        fragments.remove(firstHit);
                                        fragments.remove(fragment);
                                        holder.registerProblem(
                                            fragment,
                                            messageIsset,
                                            new MergeConstructsFix(expression, fragments, (PhpIsset)firstHit, (PhpIsset)fragment, operator)
                                        );
                                        break;
                                    }
                                    firstHit = firstHit == null ? fragment : firstHit;
                                }
                            }
                        } else {
                            /* handle !isset || !isset ... */
                            PsiElement firstHit = null;
                            int hitsCount       = 0;
                            for (final PsiElement fragment : fragments) {
                                if (fragment instanceof UnaryExpression) {
                                    final PsiElement candidate = ((UnaryExpression) fragment).getValue();
                                    if (candidate instanceof PhpIsset) {
                                        if (++hitsCount > 1 && firstHit != null) {
                                            fragments.remove(firstHit.getParent());
                                            fragments.remove(fragment);
                                            holder.registerProblem(
                                                candidate,
                                                messageIvertedIsset,
                                                new MergeConstructsFix(expression, fragments, (PhpIsset)firstHit, (PhpIsset)candidate, operator)
                                            );
                                            break;
                                        }
                                        firstHit = firstHit == null ? candidate : firstHit;
                                    }
                                }
                            }
                        }

                    }
                    fragments.clear();
                }
            }

            @NotNull
            private List<PsiElement> extract(@NotNull BinaryExpression binary, @Nullable IElementType operator) {
                final List<PsiElement> result = new ArrayList<>();
                if (binary.getOperationType() == operator) {
                    Stream.of(binary.getLeftOperand(), binary.getRightOperand())
                        .filter(Objects::nonNull).map(ExpressionSemanticUtil::getExpressionTroughParenthesis)
                        .forEach(expression -> {
                            if (expression instanceof BinaryExpression) {
                                result.addAll(this.extract((BinaryExpression) expression, operator));
                            } else {
                                result.add(expression);
                            }
                        });
                } else {
                    result.add(binary);
                }
                return result;
            }
        };
    }

    private static final class MergeConstructsFix implements LocalQuickFix {
        private static final String title = "Merge 'isset(...)' constructs";

        final private SmartPsiElementPointer<BinaryExpression> binary;
        final private List<String> fragments;
        final private SmartPsiElementPointer<PhpIsset> first;
        final private SmartPsiElementPointer<PhpIsset> second;
        final private IElementType operator;

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

        MergeConstructsFix(
            @NotNull BinaryExpression binary,
            @NotNull List<PsiElement> fragments,
            @NotNull PhpIsset first,
            @NotNull PhpIsset second,
            @NotNull IElementType operator
        ) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(binary.getProject());

            this.operator = operator;
            this.binary   = factory.createSmartPsiElementPointer(binary);
            this.first    = factory.createSmartPsiElementPointer(first);
            this.second   = factory.createSmartPsiElementPointer(second);

            this.fragments = fragments.stream().filter(Objects::nonNull)
                    .map(fragment -> {
                        final PsiElement parent = fragment.getParent();
                        return parent instanceof ParenthesizedExpression ? parent.getText() : fragment.getText();
                    })
                    .collect(Collectors.toList());
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final BinaryExpression binary = this.binary.getElement();
            final PhpIsset first          = this.first.getElement();
            final PhpIsset second         = this.second.getElement();
            if (binary != null && first != null && second != null && !project.isDisposed()) {
                /* generate isset-statement */
                final List<String> arguments = new ArrayList<>();
                Stream.of(first, second).forEach(construct ->
                    Arrays.stream(construct.getVariables()).forEach(argument -> arguments.add(argument.getText()))
                );
                final String patternIsset = operator == PhpTokenTypes.opAND ? "isset(%s)" : "!isset(%s)";
                final String isset        = String.format(patternIsset, String.join(", ", arguments));
                arguments.clear();

                /* collect new binary fragments */
                final List<String> fragments = new ArrayList<>();
                fragments.add(isset);
                fragments.addAll(this.fragments);

                /* generate replacement */
                final String delimiter   = operator == PhpTokenTypes.opAND ? " && " : " || ";
                final String replacement = '(' + String.join(delimiter, fragments) + ')';
                fragments.clear();

                /* replace expression */
                final PsiElement donor = PhpPsiElementFactory
                        .createPhpPsiFromText(project, ParenthesizedExpression.class, replacement)
                        .getArgument();
                if (donor != null) {
                    binary.replace(donor);
                }
            }
        }
    }
}