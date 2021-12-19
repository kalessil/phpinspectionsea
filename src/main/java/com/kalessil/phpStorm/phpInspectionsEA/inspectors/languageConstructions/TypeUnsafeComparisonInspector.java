package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.strategy.ClassInStringContextStrategy;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class TypeUnsafeComparisonInspector extends BasePhpInspection {
    private static final String patternHarden                = "Please consider using more strict '%s' here (hidden types casting will not be applied anymore).";
    private static final String patternCompareStrict         = "Safely use '%s' here.";
    private static final String messageToStringMethodMissing = "%class% miss __toString() implementation.";

    private final static Set<String> comparable = new HashSet<>();
    static {
        comparable.add("\\Closure");
        comparable.add("\\DateTime");
        comparable.add("\\DateTimeImmutable");
        comparable.add("\\IntlBreakIterator");
        comparable.add("\\IntlTimeZone");
        comparable.add("\\PDO");
        comparable.add("\\PDOStatement");
        comparable.add("\\ArrayObject");
        comparable.add("\\SplObjectStorage");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "TypeUnsafeComparisonInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Type unsafe comparison";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                final IElementType operator = expression.getOperationType();
                if (operator == PhpTokenTypes.opEQUAL || operator == PhpTokenTypes.opNOT_EQUAL) {
                    this.analyze(expression, operator);
                }
            }

            private void analyze(@NotNull final BinaryExpression subject, @NotNull final IElementType operator) {
                final String targetOperator = PhpTokenTypes.opEQUAL == operator ? "===" : "!==";
                final PsiElement left       = subject.getLeftOperand();
                final PsiElement right      = subject.getRightOperand();
                if (right instanceof StringLiteralExpression || left instanceof StringLiteralExpression) {
                    final PsiElement nonStringOperand;
                    final String literalValue;
                    if (right instanceof StringLiteralExpression) {
                        literalValue     = ((StringLiteralExpression) right).getContents();
                        nonStringOperand = ExpressionSemanticUtil.getExpressionTroughParenthesis(left);
                    } else {
                        literalValue     = ((StringLiteralExpression) left).getContents();
                        nonStringOperand = ExpressionSemanticUtil.getExpressionTroughParenthesis(right);
                    }

                    /* resolve 2nd operand type, if class ensure __toString is implemented */
                    if (ClassInStringContextStrategy.apply(nonStringOperand, holder, subject, messageToStringMethodMissing)) {
                        /* TODO: weak warning regarding under-the-hood string casting */
                        return;
                    }

                    /* string literal is numeric or empty, no strict compare possible */
                    if (! literalValue.isEmpty() && ! literalValue.matches("^[+-]?[0-9]*\\.?[0-9]+$")) {
                        holder.registerProblem(
                                subject,
                                String.format(MessagesPresentationUtil.prefixWithEa(patternCompareStrict), targetOperator),
                                new CompareStrictFix(targetOperator)
                        );
                        return;
                    }
                }

                /* some objects supporting direct comparison: search for .compare_objects in PHP sources */
                if (left != null && right != null) {
                    final boolean isComparableObject = this.isComparableObject(left) || this.isComparableObject(right);
                    if (! isComparableObject) {
                        holder.registerProblem(
                                subject,
                                String.format(MessagesPresentationUtil.prefixWithEa(patternHarden), targetOperator),
                                ProblemHighlightType.WEAK_WARNING
                        );
                    }

                }
            }

            private boolean isComparableObject(@NotNull PsiElement operand) {
                if (operand instanceof PhpTypedElement) {
                    final Project project  = holder.getProject();
                    final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) operand, project);
                    if (resolved != null) {
                        final PhpIndex index        = PhpIndex.getInstance(project);
                        final Set<PhpClass> classes = new HashSet<>();
                        resolved.filterUnknown().getTypes().stream()
                                .filter(t  -> t.charAt(0) == '\\')
                                .forEach(t -> classes.addAll(OpenapiResolveUtil.resolveClassesAndInterfacesByFQN(Types.getType(t), index)));
                        for (final PhpClass clazz : classes) {
                            final boolean hasAny =
                                    comparable.contains(clazz.getFQN()) ||
                                    InterfacesExtractUtil.getCrawlInheritanceTree(clazz, true).stream().anyMatch(c -> comparable.contains(c.getFQN()));
                            if (hasAny) {
                                classes.clear();
                                return true;
                            }
                        }
                        classes.clear();
                    }
                }

                return false;
            }
        };
    }


    private static final class CompareStrictFix implements LocalQuickFix {
        private static final String title = "Apply strict comparison";

        final private String operator;

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

        CompareStrictFix(@NotNull String operator) {
            this.operator = operator;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target instanceof BinaryExpression && ! project.isDisposed()) {
                final PsiElement operation   = ((BinaryExpression) target).getOperation();
                final PsiElement replacement = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, operator);
                if (operation != null && replacement != null) {
                    operation.replace(replacement);
                }
            }
        }
    }
}