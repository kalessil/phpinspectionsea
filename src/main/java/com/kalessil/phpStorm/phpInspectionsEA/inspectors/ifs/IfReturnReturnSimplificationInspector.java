package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs;

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
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class IfReturnReturnSimplificationInspector extends BasePhpInspection {
    private static final String messagePattern = "The construct can be replaced with '%s'.";

    private static final Map<IElementType, String> inversionMapping = new HashMap<>(8);
    static {
        inversionMapping.put(PhpTokenTypes.opIDENTICAL, "!==");
        inversionMapping.put(PhpTokenTypes.opNOT_IDENTICAL, "===");
        inversionMapping.put(PhpTokenTypes.opEQUAL, "!=");
        inversionMapping.put(PhpTokenTypes.opNOT_EQUAL, "==");
        inversionMapping.put(PhpTokenTypes.opGREATER, "<=");
        inversionMapping.put(PhpTokenTypes.opGREATER_OR_EQUAL, "<");
        inversionMapping.put(PhpTokenTypes.opLESS, ">=");
        inversionMapping.put(PhpTokenTypes.opLESS_OR_EQUAL, ">");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "IfReturnReturnSimplificationInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "If-return-return could be simplified";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpIf(@NotNull If statement) {
                final PsiElement cond = ExpressionSemanticUtil.getExpressionTroughParenthesis(statement.getCondition());
                if (cond instanceof BinaryExpression && statement.getElseIfBranches().length == 0) {
                    final GroupStatement ifBody = ExpressionSemanticUtil.getGroupStatement(statement);
                    if (ifBody != null && ExpressionSemanticUtil.countExpressionsInGroup(ifBody) == 1) {
                        final PsiElement ifLast = ExpressionSemanticUtil.getLastStatement(ifBody);
                        if (ifLast instanceof PhpReturn) {
                            /* find first and second returns */
                            final PhpReturn first = (PhpReturn) ifLast;
                            PhpReturn second      = null;
                            final Else elseBranch = statement.getElseBranch();
                            if (elseBranch != null) {
                                final GroupStatement elseBody = ExpressionSemanticUtil.getGroupStatement(elseBranch);
                                if (elseBody != null && ExpressionSemanticUtil.countExpressionsInGroup(elseBody) == 1) {
                                    final PsiElement elseLast = ExpressionSemanticUtil.getLastStatement(elseBody);
                                    if (elseLast instanceof PhpReturn) {
                                        second = (PhpReturn) elseLast;
                                    }
                                }
                            } else {
                                final PsiElement next = statement.getNextPsiSibling();
                                if (next instanceof PhpReturn) {
                                    second = (PhpReturn) next;
                                }
                            }

                            /* if 2nd return found, check more pattern matches */
                            if (second != null) {
                                final boolean isDirect  = PhpLanguageUtil.isTrue(first.getArgument()) && PhpLanguageUtil.isFalse(second.getArgument());
                                final boolean isReverse = !isDirect && PhpLanguageUtil.isTrue(second.getArgument()) && PhpLanguageUtil.isFalse(first.getArgument());
                                if (isDirect || isReverse) {
                                    /* false-positives: if-return if-return return - code style */
                                    if (elseBranch == null) {
                                        final PsiElement before = statement.getPrevPsiSibling();
                                        if (before instanceof If && !ExpressionSemanticUtil.hasAlternativeBranches((If) before)) {
                                            final GroupStatement prevBody = ExpressionSemanticUtil.getGroupStatement(before);
                                            if (prevBody != null && ExpressionSemanticUtil.getLastStatement(prevBody) instanceof PhpReturn) {
                                                return;
                                            }
                                        }
                                    }

                                    /* final reporting step */
                                    final String replacement = String.format(isReverse ? "return !(%s)" : "return %s", cond.getText());
                                    holder.registerProblem(
                                            statement.getFirstChild(),
                                            MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, replacement)),
                                            new SimplifyFix(holder.getProject(), statement, elseBranch == null ? second : statement, replacement)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class SimplifyFix implements LocalQuickFix {
        private static final String title = "Use return instead";

        final private SmartPsiElementPointer<PsiElement> from;
        final private SmartPsiElementPointer<PsiElement> to;
        final String replacement;

        SimplifyFix(@NotNull Project project, @NotNull PsiElement from, @NotNull PsiElement to, @NotNull String replacement) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(project);

            this.from        = factory.createSmartPsiElementPointer(from);
            this.to          = factory.createSmartPsiElementPointer(to);
            this.replacement = replacement;
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
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement from = this.from.getElement();
            final PsiElement to   = this.to.getElement();
            if (from != null && to != null && ! project.isDisposed()) {
                PhpReturn replacement = PhpPsiElementFactory.createPhpPsiFromText(project, PhpReturn.class, this.replacement + ';');
                /* Do simplifications if possible */
                final PsiElement returnArgument = replacement.getArgument();
                if (returnArgument instanceof UnaryExpression) {
                    final UnaryExpression unary = (UnaryExpression) returnArgument;
                    if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                        final PsiElement unaryArgument = unary.getValue();
                        if (unaryArgument instanceof ParenthesizedExpression) {
                            final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(unaryArgument);
                            if (argument instanceof BinaryExpression) {
                                final BinaryExpression binary = (BinaryExpression) argument;
                                final PsiElement left         = binary.getLeftOperand();
                                final PsiElement right        = binary.getRightOperand();
                                final IElementType operator   = binary.getOperationType();
                                if (left != null && right != null && inversionMapping.containsKey(operator)) {
                                    replacement = PhpPsiElementFactory.createPhpPsiFromText(
                                            project,
                                            PhpReturn.class,
                                            String.format("return %s %s %s;", left.getText(), inversionMapping.get(operator), right.getText())
                                    );
                                }
                            }
                        }
                    }
                }
                /* Do the replacement */
                if (from == to) {
                    from.replace(replacement);
                } else {
                    final PsiElement parent = from.getParent();
                    parent.addBefore(replacement, from);
                    parent.deleteChildRange(from, to);
                }
            }
        }
    }
}