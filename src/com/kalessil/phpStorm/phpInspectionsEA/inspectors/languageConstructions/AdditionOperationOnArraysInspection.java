package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
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
    private static final String strProblemDescription = "Use 'array_merge(...)' to merge arrays";

    @NotNull
    public String getDisplayName() {
        return "Probable bugs: plus operator on arrays";
    }

    @NotNull
    public String getShortName() {
        return "AdditionOperationOnArraysInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(BinaryExpression expression) {
                PsiElement objOperation = expression.getOperation();
                if (null == objOperation) {
                    return;
                }

                final IElementType operationType = objOperation.getNode().getElementType();
                if (operationType != PhpTokenTypes.opPLUS) {
                    return;
                }

                PhpIndex objIndex = PhpIndex.getInstance(holder.getProject());
                Function objScope = ExpressionSemanticUtil.getScope(expression);

                HashSet<String> typesResolved = new HashSet<>();
                TypeFromPsiResolvingUtil.resolveExpressionType(expression, objScope, objIndex, typesResolved);
                if (typesResolved.size() != 1 || !typesResolved.iterator().next().equals(Types.strArray)) {
                    return;
                }

                holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.ERROR);
            }

            public void visitPhpSelfAssignmentExpression(SelfAssignmentExpression expression) {
                PsiElement objOperation = expression.getOperation();
                if (null == objOperation) {
                    return;
                }

                final IElementType operationType = objOperation.getNode().getElementType();
                if (operationType != PhpTokenTypes.opPLUS_ASGN) {
                    return;
                }

                PhpIndex objIndex = PhpIndex.getInstance(holder.getProject());
                Function objScope = ExpressionSemanticUtil.getScope(expression);

                HashSet<String> typesResolved = new HashSet<>();
                TypeFromPsiResolvingUtil.resolveExpressionType(expression, objScope, objIndex, typesResolved);
                if (typesResolved.size() != 1 || !typesResolved.iterator().next().equals(Types.strArray)) {
                    typesResolved.clear();
                    return;
                }

                typesResolved.clear();
                holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.ERROR);
            }
        };
    }
}
