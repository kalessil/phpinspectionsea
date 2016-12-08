package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.SelfAssignmentExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPsiResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class AdditionOperationOnArraysInspection extends BasePhpInspection {
    private static final String message = "Perhaps array_merge/array_replace can be used instead. Feel free to disable the inspection if '+' is intended.";

    @NotNull
    public String getShortName() {
        return "AdditionOperationOnArraysInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(BinaryExpression expression) {
                final PsiElement operation = expression.getOperation();
                if (null != operation && PhpTokenTypes.opPLUS == operation.getNode().getElementType()) {
                    /* do not check nested operations */
                    if (expression.getParent() instanceof BinaryExpression){
                        return;
                    }

                    /* do not report ' ... + []' and '[] + ...' */
                    final PsiElement mostRight = expression.getRightOperand();
                    PsiElement mostLeft        = expression.getLeftOperand();
                    while (mostLeft instanceof BinaryExpression) {
                        mostLeft = ((BinaryExpression) mostLeft).getLeftOperand();
                    }
                    if (
                        null == mostLeft  || mostLeft instanceof ArrayCreationExpression ||
                        null == mostRight || mostRight instanceof ArrayCreationExpression
                    ) {
                        return;
                    }

                    this.inspectExpression(operation, expression);
                }
            }
            public void visitPhpSelfAssignmentExpression(SelfAssignmentExpression expression) {
                final PsiElement operation = expression.getOperation();
                if (null != operation && PhpTokenTypes.opPLUS_ASGN == operation.getNode().getElementType()) {
                    /* Do not report '... += []' */
                    if (expression.getValue() instanceof ArrayCreationExpression) {
                        return;
                    }

                    this.inspectExpression(operation, expression);
                }
            }

            /* inspection itself */
            private void inspectExpression(@NotNull PsiElement objOperation, @NotNull PsiElement expression) {
                final PhpIndex index = PhpIndex.getInstance(holder.getProject());
                final Function scope = ExpressionSemanticUtil.getScope(expression);

                final HashSet<String> typesResolved = new HashSet<>();
                TypeFromPsiResolvingUtil.resolveExpressionType(expression, scope, index, typesResolved);
                if (1 == typesResolved.size() && typesResolved.iterator().next().equals(Types.strArray)) {
                    holder.registerProblem(objOperation, message, ProblemHighlightType.ERROR);
                }
                typesResolved.clear();
            }
        };
    }
}
