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
    private static final String strProblemDescription = "'->query()' should be used instead of 'prepare-execute' calls chain";

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

                /* inspect preceding statement */
                PsiElement predecessor = reference.getParent();
                if (predecessor instanceof StatementImpl) {
                    predecessor = ((StatementImpl) predecessor).getPrevPsiSibling();
                }
                if (null != predecessor && predecessor.getFirstChild() instanceof AssignmentExpression) {
                    /* predecessor needs to be an assignment */
                    AssignmentExpression assignment = (AssignmentExpression) predecessor.getFirstChild();
                    if (!(assignment.getValue() instanceof MethodReference)) {
                        return;
                    }

                    /* predecessor's value is ->prepare */
                    MethodReference precedingReference = (MethodReference) assignment.getValue();
                    String precedingMethod = precedingReference.getName();
                    if (StringUtil.isEmpty(precedingMethod) || !precedingMethod.equals("prepare")) {
                        return;
                    }

                    PsiElement variableAssigned = assignment.getVariable();
                    PsiElement variableUsed     = reference.getClassReference();
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

