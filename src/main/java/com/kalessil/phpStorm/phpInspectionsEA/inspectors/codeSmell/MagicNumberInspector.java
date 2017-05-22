package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.PhpExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.BinaryExpressionUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ElementTypeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MagicNumberInspector extends BasePhpInspection {
    private static final String message = "Magic number should be replaced by a constant.";

    @NotNull
    public String getShortName() {
        return "MagicNumberInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpExpression(final PhpExpression expression) {
                if (isNumeric(expression)) {
                    if (!(expression.getParent() instanceof PhpReturn)) {
                        return;
                    }

                    registerProblem(expression);
                }
            }

            @Override
            public void visitPhpBinaryExpression(final BinaryExpression expression) {
                if (Objects.equals(expression.getOperationType(), PhpTokenTypes.opMUL)) {
                    if (isNumeric(expression.getLeftOperand()) &&
                        !isNumberOneNegative(expression.getLeftOperand())) {
                        registerProblem(expression.getLeftOperand());
                    }

                    if (isNumeric(expression.getRightOperand()) &&
                        !isNumberOneNegative(expression.getRightOperand())) {
                        registerProblem(expression.getRightOperand());
                    }

                    return;
                }

                if (!BinaryExpressionUtil.isComparison(expression)) {
                    return;
                }

                if (isNumeric(expression.getLeftOperand()) &&
                    !isCounting(expression)) {
                    registerProblem(expression.getLeftOperand());
                }

                if (isNumeric(expression.getRightOperand()) &&
                    !isCounting(expression)) {
                    registerProblem(expression.getRightOperand());
                }
            }

            @Override
            public void visitPhpSwitch(final PhpSwitch switchStatement) {
                for (final PhpCase switchCase : switchStatement.getCases()) {
                    final PhpPsiElement caseCondition = switchCase.getCondition();

                    if (isNumeric(caseCondition)) {
                        registerProblem(caseCondition);
                    }
                }
            }

            @Override
            public void visitPhpField(final Field field) {
                if (!field.isConstant() &&
                    isNotZeroNumber(field.getDefaultValue())) {
                    registerProblem(field.getDefaultValue());
                }
            }

            @Override
            public void visitPhpParameter(final Parameter parameter) {
                if (isNotZeroNumber(parameter.getDefaultValue())) {
                    registerProblem(parameter.getDefaultValue());
                }
            }

            @Override
            public void visitPhpSelfAssignmentExpression(final SelfAssignmentExpression expression) {
                if (Objects.equals(expression.getOperationType(), PhpTokenTypes.opMUL_ASGN)) {
                    final PhpPsiElement expressionValue = expression.getValue();

                    if (isNumberOneNegative(expressionValue)) {
                        return;
                    }

                    if (isNumeric(expressionValue)) {
                        registerProblem(expressionValue);
                    }
                }
            }

            @Override
            public void visitPhpFunctionCall(final FunctionReference reference) {
                testParametersValue(reference);
            }

            @Override
            public void visitPhpMethodReference(final MethodReference reference) {
                testParametersValue(reference);
            }

            private void testParametersValue(final FunctionReference functionReference) {
                final Function function = (Function) functionReference.resolve();

                if (function == null) {
                    for (final PsiElement referenceParameter : functionReference.getParameters()) {
                        if (isNumeric(referenceParameter)) {
                            registerProblem(referenceParameter);
                        }
                    }

                    return;
                }

                final List<Parameter>  functionParameters      = new ArrayList<>(Arrays.asList(function.getParameters()));
                final List<PsiElement> referenceParameters     = new ArrayList<>(Arrays.asList(functionReference.getParameters()));
                final int              referenceParametersSize = referenceParameters.size();

                for (int parameterIndex = 0; parameterIndex < referenceParametersSize; parameterIndex++) {
                    final PsiElement referenceParameter = referenceParameters.get(parameterIndex);

                    if (isNumeric(referenceParameter)) {
                        final Parameter functionParameter = functionParameters.get(parameterIndex);

                        if (isDefaultValued(functionParameter, referenceParameter)) {
                            continue;
                        }

                        registerProblem(referenceParameter);
                    }
                }
            }

            private boolean isDefaultValued(@Nullable final Parameter functionParameter, final PsiElement referenceParameter) {
                if (functionParameter != null) {
                    PsiElement defaultOriginal = functionParameter.getDefaultValue();

                    if (defaultOriginal == null) {
                        return false;
                    }

                    while (defaultOriginal instanceof ConstantReference) {
                        final PsiElement nextReference = ((PsiReference) defaultOriginal).resolve();

                        if (nextReference instanceof Constant) {
                            defaultOriginal = ((Constant) nextReference).getValue();
                        }
                    }

                    return (defaultOriginal != null) &&
                           defaultOriginal.getText().equals(referenceParameter.getText());
                }

                return false;
            }

            private boolean isNumeric(final PsiElement expression) {
                PsiElement testingExpression = expression;

                if (testingExpression instanceof UnaryExpression) {
                    testingExpression = ((UnaryExpression) testingExpression).getValue();
                }

                return (testingExpression instanceof PhpExpressionImpl) &&
                       (testingExpression.getNode().getElementType() == PhpElementTypes.NUMBER);
            }

            private boolean isNotZeroNumber(@Nullable final PsiElement value) {
                return (value != null) &&
                       isNumeric(value) &&
                       !"0".equals(value.getText());
            }

            private boolean isNumberOneNegative(final PsiElement unaryExpression) {
                if (!(unaryExpression instanceof UnaryExpression)) {
                    return false;
                }

                final PhpPsiElement unaryValue = ((UnaryExpression) unaryExpression).getValue();

                return (unaryValue != null) &&
                       "1".equals(unaryValue.getText());
            }

            private boolean isCounting(@NotNull final BinaryExpression binaryExpression) {
                final IElementType operationType = binaryExpression.getOperationType();

                if (operationType == null) {
                    return false;
                }

                final boolean    numericOnOpposite = isNumeric(binaryExpression.getRightOperand());
                final PsiElement numericOperand    = numericOnOpposite ? binaryExpression.getRightOperand() : binaryExpression.getLeftOperand();

                if (numericOperand == null) {
                    return false;
                }

                if (operationType.equals(PhpTokenTypes.opIDENTICAL) ||
                    operationType.equals(PhpTokenTypes.opNOT_IDENTICAL) ||
                    operationType.equals(PhpTokenTypes.opEQUAL) ||
                    operationType.equals(PhpTokenTypes.opNOT_EQUAL)) {
                    return "0".equals(numericOperand.getText());
                }

                final IElementType normalizeOperationType = numericOnOpposite ? operationType : ElementTypeUtil.rotateOperation(operationType);

                return (normalizeOperationType.equals(PhpTokenTypes.opGREATER) ||
                        normalizeOperationType.equals(PhpTokenTypes.opGREATER_OR_EQUAL)) &&
                       isBinaryNumeric(numericOperand.getText());

            }

            private boolean isBinaryNumeric(@NotNull final String numericValue) {
                return "0".equals(numericValue) ||
                       "1".equals(numericValue);
            }

            private void registerProblem(final PsiElement rightOperand) {
                problemsHolder.registerProblem(rightOperand, message, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
