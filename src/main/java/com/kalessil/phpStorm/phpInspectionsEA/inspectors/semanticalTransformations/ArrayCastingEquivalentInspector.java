package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ArrayCastingEquivalentInspector extends BasePhpInspection {
    private static final String message = "'(array) ...' construct can probably be used (can change code behaviour).";

    @NotNull
    public String getShortName() {
        return "ArrayCastingEquivalentInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If expression) {
                if (!ExpressionSemanticUtil.hasAlternativeBranches(expression)){
                    /* body has only assignment, which to be extracted */
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(expression);
                    if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) == 1) {
                        PsiElement candidate = ExpressionSemanticUtil.getLastStatement(body);
                        candidate            = (candidate == null ? null : candidate.getFirstChild());
                        if (!OpenapiTypesUtil.isAssignment(candidate)) {
                            return;
                        }

                        /* expecting !function(...) in condition */
                        PsiElement condition   = null;
                        IElementType operation = null;
                        if (expression.getCondition() instanceof UnaryExpression) {
                            final UnaryExpression inversion = (UnaryExpression) expression.getCondition();
                            operation                       = inversion.getOperation().getNode().getElementType();
                            condition                       = ExpressionSemanticUtil.getExpressionTroughParenthesis(inversion.getValue());
                        }
                        if (operation != PhpTokenTypes.opNOT || !OpenapiTypesUtil.isFunctionReference(condition)) {
                            return;
                        }

                        /* inspect expression */
                        final AssignmentExpression assignment = (AssignmentExpression) candidate;
                        final PsiElement trueExpression       = assignment.getVariable();
                        final PsiElement falseExpression      = assignment.getValue();
                        if (
                            trueExpression != null && falseExpression != null &&
                            this.isArrayCasting((FunctionReference) condition, trueExpression, falseExpression)
                        ) {
                            final String replacement = trueExpression.getText() + " = (array) " + trueExpression.getText();
                            holder.registerProblem(expression.getFirstChild(), message, new SimplifyFix(replacement));
                        }
                    }
                }
            }

            /* expecting !function(...), true and false expressions */
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                PsiElement trueExpression  = expression.getTrueVariant();
                PsiElement falseExpression = expression.getFalseVariant();
                PsiElement condition       = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (condition instanceof UnaryExpression) {
                    final UnaryExpression unary = (UnaryExpression) condition;
                    final PsiElement operation  = unary.getOperation();
                    if (operation != null && operation.getNode().getElementType() == PhpTokenTypes.opNOT) {
                        condition       = ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue());
                        trueExpression  = expression.getFalseVariant();
                        falseExpression = expression.getTrueVariant();
                    }
                }

                if (trueExpression != null && falseExpression != null && OpenapiTypesUtil.isFunctionReference(condition)) {
                    if (this.isArrayCasting((FunctionReference) condition, trueExpression, falseExpression)) {
                        final String replacement  = "(array) " + trueExpression.getText();
                        holder.registerProblem(expression, message, new SimplifyFix(replacement));
                    }
                }
            }

            private boolean isArrayCasting(
                @NotNull FunctionReference condition,
                @NotNull PsiElement trueExpression,
                @NotNull PsiElement falseExpression
            ) {
                /* false variant should be array creation */
                if (falseExpression instanceof ArrayCreationExpression) {
                    /* condition expected to be is_array(arg) */
                    final String functionName = condition.getName();
                    final PsiElement[] params = condition.getParameters();
                    if (params.length == 1 && functionName != null && functionName.equals("is_array")) {
                        /* extract array values, expected one value only */
                        final List<PsiElement> valuesSet = new ArrayList<>();
                        for (final PsiElement child : falseExpression.getChildren()) {
                            if (child instanceof PhpPsiElement) {
                                valuesSet.add(child.getFirstChild());
                            }
                        }
                        /* ensure both true/false branches applied to the same subject */
                        boolean result =
                            valuesSet.size() == 1 &&
                            PsiEquivalenceUtil.areElementsEquivalent(trueExpression, params[0]) &&
                            PsiEquivalenceUtil.areElementsEquivalent(trueExpression, valuesSet.get(0));
                        valuesSet.clear();
                        /* ensure the subject type is array casting safe */
                        if (result) {
                            final Project project      = trueExpression.getProject();
                            final PhpType resolvedType = ((PhpTypedElement) trueExpression).getType().global(project);
                            if (resolvedType.hasUnknown() || resolvedType.getTypes().isEmpty()) {
                                /* well, types resolved partially - do not report */
                                result = false;
                            } else {
                                /* also, object casting to array "exports" it instead of wrapping */
                                for (final String type : resolvedType.getTypes()) {
                                    if (Types.getType(type).startsWith("\\")) {
                                        result = false;
                                        break;
                                    }
                                }
                            }
                        }

                        return result;
                    }
                }

                return false;
            }
        };
    }

    private class SimplifyFix extends UseSuggestedReplacementFixer {
        final private String expression;

        @NotNull
        @Override
        public String getName() {
            return "Use array casting instead";
        }

        SimplifyFix(@NotNull String expression) {
            super(expression);
            this.expression = expression;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression != null) {
                final PsiElement target = expression instanceof TernaryExpression ? expression : expression.getParent();
                final String pattern    = "(" + this.expression + ")";
                final ParenthesizedExpression replacement
                        = PhpPsiElementFactory.createPhpPsiFromText(project, ParenthesizedExpression.class, pattern);
                target.replace(replacement.getArgument());
            }
        }
    }
}
