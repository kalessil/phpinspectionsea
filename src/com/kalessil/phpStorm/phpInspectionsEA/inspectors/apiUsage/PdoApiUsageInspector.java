package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class PdoApiUsageInspector extends BasePhpInspection {
    private static final String strProblemDescription = "'->query(...)' or '>exec(...)'  should be used instead of 'prepare-execute' calls chain";

    @NotNull
    public String getShortName() {
        return "PdoApiUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethodReference(MethodReference reference) {
                /* check requirements */
                final PsiElement[] arrParams = reference.getParameters();
                final String strMethod       = reference.getName();
                if (arrParams.length > 1 || StringUtil.isEmpty(strMethod) || !strMethod.equals("execute")) {
                    return;
                }

                /* TODO: successor must not be same call (arguments can differ) */

                /* inspect preceding statement */
                final PsiElement parent = reference.getParent();
                PsiElement predecessor  = null;
                if (parent instanceof StatementImpl) {
                    predecessor = ((StatementImpl) parent).getPrevPsiSibling();
                }
                if (null != predecessor && predecessor.getFirstChild() instanceof AssignmentExpression) {
                    /* predecessor needs to be an assignment */
                    final AssignmentExpression assignment = (AssignmentExpression) predecessor.getFirstChild();
                    if (!(assignment.getValue() instanceof MethodReference)) {
                        return;
                    }

                    /* predecessor's value is ->prepare */
                    final MethodReference precedingReference = (MethodReference) assignment.getValue();
                    final String precedingMethod             = precedingReference.getName();
                    if (StringUtil.isEmpty(precedingMethod) || !precedingMethod.equals("prepare")) {
                        return;
                    }

                    final PsiElement variableAssigned = assignment.getVariable();
                    final PsiElement variableUsed     = reference.getClassReference();
                    if (
                        null != variableAssigned && null != variableUsed &&
                        PsiEquivalenceUtil.areElementsEquivalent(variableAssigned, variableUsed)
                    ) {
                        holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }
        };
    }
}

