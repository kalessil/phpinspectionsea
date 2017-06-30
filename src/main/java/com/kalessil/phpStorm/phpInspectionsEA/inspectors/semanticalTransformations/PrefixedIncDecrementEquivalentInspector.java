package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPlatformResolverUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class PrefixedIncDecrementEquivalentInspector extends BasePhpInspection {
    private static final String patternIncrementEquivalent = "Can be safely replaced with '++%s%'.";
    private static final String patternDecrementEquivalent = "Can be safely replaced with '--%s%'.";

    @NotNull
    public String getShortName() {
        return "PrefixedIncDecrementEquivalentInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* ensures we are not touching arrays only, not strings and not objects */
            private boolean isArrayAccessOrString(@Nullable PhpPsiElement variable) {
                if (variable instanceof ArrayAccessExpression) {
                    final HashSet<String> containerTypes = new HashSet<>();
                    TypeFromPlatformResolverUtil.resolveExpressionType(((ArrayAccessExpression) variable).getValue(), containerTypes);
                    boolean isArray = !containerTypes.contains(Types.strString) && containerTypes.contains(Types.strArray);

                    containerTypes.clear();
                    return !isArray;
                }

                return false;
            }

            /** self assignments */
            public void visitPhpSelfAssignmentExpression(SelfAssignmentExpression expression) {
                final IElementType operation = expression.getOperationType();
                final PhpPsiElement value    = expression.getValue();
                final PhpPsiElement variable = expression.getVariable();
                if (null != value && null != operation && null != variable) {
                    if (operation == PhpTokenTypes.opPLUS_ASGN) {
                        if (value.getText().equals("1") && !isArrayAccessOrString(variable)) {
                            final String message = patternIncrementEquivalent.replace("%s%", variable.getText());
                            holder.registerProblem(expression, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(operation));
                        }

                        return;
                    }

                    if (operation == PhpTokenTypes.opMINUS_ASGN) {
                        if (value.getText().equals("1") && !isArrayAccessOrString(variable)) {
                            final String message = patternDecrementEquivalent.replace("%s%", variable.getText());
                            holder.registerProblem(expression, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(operation));
                        }
                        //return;
                    }
                }
            }

            /** assignments expressions inspection*/
            public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {
                final PhpPsiElement variable = assignmentExpression.getVariable();
                if (null != variable && assignmentExpression.getValue() instanceof BinaryExpression) {
                    final BinaryExpression value = (BinaryExpression) assignmentExpression.getValue();

                    /* operation and operands provided */
                    final PsiElement leftOperand  = value.getLeftOperand();
                    final PsiElement rightOperand = value.getRightOperand();
                    final IElementType operation  = value.getOperationType();
                    if (null == leftOperand || null == rightOperand || null == operation) {
                        return;
                    }

                    if (operation == PhpTokenTypes.opPLUS) {
                        /* plus operation: operand position NOT important */
                        if (
                            (leftOperand.getText().equals("1") && PsiEquivalenceUtil.areElementsEquivalent(rightOperand, variable)) ||
                            (rightOperand.getText().equals("1") && PsiEquivalenceUtil.areElementsEquivalent(leftOperand, variable))
                        ) {
                            if (!isArrayAccessOrString(assignmentExpression.getVariable())) {
                                final String message = patternIncrementEquivalent.replace("%s%", variable.getText());
                                holder.registerProblem(assignmentExpression, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(operation));
                            }
                        }

                        return;
                    }

                    if (operation == PhpTokenTypes.opMINUS) {
                        /* minus operation: operand position IS important */
                        if (
                            rightOperand.getText().equals("1") &&
                            PsiEquivalenceUtil.areElementsEquivalent(leftOperand, variable) &&
                            !isArrayAccessOrString(assignmentExpression.getVariable())
                        ) {
                            final String message = patternDecrementEquivalent.replace("%s%", variable.getText());
                            holder.registerProblem(assignmentExpression, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(operation));
                        }

                        //return;
                    }
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @Nullable final String operation;

        @NotNull
        @Override
        public String getName() {
            return "Use suggested replacement";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        public TheLocalFix (@NotNull IElementType operation) {
            super();

            if (PhpTokenTypes.opMINUS == operation || PhpTokenTypes.opMINUS_ASGN == operation) {
                this.operation = "--";
                return;
            }
            if (PhpTokenTypes.opPLUS == operation || PhpTokenTypes.opPLUS_ASGN == operation) {
                this.operation = "++";
                return;
            }
            this.operation = null;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement assignment = descriptor.getPsiElement();
            if (null != this.operation && assignment instanceof AssignmentExpression) {
                final PsiElement variable = ((AssignmentExpression) assignment).getVariable();
                if (null != variable) {
                    final String pattern         = this.operation + variable.getText();
                    final PsiElement replacement = PhpPsiElementFactory.createFromText(project, UnaryExpression.class, pattern);
                    if (null != replacement) {
                        assignment.replace(replacement);
                    }
                }
            }
        }
    }
}
