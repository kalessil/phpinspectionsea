package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.ArrayIndex;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.impl.AssignmentExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiAssignmentUsageInspector extends BasePhpInspection {
    private static final String messagePattern = "'list(...) = %a%' can be used instead";

    @NotNull
    public String getShortName() {
        return "MultiAssignmentUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {
                /* ensure that preceding expression is also assignment */
                final PsiElement parent = assignmentExpression.getParent();
                if (!(parent instanceof StatementImpl)) {
                    return;
                }
                final PsiElement previous = ((StatementImpl) parent).getPrevPsiSibling();
                if (!(previous instanceof StatementImpl)) {
                    return;
                }
                final PhpPsiElement previousExpression = ((StatementImpl) previous).getFirstPsiChild();
                if (!(previousExpression instanceof AssignmentExpressionImpl)) {
                    return;
                }

                /* analyze if containers matches */
                final PsiElement ownContainer = getContainer(assignmentExpression);
                if (null != ownContainer) {
                    final PsiElement previousContainer = getContainer((AssignmentExpression) previousExpression);
                    if (null != previousContainer && PsiEquivalenceUtil.areElementsEquivalent(ownContainer, previousContainer)) {
                        final String message = messagePattern.replace("%a%", ownContainer.getText());
                        holder.registerProblem(assignmentExpression, message, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }

            @Nullable
            private PsiElement getContainer(@NotNull AssignmentExpression assignmentExpression) {
                /* value needs to be a array access expression */
                final PsiElement accessCandidate = assignmentExpression.getValue();
                if (!(accessCandidate instanceof ArrayAccessExpression)) {
                    return null;
                }

                /* ensure we have finished structure */
                final ArrayAccessExpression value = (ArrayAccessExpression) accessCandidate;
                final PsiElement container        = value.getValue();
                final ArrayIndex index            = value.getIndex();
                if (null == container || null == index || null == index.getValue()) {
                    return null;
                }

                /* we'll check only numeric arrays */
                final PsiElement indexValue = index.getValue();
                if (PhpElementTypes.NUMBER == indexValue.getNode().getElementType()) {
                    return container;
                }

                return null;
            }
        };
    }
}
