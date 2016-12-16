package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AmbiguousMethodsCallsInArrayMappingInspector extends BasePhpInspection {
    private static final String message = "Duplicated method calls should be moved to local variable";

    @NotNull
    public String getShortName() {
        return "AmbiguousMethodsCallsInArrayMappingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpForeach(ForeachStatement foreach) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(foreach);
                if (null != body) {
                    /* do net search with utils, as we can have nested loops */
                    for (PsiElement expression : body.getStatements()) {
                        final PsiElement assignCandidate = expression.getFirstChild();
                        if (assignCandidate instanceof AssignmentExpression) {
                            this.isStatementMatchesInspection((AssignmentExpression) assignCandidate);
                        }
                    }
                }
            }

            private void isStatementMatchesInspection(@NotNull AssignmentExpression assignment) {
                /* verify basic structure */
                final PsiElement value    = assignment.getValue();
                final PsiElement variable = assignment.getVariable();
                if (null == value || !(variable instanceof ArrayAccessExpression)) {
                    return;
                }

                /* verify if both parts contains calls */
                final Collection<PsiElement> varCalls = PsiTreeUtil.findChildrenOfType(variable, FunctionReference.class);
                if (0 == varCalls.size()) {
                    return;
                }
                final Collection<PsiElement> valCalls = PsiTreeUtil.findChildrenOfType(value, FunctionReference.class);
                if (0 == valCalls.size()) {
                    if (value instanceof FunctionReference) {
                        valCalls.add(value);
                    } else {
                        return;
                    }
                }

                /* iterate over calls in the value, match them with calls in the variable */
                for (PsiElement inValue : valCalls) {
                    for (PsiElement inVariable : varCalls) {
                        if (PsiEquivalenceUtil.areElementsEquivalent(inValue, inVariable)) {
                            /* report an issue and continue with outer loop */
                            holder.registerProblem(inValue, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            break;
                        }
                    }
                }
                valCalls.clear();
                varCalls.clear();
            }
        };
    }
}
