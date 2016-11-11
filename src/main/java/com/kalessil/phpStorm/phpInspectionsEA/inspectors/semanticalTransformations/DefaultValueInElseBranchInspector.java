package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class DefaultValueInElseBranchInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Assignment in this branch shall be moved before if";

    @NotNull
    public String getShortName() {
        return "DefaultValueInElseBranchInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {
                /* skip ifs without else */
                Else objElseStatement = ifStatement.getElseBranch();
                if (null == objElseStatement) {
                    return;
                }


                /* collect all group statement for further analysis */
                LinkedList<GroupStatement> objGroupStatementsList = new LinkedList<GroupStatement>();
                objGroupStatementsList.add(ExpressionSemanticUtil.getGroupStatement(ifStatement));
                for (ControlStatement objElseIf : ifStatement.getElseIfBranches()) {
                    objGroupStatementsList.add(ExpressionSemanticUtil.getGroupStatement(objElseIf));
                }
                objGroupStatementsList.add(ExpressionSemanticUtil.getGroupStatement(objElseStatement));


                /* collect assignments or stop inspecting when structure expectations are not met */
                LinkedList<AssignmentExpression> objAssignmentsList = new LinkedList<AssignmentExpression>();
                for (GroupStatement objGroup : objGroupStatementsList) {
                    /* only one expression in group statement */
                    if (null == objGroup || 1 != ExpressionSemanticUtil.countExpressionsInGroup(objGroup)) {
                        objGroupStatementsList.clear();
                        return;
                    }

                    /* find assignments, take comments in account */
                    AssignmentExpression objAssignmentExpression = null;
                    for (PsiElement objIfChild : objGroup.getChildren()) {
                        /* assignment expression check */
                        if (objIfChild instanceof Statement && objIfChild.getFirstChild() instanceof AssignmentExpression) {
                            AssignmentExpression branchAssignment  = (AssignmentExpression) objIfChild.getFirstChild();
                            PhpPsiElement branchAssignmentVariable = branchAssignment.getVariable();
                            /* target value is variable/property */
                            if (branchAssignmentVariable instanceof Variable || branchAssignmentVariable instanceof FieldReference) {
                                objAssignmentExpression = branchAssignment;
                            }
                            break;
                        }
                    }
                    if (null == objAssignmentExpression) {
                        objGroupStatementsList.clear();
                        return;
                    }

                    objAssignmentsList.add(objAssignmentExpression);
                }
                objGroupStatementsList.clear();


                /* ensure all assignments has one subject, define the one to compare with */
                PhpPsiElement objSubjectToCompareWith = objAssignmentsList.peekFirst().getVariable();
                if (null == objSubjectToCompareWith) {
                    objAssignmentsList.clear();
                    return;
                }
                /* now check all assignments against subject identity */
                PhpPsiElement objSubjectFromExpression;
                for (AssignmentExpression objSubjectAssignmentExpression : objAssignmentsList) {
                    /* ensure target variable is discoverable */
                    objSubjectFromExpression = objSubjectAssignmentExpression.getVariable();
                    if (null == objSubjectFromExpression) {
                        objAssignmentsList.clear();
                        return;
                    }

                    /* check assignment type, work with siblings due to lack of API */
                    PsiElement objOperation = objSubjectFromExpression.getNextSibling();
                    if (objOperation instanceof PsiWhiteSpace) {
                        objOperation = objOperation.getNextSibling();
                    }
                    /* assignment operator check */
                    if (PhpTokenTypes.opASGN != objOperation.getNode().getElementType()) {
                        objAssignmentsList.clear();
                        return;
                    }

                    /* ensure target variables matches */
                    if (!PsiEquivalenceUtil.areElementsEquivalent(objSubjectToCompareWith, objSubjectFromExpression)) {
                        objAssignmentsList.clear();
                        return;
                    }
                }


                /* verify candidate value: array/string/number/constant */
                PhpPsiElement objCandidate = objAssignmentsList.getLast().getValue();
                objAssignmentsList.clear();
                if (this.isDefaultValueCandidateFits(objCandidate)) {
                    /* point the problem out */
                    holder.registerProblem(objElseStatement.getFirstChild(), strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                }
            }

            private boolean isDefaultValueCandidateFits(PhpPsiElement objCandidate) {
                /* quick check on expression type basis */
                if (
                    objCandidate instanceof StringLiteralExpression ||
                    objCandidate instanceof ArrayCreationExpression ||
                    objCandidate instanceof ConstantReference ||
                    objCandidate instanceof Variable
                ) {
                    return true;
                }

                /* numbers check needs additional checks */
                //noinspection RedundantIfStatement
                if (objCandidate instanceof PhpExpression && PhpElementTypes.NUMBER == objCandidate.getNode().getElementType()) {
                    return true;
                }

                return false;
            }
        };
    }
}