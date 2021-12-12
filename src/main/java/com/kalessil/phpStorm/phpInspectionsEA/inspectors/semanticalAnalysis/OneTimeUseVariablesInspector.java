package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocVariable;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.elements.PhpThrowExpression;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

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
    public boolean ALLOW_LONG_STATEMENTS       = false;
    public boolean ANALYZE_RETURN_STATEMENTS   = true;
    public boolean ANALYZE_THROW_STATEMENTS    = true;
    public boolean ANALYZE_ARRAY_DESTRUCTURING = true;

    private static final String messagePattern = "Variable $%v% is redundant.";

    @NotNull
    @Override
    public String getShortName() {
        return "OneTimeUseVariablesInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "One-time use variables";
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
                        if (null != function) {
                            for (final Parameter param: function.getParameters()) {
                                if (param.isPassByRef() && param.getName().equals(variableName)) {
                                    return;
                                }
                            }

                            final List<Variable> useList = ExpressionSemanticUtil.getUseListVariables(function);
                            if (useList != null && ! useList.isEmpty()) {
                                for (final Variable param: useList) {
                                    if (param.getName().equals(variableName)) {
                                        /* detect parameters by reference in use clause */
                                        PsiElement ampersandCandidate = param.getPrevSibling();
                                        if (ampersandCandidate instanceof PsiWhiteSpace) {
                                            ampersandCandidate = ampersandCandidate.getPrevSibling();
                                        }
                                        if (OpenapiTypesUtil.is(ampersandCandidate, PhpTokenTypes.opBIT_AND)) {
                                            return;
                                        }
                                    }
                                }
                                useList.clear();
                            }
                        }

                        /* too long return/throw statements can be decoupled as a variable */
                        if (!ALLOW_LONG_STATEMENTS && assign.getText().length() > 80) {
                            return;
                        }

                        /* heavy part, find usage inside function/method to analyze multiple writes */
                        final PhpScopeHolder parentScope = ExpressionSemanticUtil.getScope(assign);
                        if (null != parentScope) {
                            int countWrites = 0;
                            int countReads  = 0;
                            for (final PhpAccessVariableInstruction oneCase: OpenapiControlFlowUtil.getFollowingVariableAccessInstructions(parentScope.getControlFlow().getEntryPoint(), variableName)) {
                                final boolean isWrite = oneCase.getAccess().isWrite();
                                if (isWrite) {
                                    /* false-positives: type specification */
                                    final PsiElement context = oneCase.getAnchor().getParent();
                                    if (OpenapiTypesUtil.isAssignment(context)) {
                                        final boolean typeAnnotated = this.isTypeAnnotated((PhpPsiElement) context.getParent(), variableName);
                                        if (typeAnnotated) {
                                            return;
                                        }
                                    }
                                }

                                countWrites += isWrite ? 1 : 0;
                                countReads  += isWrite ? 0 : 1;
                                if (countWrites > 1 || countReads > 1) {
                                    return;
                                }
                            }
                        }

                        if (!(assignValue instanceof NewExpression) || PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP540)) {
                            holder.registerProblem(
                                    assignVariable,
                                    MessagesPresentationUtil.prefixWithEa(messagePattern.replace("%v%", variableName)),
                                    new TheLocalFix(holder.getProject(), assign.getParent(), argument, assignValue)
                            );
                        }
                    }
                }
            }

            @Override
            public void visitPhpReturn(@NotNull PhpReturn returnStatement) {
                if (ANALYZE_RETURN_STATEMENTS) {
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
                        final Variable variable = this.getVariable((PhpPsiElement) argument);
                        if (null != variable) {
                            checkOneTimeUse(returnStatement, variable);
                        }
                    }
                }
            }

            @Override
            public void visitPhpMultiassignmentExpression(@NotNull MultiassignmentExpression multiassignmentExpression) {
                if (ANALYZE_ARRAY_DESTRUCTURING) {
                    final PsiElement firstChild = multiassignmentExpression.getFirstChild();
                    final IElementType nodeType = null == firstChild ? null : firstChild.getNode().getElementType();
                    if (null != nodeType && (PhpTokenTypes.kwLIST == nodeType || PhpTokenTypes.chLBRACKET == nodeType)) {
                        final Variable variable = this.getVariable(multiassignmentExpression.getValue());
                        final PsiElement parent = multiassignmentExpression.getParent();
                        if (null != variable && OpenapiTypesUtil.isStatementImpl(parent)) {
                            checkOneTimeUse((PhpPsiElement) parent, variable);
                        }
                    }
                }
            }

            /* TODO: once got bored, add foreach source pattern here =) I'm naive but nevertheless ^_^ */

            @Override
            public void visitPhpThrowExpression(@NotNull PhpThrowExpression expression) {
                if (ANALYZE_THROW_STATEMENTS) {
                    final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getArgument());
                    if (argument instanceof PhpPsiElement) {
                        final Variable variable = this.getVariable((PhpPsiElement) argument);
                        if (null != variable) {
                            checkOneTimeUse(expression.getExpression(), variable);
                        }
                    }
                }
            }

            @Nullable
            private Variable getVariable(@Nullable PhpPsiElement expression) {
                if (expression != null) {
                    if (expression instanceof Variable) {
                        return (Variable) expression;
                    } else if (expression instanceof MemberReference) {
                        final MemberReference reference = (MemberReference) expression;
                        final PsiElement candidate      = reference.getFirstChild();
                        if (candidate instanceof Variable) {
                            final PsiElement operator = OpenapiPsiSearchUtil.findResolutionOperator(reference);
                            if (OpenapiTypesUtil.is(operator, PhpTokenTypes.ARROW)) {
                                return (Variable) candidate;
                            }
                        }
                    } else if (OpenapiTypesUtil.isPhpExpressionImpl(expression)) {
                        /* instanceof passes child classes as well, what isn't correct */
                        return getVariable(expression.getFirstPsiChild());
                    }
                }

                return null;
            }

            private boolean isTypeAnnotated(@NotNull PhpPsiElement current, @NotNull String variableName) {
                boolean result = false;
                final PsiElement phpdocCandidate = current.getPrevPsiSibling();
                if (phpdocCandidate instanceof PhpDocComment) {
                    final PhpDocTag[] hints = ((PhpDocComment) phpdocCandidate).getTagElementsByName("@var");
                    if (hints.length == 1) {
                        final PhpDocVariable specifiedVariable = PsiTreeUtil.findChildOfType(hints[0], PhpDocVariable.class);
                        result = specifiedVariable != null && specifiedVariable.getName().equals(variableName);
                    }
                }
                return result;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Allow long statements (80+ chars)", ALLOW_LONG_STATEMENTS, (isSelected) -> ALLOW_LONG_STATEMENTS = isSelected);
            component.addCheckbox("Analyze return statements", ANALYZE_RETURN_STATEMENTS, (isSelected) -> ANALYZE_RETURN_STATEMENTS = isSelected);
            component.addCheckbox("Analyze throw statements", ANALYZE_THROW_STATEMENTS, (isSelected) -> ANALYZE_THROW_STATEMENTS = isSelected);
            component.addCheckbox("Analyze array destructuring", ANALYZE_ARRAY_DESTRUCTURING, (isSelected) -> ANALYZE_ARRAY_DESTRUCTURING = isSelected);
        });
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Inline value";

        private final SmartPsiElementPointer<PsiElement> assignment;
        private final SmartPsiElementPointer<PsiElement> value;
        private final SmartPsiElementPointer<Variable> variable;

        TheLocalFix(@NotNull Project project, @NotNull PsiElement assignment, @NotNull Variable variable, @NotNull PsiElement value) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(project);

            this.assignment = factory.createSmartPsiElementPointer(assignment);
            this.variable   = factory.createSmartPsiElementPointer(variable);
            this.value      = factory.createSmartPsiElementPointer(value);
        }

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
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
            if (value instanceof NewExpression || value instanceof TernaryExpression) {
                wrap = true;
            } else if (value instanceof UnaryExpression) {
                wrap = OpenapiTypesUtil.is(((UnaryExpression) value).getOperation(), PhpTokenTypes.kwCLONE);
            } else if (value instanceof BinaryExpression) {
                wrap = ((BinaryExpression) value).getOperationType() == PhpTokenTypes.opCOALESCE;
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

