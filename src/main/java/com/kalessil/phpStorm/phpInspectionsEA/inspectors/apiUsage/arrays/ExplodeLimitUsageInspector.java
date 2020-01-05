package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ExplodeLimitUsageInspector extends PhpInspection {
    private static final String messagePositiveLimitPattern = "'%s' could be used here (only some parts has been used).";
    private static final String messageNegativeLimitPattern = "'%s' could be used here (following 'array_pop(...)' call to be dropped then).";

    @NotNull
    @Override
    public String getShortName() {
        return "ExplodeLimitUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'explode(...)' limit can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("explode")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length >= 2) {
                        if (this.canApplyNegativeLimit(reference) && this.isFromRootNamespace(reference)) {
                            final boolean canCalculateLimit = arguments.length == 2 || OpenapiTypesUtil.isNumber(arguments[2]);
                            if (canCalculateLimit) {
                                int limit;
                                try {
                                    limit = arguments.length == 3 ? Integer.parseInt(arguments[2].getText()) : 0;
                                } catch (final NumberFormatException wrongFormat) {
                                    limit = Integer.MAX_VALUE;
                                }
                                if (limit <= 0) {
                                    final String replacement = String.format(
                                            "%sexplode(%s, %s, %s)",
                                            reference.getImmediateNamespaceName(),
                                            arguments[0].getText(),
                                            arguments[1].getText(),
                                            limit - 1
                                    );
                                    holder.registerProblem(
                                            reference,
                                            String.format(ReportingUtil.wrapReportedMessage(messageNegativeLimitPattern), replacement),
                                            new AddPNegativeLimitArgumentFixer(replacement)
                                    );
                                }
                            }
                        } else if (arguments.length == 2 && this.canApplyPositiveLimit(reference) && this.isFromRootNamespace(reference)) {
                            final String replacement = String.format(
                                    "%sexplode(%s, %s, 2)",
                                    reference.getImmediateNamespaceName(),
                                    arguments[0].getText(),
                                    arguments[1].getText()
                            );
                            holder.registerProblem(
                                    reference,
                                    String.format(ReportingUtil.wrapReportedMessage(messagePositiveLimitPattern), replacement),
                                    new AddPositiveLimitArgumentFixer(replacement)
                            );
                        }
                    }
                }
            }

            private boolean canApplyNegativeLimit(@NotNull PsiElement expression) {
                final PsiElement parent = expression.getParent();
                if (OpenapiTypesUtil.isAssignment(parent)) {
                    final PsiElement grandParent = parent.getParent();
                    if (OpenapiTypesUtil.isStatementImpl(grandParent)) {
                        final PsiElement next = ((PhpPsiElement) grandParent).getNextPsiSibling();
                        if (OpenapiTypesUtil.isStatementImpl(next)) {
                            final PsiElement callCandidate = next.getFirstChild();
                            if (OpenapiTypesUtil.isFunctionReference(callCandidate)) {
                                final FunctionReference reference = (FunctionReference) callCandidate;
                                final String functionName         = reference.getName();
                                if (functionName != null && functionName.equals("array_pop")) {
                                    final PsiElement[] callArguments = reference.getParameters();
                                    if (callArguments.length == 1) {
                                        final PsiElement container = ((AssignmentExpression) parent).getVariable();
                                        final PsiElement match     = callArguments[0];
                                        return container != null && match != null && OpenapiEquivalenceUtil.areEqual(container, match);
                                    }
                                }
                            }
                        }
                    }
                }

                return false;
            }

            private boolean canApplyPositiveLimit(@NotNull PsiElement expression) {
                final PsiElement parent = expression.getParent();
                if (parent instanceof ParameterList) {
                    final PsiElement grandParent = parent.getParent();
                    if (OpenapiTypesUtil.isFunctionReference(grandParent)) {
                        final String functionName = ((FunctionReference) grandParent).getName();
                        return functionName != null && functionName.equals("current");
                    }
                } else if (parent instanceof ArrayAccessExpression) {
                    final ArrayIndex index = ((ArrayAccessExpression) parent).getIndex();
                    if (index != null) {
                        final PsiElement indexValue = index.getValue();
                        return indexValue != null && OpenapiTypesUtil.isNumber(indexValue) && indexValue.getText().equals("0");
                    }
                } else if (OpenapiTypesUtil.isAssignment(parent)) {
                    final PsiElement container = ((AssignmentExpression) parent).getVariable();
                    if (container instanceof Variable) {
                        final Function scope = ExpressionSemanticUtil.getScope(expression);
                        if (scope != null) {
                            final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(scope);
                            if (body != null) {
                                final Variable variable      = (Variable) container;
                                final String variableName    = variable.getName();
                                boolean reachedStartingPoint = false;
                                boolean result               = false;
                                for (final Variable match : PsiTreeUtil.findChildrenOfType(body, Variable.class)) {
                                    reachedStartingPoint = reachedStartingPoint || match == variable;
                                    if (reachedStartingPoint && match != variable && variableName.equals(match.getName())) {
                                        final PsiElement context = match.getParent();
                                        if (!(result = context instanceof ArrayAccessExpression && this.canApplyPositiveLimit(match))) {
                                            break;
                                        }
                                    }
                                }
                                return result;
                            }
                        }
                    }
                } else if (OpenapiTypesUtil.isPhpExpressionImpl(parent)) {
                    final PsiElement grandParent = parent.getParent();
                    if (grandParent instanceof MultiassignmentExpression) {
                        final MultiassignmentExpression assignment = (MultiassignmentExpression) grandParent;
                        if (assignment.getVariables().size() == 1) {
                            int commasCount = 0;
                            PsiElement child = assignment.getFirstChild();
                            while (child != null && !OpenapiTypesUtil.is(child, PhpTokenTypes.opASGN)) {
                                if (OpenapiTypesUtil.is(child, PhpTokenTypes.opCOMMA) && ++commasCount > 1) {
                                    break;
                                }
                                child = child.getNextSibling();
                            }
                            return commasCount < 2;
                        }
                    }
                }

                return false;
            }
        };
    }

    private static final class AddPositiveLimitArgumentFixer extends UseSuggestedReplacementFixer {
        private static final String title = "Add limit to 'explode(...)' call";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        AddPositiveLimitArgumentFixer(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class AddPNegativeLimitArgumentFixer implements LocalQuickFix {
        private static final String title = "Add limit to 'explode(...)' and drop 'array_pop(...)' call";

        final private String expression;

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        AddPNegativeLimitArgumentFixer(@NotNull String expression) {
            super();
            this.expression = expression;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression != null && !project.isDisposed()) {
                final PsiElement replacement = PhpPsiElementFactory
                        .createPhpPsiFromText(project, ParenthesizedExpression.class, '(' + this.expression + ')')
                        .getArgument();
                if (replacement != null) {
                    final PhpPsiElement statementCandidate = (PhpPsiElement) expression.getParent().getParent();
                    if (OpenapiTypesUtil.isStatementImpl(statementCandidate)) {
                        final PsiElement next = statementCandidate.getNextPsiSibling();
                        if (next != null) {
                            next.delete();
                            expression.replace(replacement);
                        }
                    }
                }
            }
        }
    }
}
