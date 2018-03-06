package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class OneTimeUseVariablesInspector extends BasePhpInspection {
    // Inspection options.
    public boolean ALLOW_LONG_STATEMENTS = false;

    private static final String messagePattern = "Variable $%v% is redundant.";

    @NotNull
    public String getShortName() {
        return "OneTimeUseVariablesInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            void checkOneTimeUse(@NotNull PhpPsiElement construct, @NotNull Variable argument) {
                final String variableName = argument.getName();
                final PsiElement previous = construct.getPrevPsiSibling();
                /* verify preceding expression (assignment needed) */
                if (null != previous && OpenapiTypesUtil.isAssignment(previous.getFirstChild())) {
                    final AssignmentExpression assign = (AssignmentExpression) previous.getFirstChild();

                    /* ensure variables are the same */
                    final PhpPsiElement assignVariable = assign.getVariable();
                    final PsiElement assignValue       = ExpressionSemanticUtil.getExpressionTroughParenthesis(assign.getValue());
                    if (null != assignValue && assignVariable instanceof Variable) {
                        final String assignVariableName = assignVariable.getName();
                        if (assignVariableName == null || !assignVariableName.equals(variableName)) {
                            return;
                        }

                        /* check if variable as a function/use(...) parameter by reference */
                        final Function function = ExpressionSemanticUtil.getScope(construct);
                        if (function != null) {
                            final boolean isReference =
                                    this.isArgumentReference(argument, function) ||
                                    this.isBoundReference(argument, function);
                            if (isReference) {
                                return;
                            }
                        }

                        /* too long return/throw statements can be decoupled as a variable */
                        if (!ALLOW_LONG_STATEMENTS && assign.getText().length() > 80) {
                            return;
                        }

                        /* heavy part, find usage inside function/method to analyze multiple writes */
                        final PhpScopeHolder parentScope = ExpressionSemanticUtil.getScope(assign);
                        if (null != parentScope) {
                            final PhpEntryPointInstruction entryPoint   = parentScope.getControlFlow().getEntryPoint();
                            final PhpAccessVariableInstruction[] usages = PhpControlFlowUtil.getFollowingVariableAccessInstructions(entryPoint, variableName, false);

                            int countWrites = 0;
                            int countReads  = 0;
                            for (PhpAccessVariableInstruction oneCase: usages) {
                                final boolean isWrite = oneCase.getAccess().isWrite();

                                countWrites += isWrite ? 1 : 0;
                                countReads  += isWrite ? 0 : 1;
                                if (countWrites > 1 || countReads > 1) {
                                    return;
                                }
                            }
                        }

                        final String message    = messagePattern.replace("%v%", variableName);
                        final TheLocalFix fixer = new TheLocalFix(assign.getParent(), argument, assignValue);
                        holder.registerProblem(assignVariable, message, fixer);
                    }
                }
            }

            @Override
            public void visitPhpReturn(@NotNull PhpReturn returnStatement) {
                /* if function returning reference, do not inspect returns */
                final Function callable   = ExpressionSemanticUtil.getScope(returnStatement);
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(callable);
                if (null != callable && null != nameNode) {
                    /* is defined like returning reference */
                    PsiElement referenceCandidate = nameNode.getPrevSibling();
                    if (referenceCandidate instanceof PsiWhiteSpace) {
                        referenceCandidate = referenceCandidate.getPrevSibling();
                    }
                    if (OpenapiTypesUtil.is(referenceCandidate, PhpTokenTypes.opBIT_AND)) {
                        return;
                    }
                }

                /* regular function, check one-time use variables */
                final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(returnStatement.getArgument());
                if (argument instanceof PhpPsiElement) {
                    final Variable variable = this.getVariable(argument);
                    if (null != variable) {
                        checkOneTimeUse(returnStatement, variable);
                    }
                }
            }

            @Override
            public void visitPhpMultiassignmentExpression(@NotNull MultiassignmentExpression expression) {
                final PsiElement value = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getValue());
                if (value != null) {
                    final Variable variable = this.getVariable(value);
                    final PsiElement parent = expression.getParent();
                    if (variable != null && OpenapiTypesUtil.isStatementImpl(parent)) {
                        final PsiElement first = expression.getFirstChild();
                        final boolean isTarget =
                                OpenapiTypesUtil.is(first, PhpTokenTypes.kwLIST) ||
                                OpenapiTypesUtil.is(first, PhpTokenTypes.chLBRACKET);
                        if (isTarget) {
                            this.checkOneTimeUse((PhpPsiElement) parent, variable);
                        }
                    }
                }
            }

            /* TODO: once got bored, add foreach source pattern here =) I'm naive but nevertheless ^_^ */

            @Override
            public void visitPhpThrow(@NotNull PhpThrow expression) {
                final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getArgument());
                if (argument instanceof PhpPsiElement) {
                    final Variable variable = this.getVariable((PhpPsiElement) argument);
                    if (variable != null) {
                        this.checkOneTimeUse(expression, variable);
                    }
                }
            }

            @Nullable
            private Variable getVariable(@NotNull PsiElement expression) {
                Variable result = null;
                if (expression instanceof Variable) {
                    result = (Variable) expression;
                } else if (expression instanceof FieldReference) {
                    final PsiElement candidate = expression.getFirstChild();
                    if (candidate instanceof Variable) {
                        result = (Variable) candidate;
                    }
                } else if (OpenapiTypesUtil.isPhpExpressionImpl(expression)) {
                    /* instanceof passes child classes as well, what isn't correct */
                    final PsiElement candidate = expression.getFirstChild();
                    if (candidate != null) {
                        result = this.getVariable(candidate);
                    }
                }
                return result;
            }

            private boolean isArgumentReference(@NotNull Variable variable, @NotNull Function function) {
                boolean result            = false;
                final String variableName = variable.getName();
                for (final Parameter parameter : function.getParameters()) {
                    if (parameter.getName().equals(variableName) && parameter.isPassByRef()) {
                        result = true;
                        break;
                    }
                }
                return result;

            }

            private boolean isBoundReference(@NotNull Variable variable, @NotNull Function function) {
                boolean result            = false;
                final List<Variable> used = ExpressionSemanticUtil.getUseListVariables(function);
                if (used != null) {
                    final String variableName      = variable.getName();
                    final Optional<Variable> match = used.stream().filter(v -> v.getName().equals(variableName)).findFirst();
                    if (match.isPresent()) {
                        final PsiElement previous  = match.get().getPrevSibling();
                        final PsiElement candidate = previous instanceof PsiWhiteSpace ? previous.getPrevSibling() : previous;
                        result                     = OpenapiTypesUtil.is(candidate, PhpTokenTypes.opBIT_AND);
                    }
                    used.clear();
                }
                return result;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addCheckbox("Allow long statements (80+ chars)", ALLOW_LONG_STATEMENTS, (isSelected) -> ALLOW_LONG_STATEMENTS = isSelected)
        );
    }

    private static class TheLocalFix implements LocalQuickFix {
        private final SmartPsiElementPointer<PsiElement> assignment;
        private final SmartPsiElementPointer<PsiElement> value;
        private final SmartPsiElementPointer<Variable> variable;

        TheLocalFix(@NotNull PsiElement assignment, @NotNull Variable variable, @NotNull PsiElement value) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(value.getProject());

            this.assignment = factory.createSmartPsiElementPointer(assignment);
            this.variable   = factory.createSmartPsiElementPointer(variable);
            this.value      = factory.createSmartPsiElementPointer(value);
        }

        @NotNull
        @Override
        public String getName() {
            return "Inline value";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement assignment = this.assignment.getElement();
            final Variable variable     = this.variable.getElement();
            final PsiElement value      = this.value.getElement();
            if (null == assignment || null == variable || null == value || project.isDisposed()) {
                return;
            }

            /* delete preceding PhpDoc */
            final PhpPsiElement previous = ((Statement) assignment).getPrevPsiSibling();
            if (previous instanceof PhpDocComment) {
                previous.delete();
            }

            /* delete space after the method */
            PsiElement nextExpression = assignment.getNextSibling();
            if (nextExpression instanceof PsiWhiteSpace) {
                nextExpression.delete();
            }

            /* inline the value */
            boolean wrap = false;
            if (value instanceof NewExpression) {
                wrap = true;
            } else if (value instanceof UnaryExpression) {
                wrap = OpenapiTypesUtil.is(((UnaryExpression) value).getOperation(), PhpTokenTypes.kwCLONE);
            }

            if (wrap && variable.getParent() instanceof MemberReference) {
                final String wrappedPattern = '(' + value.getText() + ')';
                final ParenthesizedExpression wrapped
                    = PhpPsiElementFactory.createPhpPsiFromText(project, ParenthesizedExpression.class, wrappedPattern);
                variable.replace(wrapped);
            } else {
                variable.replace(value);
            }

            /* delete assignment itself */
            assignment.delete();
        }
    }
}

