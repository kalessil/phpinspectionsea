package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

public class NullCoalescingOperatorCanBeUsedInspector extends BasePhpInspection {
    private static final String messageUseOperator = "' ... ?? ...' construction shall be used instead";

    @NotNull
    public String getShortName() {
        return "NullCoalescingOperatorCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpTernaryExpression(TernaryExpression expression) {
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (!phpVersion.hasFeature(PhpLanguageFeature.COALESCE_OPERATOR)) {
                    return;
                }

                PsiElement issetCandidate = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());

                /* condition can be inverted */
                boolean isInverted = false;
                if (issetCandidate instanceof UnaryExpression) {
                    final PsiElement operator = ((UnaryExpression) issetCandidate).getOperation();
                    if (null != operator && PhpTokenTypes.opNOT == operator.getNode().getElementType()) {
                        isInverted = true;
                        issetCandidate = ((UnaryExpression) issetCandidate).getValue();
                    }
                }


                if (issetCandidate instanceof PhpIsset) {
                    final PhpIsset isset = (PhpIsset) issetCandidate;
                    if (1 != isset.getVariables().length) {
                        return;
                    }

                    /* construction requirements */
                    PsiElement alternativeVariant = isInverted ? expression.getFalseVariant() : expression.getTrueVariant();
                    alternativeVariant            = ExpressionSemanticUtil.getExpressionTroughParenthesis(alternativeVariant);
                    if (null == alternativeVariant) {
                        return;
                    }
                    final PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(isset.getVariables()[0]);
                    if (null == condition) {
                        return;
                    }

                    /* inspection itself */
                    if (PsiEquivalenceUtil.areElementsEquivalent(condition, alternativeVariant)) {
                        holder.registerProblem(alternativeVariant, messageUseOperator, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(isInverted));
                    }

                    return;
                }

                /* older version might influence isset->array_key_exists in ternary conditions */
                if (issetCandidate instanceof FunctionReference) {
                    /* verify  general function requirements */
                    final FunctionReference call = (FunctionReference) issetCandidate;
                    final PsiElement[] params    = call.getParameters();
                    final String functionName    = call.getName();
                    if (2 != params.length || StringUtil.isEmpty(functionName) || !functionName.equals("array_key_exists")) {
                        return;
                    }

                    /* when array_key_exists alternative value is not null, it intended to be so */
                    PsiElement alternativeVariant = isInverted ? expression.getTrueVariant() : expression.getFalseVariant();
                    alternativeVariant            = ExpressionSemanticUtil.getExpressionTroughParenthesis(alternativeVariant);
                    if (!PhpLanguageUtil.isNull(alternativeVariant)) {
                        return;
                    }

                    /* construction requirements */
                    PsiElement primaryVariant = isInverted ? expression.getFalseVariant() : expression.getTrueVariant();
                    primaryVariant            = ExpressionSemanticUtil.getExpressionTroughParenthesis(primaryVariant);
                    if (primaryVariant instanceof ArrayAccessExpression) {
                        final ArrayAccessExpression array = (ArrayAccessExpression) primaryVariant;
                        final PsiElement container        = array.getValue();
                        final ArrayIndex index            = array.getIndex();
                        if (null == container || null == index || null == index.getValue()) {
                            return;
                        }

                        /* match array_key_exists arguments with corresponding true variant */
                        if (
                            null == params[0] || null == params[1] ||
                            !PsiEquivalenceUtil.areElementsEquivalent(params[1], container) ||
                            !PsiEquivalenceUtil.areElementsEquivalent(params[0], index.getValue())
                        ) {
                            return;
                        }

                        holder.registerProblem(primaryVariant, messageUseOperator, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(isInverted));
                    }
                }
            }
        };
    }

    static private class TheLocalFix implements LocalQuickFix {
        private boolean isInverted;

        public TheLocalFix(boolean isInverted) {
            super();

            this.isInverted = isInverted;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use ?? instead";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement target                 = descriptor.getPsiElement();
            final PsiElement ternaryCandidate = target.getParent();
            if (ternaryCandidate instanceof TernaryExpression) {
                final TernaryExpression ternary = (TernaryExpression) ternaryCandidate;
                if (null == ternary.getCondition()) {
                    return;
                }

                /* if inverted case, swap branches and use regular fixing procedure */
                if (this.isInverted && null != ternary.getTrueVariant() && null != ternary.getFalseVariant()) {
                    final PsiElement temp = ternary.getTrueVariant().copy();
                    ternary.getTrueVariant().replace(ternary.getFalseVariant().copy());
                    ternary.getFalseVariant().replace(temp);

                    target = ternary.getTrueVariant();
                }

                /* swap parts */
                ternary.getCondition().replace(target.copy());

                /* cleanup spaces around */
                PsiElement before = target.getPrevSibling();
                if (before instanceof PsiWhiteSpace) {
                    before = before.getPrevSibling();
                    target.getPrevSibling().delete();
                }
                PsiElement after = target.getNextSibling();
                if (after instanceof PsiWhiteSpace) {
                    after = after.getNextSibling();
                    target.getNextSibling().delete();
                }

                /* modify the operator and drop the true expression */
                //noinspection ConstantConditions I'm pretty sure that hardcoded expression is not producing nulls
                before.replace(PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "??"));
                target.delete();
                after.delete();
            }
        }
    }
}