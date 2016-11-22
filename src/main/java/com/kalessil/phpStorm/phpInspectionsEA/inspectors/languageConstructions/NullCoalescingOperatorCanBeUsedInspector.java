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
                if (issetCandidate instanceof PhpIsset) {
                    final PhpIsset isset = (PhpIsset) issetCandidate;
                    if (1 != isset.getVariables().length) {
                        return;
                    }

                    /* construction requirements */
                    final PsiElement trueVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                    if (null == trueVariant) {
                        return;
                    }
                    final PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(isset.getVariables()[0]);
                    if (null == condition) {
                        return;
                    }

                    if (PsiEquivalenceUtil.areElementsEquivalent(condition, trueVariant)) {
                        holder.registerProblem(trueVariant, messageUseOperator, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
                    }
                }

                /* older version might influence isset->array_key_exists in ternary conditions */
                if (issetCandidate instanceof FunctionReference) {
                    final String functionName = ((FunctionReference) issetCandidate).getName();
                    if (!StringUtil.isEmpty(functionName) && functionName.equals("array_key_exists")) {
                        /* when array_key_exists alternative value is not null, it intended to be so */
                        final PsiElement falseVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getFalseVariant());
                        if (!PhpLanguageUtil.isNull(falseVariant)) {
                            return;
                        }

                        /* construction requirements */
                        final PsiElement trueVariant = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getTrueVariant());
                        if (trueVariant instanceof ArrayAccessExpression) {
                            final ArrayAccessExpression array = (ArrayAccessExpression) trueVariant;
                            final PsiElement container        = array.getValue();
                            final ArrayIndex index            = array.getIndex();
                            if (null == container || null == index || null == index.getValue()) {
                                return;
                            }

                            /* match array_key_exists arguments with corresponding true variant */
                            final PsiElement[] params = ((FunctionReference) issetCandidate).getParameters();
                            if (
                                null == params[0] || null == params[1] ||
                                !PsiEquivalenceUtil.areElementsEquivalent(params[1], container) ||
                                !PsiEquivalenceUtil.areElementsEquivalent(params[0], index.getValue())
                            ) {
                                return;
                            }

                            holder.registerProblem(trueVariant, messageUseOperator, ProblemHighlightType.WEAK_WARNING);
                        }
                    }
                }
            }
        };
    }

    static private class TheLocalFix implements LocalQuickFix {
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
            final PsiElement target           = descriptor.getPsiElement();
            final PsiElement ternaryCandidate = target.getParent();
            if (ternaryCandidate instanceof TernaryExpression) {
                final TernaryExpression ternary = (TernaryExpression) ternaryCandidate;
                if (null == ternary.getCondition()) {
                    return;
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