package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class PdoApiUsageInspector extends BasePhpInspection {
    private static final String message = "'->query(...)' or '->exec(...)'  should be used instead of 'prepare-execute' calls chain.";

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
                final String methodName      = reference.getName();
                if (0 != arrParams.length || StringUtil.isEmpty(methodName) || !methodName.equals("execute")) {
                    return;
                }

                /* inspect preceding and succeeding statement */
                final PsiElement parent = reference.getParent();
                PsiElement predecessor  = null;
                PsiElement successor    = null;
                if (parent instanceof StatementImpl) {
                    predecessor = ((StatementImpl) parent).getPrevPsiSibling();
                    while (predecessor instanceof PhpDocComment) {
                        predecessor = ((PhpDocComment) predecessor).getPrevPsiSibling();
                    }

                    successor = ((StatementImpl) parent).getNextPsiSibling();
                    while (successor instanceof PhpDocComment) {
                        successor = ((PhpDocComment) successor).getNextPsiSibling();
                    }
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
                        /* succeeding statement must not be ...->execute (bulk execution of prepared statement) */
                        if (null != successor && successor.getFirstChild() instanceof MethodReference) {
                            final MethodReference succeedingReference = (MethodReference) successor.getFirstChild();
                            final String succeedingMethod             = succeedingReference.getName();
                            if (
                                !StringUtil.isEmpty(succeedingMethod) && succeedingMethod.equals("execute") &&
                                PsiEquivalenceUtil.areElementsEquivalent(variableUsed, succeedingReference.getFirstChild())
                            ){
                                return;
                            }
                        }

                        holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }
        };
    }
}

