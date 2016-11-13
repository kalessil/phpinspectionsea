package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocCommentImpl;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.AssignmentExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiAssignmentUsageInspector extends BasePhpInspection {
    private static final String messagePattern      = "Perhaps 'list(...) = %a%' can be used instead (check similar statements)";
    private static final String messageImplicitList = "foreach (... as list(...)) is possible since PHP 5.5";

    @NotNull
    public String getShortName() {
        return "MultiAssignmentUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMultiassignmentExpression(MultiassignmentExpression multiassignmentExpression) {
                /* ensure php version is at least PHP 5.5 */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (phpVersion.compareTo(PhpLanguageLevel.PHP550) < 0) {
                    return;
                }

                /* verify if it's dedicated statement and it's the list(...) construction */
                PsiElement parent = multiassignmentExpression.getParent();
                if (!(parent instanceof StatementImpl)) {
                    return;
                }
                final PsiElement listKeyword = multiassignmentExpression.getFirstChild();
                if (null == listKeyword || !listKeyword.getText().equalsIgnoreCase("list")) {
                    return;
                }

                /* extract container: it needs to be a variable */
                PsiElement container = multiassignmentExpression.getValue();
                if (container instanceof PhpExpressionImpl) {
                    container = ((PhpExpressionImpl) container).getFirstPsiChild();
                }
                if (!(container instanceof VariableImpl)) {
                    return;
                }

                /* lookup parent foreach-statements for providing the container */
                /* TODO: check if container being used 2+ times in the foreach-expression */
                boolean stopAnalysis       = false;
                final String containerName = ((Variable) container).getName();
                while (null != parent && ! (parent instanceof Function) && ! (parent instanceof PhpFile)) {
                    if (parent instanceof ForeachStatement) {
                        final List<Variable> variables = ((ForeachStatement) parent).getVariables();
                        for (Variable variable : variables) {
                            final String variableName = variable.getName();
                            if (!StringUtil.isEmpty(variableName) && variableName.equals(containerName)) {
                                stopAnalysis = true;

                                holder.registerProblem(multiassignmentExpression, messageImplicitList, ProblemHighlightType.WEAK_WARNING);
                                break;
                            }
                        }
                        variables.clear();

                        if (stopAnalysis) {
                            break;
                        }
                    }
                    parent = parent.getParent();
                }
            }

            public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {
                /* ensure we are writing into a variable */
                if (!(assignmentExpression.getVariable() instanceof VariableImpl)){
                    return;
                }

                /* ensure that preceding expression is also an assignment */
                final PsiElement parent = assignmentExpression.getParent();
                if (!(parent instanceof StatementImpl)) {
                    return;
                }
                PsiElement previous = ((StatementImpl) parent).getPrevPsiSibling();
                while (previous instanceof PhpDocCommentImpl) {
                    previous = ((PhpDocCommentImpl) previous).getPrevPsiSibling();
                }
                if (!(previous instanceof StatementImpl)) {
                    return;
                }
                final PhpPsiElement previousExpression = ((StatementImpl) previous).getFirstPsiChild();
                if (!(previousExpression instanceof AssignmentExpressionImpl)) {
                    return;
                }

                /* analyze if containers are matching */
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
