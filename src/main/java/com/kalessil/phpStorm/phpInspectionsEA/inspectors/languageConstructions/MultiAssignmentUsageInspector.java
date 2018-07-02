package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiAssignmentUsageInspector extends BasePhpInspection {
    private static final String messagePattern      = "Perhaps 'list(...) = %a%' can be used instead (check similar statements).";
    private static final String messageImplicitList = "foreach (... as list(...)) could be used instead.";

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
                final PhpLanguageLevel phpVersion
                        = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (!phpVersion.hasFeature(PhpLanguageFeature.FOREACH_LIST)) {
                    return;
                }

                /* verify if it's dedicated statement and it's the list(...) construction */
                PsiElement parent = multiassignmentExpression.getParent();
                if (!OpenapiTypesUtil.isStatementImpl(parent)) {
                    return;
                }
                final PsiElement listKeyword = multiassignmentExpression.getFirstChild();
                final IElementType nodeType  = null == listKeyword ? null : listKeyword.getNode().getElementType();
                if (null == nodeType || (PhpTokenTypes.kwLIST != nodeType && PhpTokenTypes.chLBRACKET != nodeType)) {
                    return;
                }

                /* extract container: it needs to be a variable */
                PsiElement container = multiassignmentExpression.getValue();
                if (OpenapiTypesUtil.isPhpExpressionImpl(container)) {
                    container = ((PhpExpression) container).getFirstPsiChild();
                }
                if (!(container instanceof Variable)) {
                    return;
                }

                /* lookup parent foreach-statements for providing the container */
                /* TODO: check if container being used 2+ times in the foreach-expression */
                boolean stopAnalysis       = false;
                final String containerName = ((Variable) container).getName();
                while (null != parent && ! (parent instanceof Function) && ! (parent instanceof PhpFile)) {
                    if (parent instanceof ForeachStatement) {
                        final List<Variable> variables = ((ForeachStatement) parent).getVariables();
                        for (final Variable variable : variables) {
                            if (variable.getName().equals(containerName)) {
                                stopAnalysis = true;

                                holder.registerProblem(multiassignmentExpression, messageImplicitList);
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
                if (!(assignmentExpression.getVariable() instanceof Variable)){
                    return;
                }

                /* ensure that preceding expression is also an assignment */
                final PsiElement parent = assignmentExpression.getParent();
                if (!OpenapiTypesUtil.isStatementImpl(parent)) {
                    return;
                }
                PsiElement previous = ((Statement) parent).getPrevPsiSibling();
                while (previous instanceof PhpDocComment) {
                    previous = ((PhpDocComment) previous).getPrevPsiSibling();
                }
                if (!OpenapiTypesUtil.isStatementImpl(previous)) {
                    return;
                }
                final PhpPsiElement previousExpression = ((Statement) previous).getFirstPsiChild();
                if (!OpenapiTypesUtil.isAssignment(previousExpression)) {
                    return;
                }

                /* analyze if containers are matching */
                final PsiElement ownContainer = getContainer(assignmentExpression);
                if (ownContainer != null) {
                    final PsiElement previousContainer = getContainer((AssignmentExpression) previousExpression);
                    if (previousContainer != null && OpenapiEquivalenceUtil.areEqual(ownContainer, previousContainer)) {
                        final String message = messagePattern.replace("%a%", ownContainer.getText());
                        holder.registerProblem(assignmentExpression, message);
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
                if (OpenapiTypesUtil.isNumber(index.getValue())) {
                    return container;
                }

                return null;
            }
        };
    }
}
