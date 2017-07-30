package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnnecessaryCastingInspector extends BasePhpInspection {
    private static final String message = "This type casting is not necessary, as the argument is of needed type.";

    private static final Map<IElementType, String> typesMapping = new HashMap<>();
    static {
        typesMapping.put(PhpTokenTypes.opINTEGER_CAST, Types.strInteger);
        typesMapping.put(PhpTokenTypes.opFLOAT_CAST, Types.strFloat);
        typesMapping.put(PhpTokenTypes.opBOOLEAN_CAST, Types.strBoolean);
        typesMapping.put(PhpTokenTypes.opSTRING_CAST, Types.strString);
        typesMapping.put(PhpTokenTypes.opARRAY_CAST, Types.strArray);
    }

    @NotNull
    public String getShortName() {
        return "UnnecessaryCastingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpUnaryExpression(@NotNull UnaryExpression expression) {
                final PsiElement argument   = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getValue());
                final PsiElement operation  = expression.getOperation();
                final IElementType operator = operation == null ? null : operation.getNode().getElementType();
                if (argument instanceof PhpTypedElement && typesMapping.containsKey(operator)) {
                    final PhpType resolved  = this.resolveStrictly((PhpTypedElement) argument);
                    final Set<String> types = resolved.hasUnknown() ? new HashSet<>() : resolved.getTypes();
                    if (types.size() == 1 && typesMapping.get(operator).equals(Types.getType(types.iterator().next()))) {
                        if (!(argument instanceof Variable) || !this.isWeakTypedParameter((Variable) argument)) {
                            //noinspection ConstantConditions ; at this point operation is not null
                            holder.registerProblem(
                                operation,
                                message,
                                ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                new ReplaceWithArgumentFix()
                            );
                        }
                    }
                }
            }

            @NotNull
            private PhpType resolveStrictly(@NotNull PhpTypedElement expression) {
                final Project project = holder.getProject();
                PhpType result        = PhpType.EMPTY;
                if (expression instanceof FieldReference) { /* fields has no type hints, hence private only */
                    final PsiElement resolved = ((FieldReference) expression).resolve();
                    if (resolved instanceof Field) {
                        final Field referencedField = (Field) resolved;
                        if (referencedField.getModifier().isPrivate()) {
                            result = referencedField.getType().global(project);
                        }
                    }
                } else if (expression instanceof MethodReference) { /* requires implicit return type declaration */
                    final PsiElement resolved = ((FunctionReference) expression).resolve();
                    if (resolved instanceof Function) {
                        final Function referencedFunction = (Function) resolved;
                        final PsiElement returnedType = referencedFunction.getReturnType();
                        if (returnedType != null) {
                            result = referencedFunction.getType().global(project);
                        }
                    }
                } else if (expression instanceof BinaryExpression) {
                    result = expression.getType().global(project);
                    /* workaround for https://youtrack.jetbrains.com/issue/WI-37466 */
                    if (PhpTokenTypes.tsMATH_OPS.contains(((BinaryExpression) expression).getOperationType())) {
                        PsiElement candidate = (PsiElement) expression;
                        while (candidate instanceof BinaryExpression) {
                            final BinaryExpression binary = (BinaryExpression) candidate;
                            final PsiElement right
                                    = ExpressionSemanticUtil.getExpressionTroughParenthesis(binary.getRightOperand());
                            if (right instanceof PhpTypedElement) {
                                final PhpType resolved  = ((PhpTypedElement) right).getType().global(project);
                                final Set<String> types = resolved.hasUnknown() ? new HashSet<>() : resolved.getTypes();
                                if (types.stream().map(Types::getType).collect(Collectors.toSet()).contains(Types.strFloat)) {
                                    final Set<String> patchedTypes = result.getTypes();
                                    patchedTypes.addAll(PhpType.FLOAT.getTypes());
                                    patchedTypes.removeAll(PhpType.INT.getTypes());
                                    result = new PhpType();
                                    patchedTypes.forEach(result::add);
                                    break;
                                }
                            }
                            candidate = ExpressionSemanticUtil.getExpressionTroughParenthesis(binary.getLeftOperand());
                        }
                    }
                } else {
                    result = expression.getType().global(project);
                }
                return result;
            }

            private boolean isWeakTypedParameter(@NotNull Variable variable) {
                boolean result = false;
                final Function scope = ExpressionSemanticUtil.getScope(variable);
                if (scope != null) {
                    final String variableName = variable.getName();
                    for (final Parameter parameter : scope.getParameters()) {
                        if (parameter.getName().equals(variableName)) {
                            result = parameter.getDeclaredType().isEmpty();
                            break;
                        }
                    }
                }
                return result;
            }
        };
    }

    private static class ReplaceWithArgumentFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Remove unnecessary casting";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement().getParent();
            if (expression instanceof UnaryExpression) {
                final PsiElement argument = ((UnaryExpression) expression).getValue();
                if (argument != null) {
                    expression.replace(argument.copy());
                }
            }
        }
    }
}
