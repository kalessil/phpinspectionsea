package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class DefaultValueInElseBranchInspector extends BasePhpInspection {
    private static final String message = "Assignment in this branch should be moved before the if.";

    @NotNull
    public String getShortName() {
        return "DefaultValueInElseBranchInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpIf(@NotNull If ifStatement) {
                /* skip ifs without else */
                final Else elseStatement = ifStatement.getElseBranch();
                if (null == elseStatement) {
                    return;
                }


                /* collect all group statement for further analysis */
                final List<GroupStatement> groupStatementsList = new ArrayList<>();
                final List<PhpPsiElement> conditionsList       = new ArrayList<>();
                groupStatementsList.add(ExpressionSemanticUtil.getGroupStatement(ifStatement));
                conditionsList.add(ifStatement.getCondition());
                for (final ControlStatement elseIf : ifStatement.getElseIfBranches()) {
                    groupStatementsList.add(ExpressionSemanticUtil.getGroupStatement(elseIf));
                    conditionsList.add(elseIf.getCondition());
                }
                groupStatementsList.add(ExpressionSemanticUtil.getGroupStatement(elseStatement));


                /* collect assignments or stop inspecting when structure expectations are not met */
                final LinkedList<AssignmentExpression> assignmentsList = new LinkedList<>();
                for (final GroupStatement group : groupStatementsList) {
                    /* only one expression in group statement */
                    if (null == group || 1 != ExpressionSemanticUtil.countExpressionsInGroup(group)) {
                        groupStatementsList.clear();
                        conditionsList.clear();
                        return;
                    }

                    /* find assignments, take comments in account */
                    AssignmentExpression assignmentExpression = null;
                    for (final PsiElement ifChild : group.getChildren()) {
                        /* assignment expression check */
                        if (ifChild instanceof Statement && ifChild.getFirstChild() instanceof AssignmentExpression) {
                            final AssignmentExpression branchAssignment  = (AssignmentExpression) ifChild.getFirstChild();
                            final PhpPsiElement branchAssignmentVariable = branchAssignment.getVariable();
                            /* target value is variable/property */
                            if (branchAssignmentVariable instanceof Variable || branchAssignmentVariable instanceof FieldReference) {
                                assignmentExpression = branchAssignment;
                            }
                            break;
                        }
                    }
                    if (null == assignmentExpression) {
                        groupStatementsList.clear();
                        conditionsList.clear();
                        return;
                    }

                    assignmentsList.add(assignmentExpression);
                }
                groupStatementsList.clear();


                /* ensure all assignments has one subject, define the one to compare with */
                final PhpPsiElement subjectToCompareWith = assignmentsList.peekFirst().getVariable();
                if (null == subjectToCompareWith) {
                    assignmentsList.clear();
                    return;
                }
                /* now check all assignments against subject identity */
                PhpPsiElement subjectFromExpression;
                for (final AssignmentExpression subjectAssignmentExpression : assignmentsList) {
                    /* ensure target variable is discoverable */
                    subjectFromExpression = subjectAssignmentExpression.getVariable();
                    if (null == subjectFromExpression) {
                        assignmentsList.clear();
                        return;
                    }

                    /* check assignment type, work with siblings due to lack of API */
                    PsiElement operation = subjectFromExpression.getNextSibling();
                    if (operation instanceof PsiWhiteSpace) {
                        operation = operation.getNextSibling();
                    }
                    /* assignment operator check */
                    if (PhpTokenTypes.opASGN != operation.getNode().getElementType()) {
                        assignmentsList.clear();
                        return;
                    }

                    /* ensure target variables matches */
                    if (!OpeanapiEquivalenceUtil.areEqual(subjectToCompareWith, subjectFromExpression)) {
                        assignmentsList.clear();
                        return;
                    }
                }


                /* verify candidate value: array/string/number/constant */
                final PhpPsiElement candidate  = assignmentsList.getLast().getValue();
                final boolean isValidCandidate = null != candidate && isDefaultValueCandidateFits(candidate);
                if (isValidCandidate && !isContainerUsedInConditions(conditionsList, subjectToCompareWith)) {
                    /* point the problem out */
                    holder.registerProblem(elseStatement.getFirstChild(), message, new MoveDefaultValueFix());
                }
                assignmentsList.clear();
                conditionsList.clear();
            }

            private boolean isContainerUsedInConditions(@NotNull List<PhpPsiElement> conditions, @NotNull PhpPsiElement container) {
                boolean result = false;

                final Class clazz = container.getClass();
                for (final PhpPsiElement condition : conditions) {
                    if (null == condition) {
                        continue;
                    }

                    if (clazz == condition.getClass() && OpeanapiEquivalenceUtil.areEqual(condition, container)) {
                        result = true;
                        break;
                    }

                    for (final PsiElement matchCandidate : PsiTreeUtil.findChildrenOfType(condition, container.getClass())) {
                        if (OpeanapiEquivalenceUtil.areEqual(matchCandidate, container)) {
                            result = true;
                            break;
                        }
                    }
                    if (result) {
                        break;
                    }
                }

                return result;
            }

            private boolean isDefaultValueCandidateFits(@NotNull PhpPsiElement candidate) {
                /* quick check on expression type basis */
                if (
                    candidate instanceof StringLiteralExpression ||
                    candidate instanceof ArrayCreationExpression ||
                    candidate instanceof ConstantReference ||
                    candidate instanceof Variable
                ) {
                    return true;
                }

                /* numbers check needs additional checks */
                //noinspection RedundantIfStatement
                if (OpenapiTypesUtil.isNumber(candidate)) {
                    return true;
                }

                return false;
            }
        };
    }

    private static class MoveDefaultValueFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Place default value before if";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression != null && !project.isDisposed()) {
                final PsiElement elseStatement = expression.getParent();
                final PsiElement ifStatement   = elseStatement.getParent();
                final GroupStatement elseBody  = ExpressionSemanticUtil.getGroupStatement(elseStatement);
                if (elseBody != null) {
                    final PsiElement assignment = elseBody.getFirstPsiChild();
                    if (assignment != null) {
                        ifStatement.getParent().addBefore(assignment, ifStatement);
                        elseStatement.delete();
                    }
                }
            }
        }
    }
}