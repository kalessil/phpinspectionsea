package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.strategy;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpIsset;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.jetbrains.php.lang.psi.elements.impl.BinaryExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.ConstantReferenceImpl;
import com.jetbrains.php.refactoring.PhpRefactoringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;

public class IssetAndNullComparisonStrategy {
    final static String messagePattern = "Seems like '%e%' is already covered by isset";

    static public boolean apply(@NotNull LinkedList<PsiElement> conditions, @NotNull ProblemsHolder holder) {
        /* first ensure that we have null identity checks at all */
        HashMap<PsiElement, PsiElement> nullTestSubjects = new HashMap<PsiElement, PsiElement>();
        for (PsiElement oneCondition : conditions) {
            if (oneCondition instanceof BinaryExpressionImpl) {
                final BinaryExpressionImpl expression = (BinaryExpressionImpl) oneCondition;

                /* we need only !== and === operations */
                final PsiElement operation  = expression.getOperation();
                final IElementType operator = null == operation ? null : operation.getNode().getElementType();
                if (operator != PhpTokenTypes.opIDENTICAL && operator != PhpTokenTypes.opNOT_IDENTICAL) {
                    continue;
                }

                /* quickly check if any operands is a constant */
                final PsiElement left  = expression.getLeftOperand();
                final PsiElement right = expression.getRightOperand();
                if (!(left instanceof ConstantReferenceImpl) && !(right instanceof ConstantReferenceImpl)) {
                    continue;
                }

                /* store null test subjects */
                if (right instanceof ConstantReferenceImpl && PhpLangUtil.isNull((ConstantReferenceImpl) right)) {
                    if (null != left) {
                        nullTestSubjects.put(expression, left);
                    }
                    continue;
                }
                if (left instanceof ConstantReferenceImpl && PhpLangUtil.isNull((ConstantReferenceImpl) left)) {
                    if (null != right) {
                        nullTestSubjects.put(expression, right);
                    }
                    // continue;
                }
            }
        }
        if (0 == nullTestSubjects.size()) {
            return false;
        }

        boolean hasReportedExpressions = false;
        for (PsiElement oneCondition : conditions) {
            /* do not process null identity checks */
            if (nullTestSubjects.containsKey(oneCondition)) {
                continue;
            }

            /* unwrap ! and () */
            PsiElement issetCandidate = oneCondition;
            if (issetCandidate instanceof UnaryExpression) {
                final PsiElement notOperatorCandidate = ((UnaryExpression) issetCandidate).getOperation();
                if (null != notOperatorCandidate && notOperatorCandidate.getNode().getElementType() == PhpTokenTypes.opNOT) {
                    PsiElement invertedValue = ((UnaryExpression) issetCandidate).getValue();
                    invertedValue = ExpressionSemanticUtil.getExpressionTroughParenthesis(invertedValue);
                    if (null == invertedValue) {
                        continue;
                    }

                    issetCandidate = invertedValue;
                }
            }
            if (!(issetCandidate instanceof PhpIsset) || 0 == ((PhpIsset) issetCandidate).getVariables().length) {
                continue;
            }

            /* process isset constructions */
            for (PsiElement issetArgument : ((PhpIsset) issetCandidate).getVariables()) {
                /* compare with know null identity checked subjects */
                for (PsiElement nullTestExpression: nullTestSubjects.keySet()) {
                    final PsiElement subject = nullTestSubjects.get(nullTestExpression);
                    if (PsiEquivalenceUtil.areElementsEquivalent(subject, issetArgument)) {
                        hasReportedExpressions = true;

                        final String message = messagePattern.replace("%e%", nullTestExpression.getText());
                        holder.registerProblem(nullTestExpression, message, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }
        }
        nullTestSubjects.clear();

        return hasReportedExpressions;
    }
}
