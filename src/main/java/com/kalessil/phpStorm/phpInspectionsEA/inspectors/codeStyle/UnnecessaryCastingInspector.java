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
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnnecessaryCastingInspector extends BasePhpInspection {
    private static final String messageGeneric     = "This type casting is not necessary, as the argument is of needed type.";
    private static final String messageConcatenate = "This type casting is not necessary, as concatenation casts the argument.";

    private static final Map<IElementType, String> typesMapping = new HashMap<>();
    static {
        typesMapping.put(PhpTokenTypes.opINTEGER_CAST, Types.strInteger);
        typesMapping.put(PhpTokenTypes.opFLOAT_CAST,   Types.strFloat);
        typesMapping.put(PhpTokenTypes.opBOOLEAN_CAST, Types.strBoolean);
        typesMapping.put(PhpTokenTypes.opSTRING_CAST,  Types.strString);
        typesMapping.put(PhpTokenTypes.opARRAY_CAST,   Types.strArray);
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
                if (operator != null && argument instanceof PhpTypedElement) {
                    final PsiElement parent = expression.getParent();
                    if (operator == PhpTokenTypes.opSTRING_CAST) {
                        /* case 1: concatenation `... . (string)...` */
                        if (parent instanceof BinaryExpression) {
                            final BinaryExpression binary = (BinaryExpression) parent;
                            if (binary.getOperationType() == PhpTokenTypes.opCONCAT) {
                                holder.registerProblem(operation, messageConcatenate, ProblemHighlightType.LIKE_UNUSED_SYMBOL, new ReplaceWithArgumentFix());
                                return;
                            }
                        }
                        /* case 2: self assign with `... .=  (string)...` */
                        else if (parent instanceof SelfAssignmentExpression) {
                            final SelfAssignmentExpression assignment = (SelfAssignmentExpression) parent;
                            if (assignment.getOperationType() == PhpTokenTypes.opCONCAT_ASGN) {
                                holder.registerProblem(operation, messageConcatenate, ProblemHighlightType.LIKE_UNUSED_SYMBOL, new ReplaceWithArgumentFix());
                                return;
                            }
                        }
                    }
                    /* case 3: un-needed due to types */
                    if (typesMapping.containsKey(operator)) {
                        final Set<String> types = this.resolveStrictly((PhpTypedElement) argument).getTypes();
                        if (types.size() == 1 && typesMapping.get(operator).equals(Types.getType(types.iterator().next()))) {
                            if (!(argument instanceof Variable) || !this.isWeakTypedParameter((Variable) argument)) {
                                final boolean isTarget = !this.isNullCoalescingOnly(argument);
                                if (isTarget) {
                                    holder.registerProblem(operation, messageGeneric, ProblemHighlightType.LIKE_UNUSED_SYMBOL, new ReplaceWithArgumentFix());
                                }
                            }
                        }
                    }
                }
            }

            private boolean isNullCoalescingOnly(@NotNull PsiElement argument) {
                boolean result                 = false;
                final Set<PsiElement> variants = PossibleValuesDiscoveryUtil.discover(argument);
                if (variants.size() == 1) {
                    final PsiElement candidate = variants.iterator().next().getParent();
                    if (candidate instanceof BinaryExpression) {
                        result = ((BinaryExpression) candidate).getOperationType() == PhpTokenTypes.opCOALESCE;
                    }
                }
                variants.clear();
                return result;
            }

            @NotNull
            private PhpType resolveStrictly(@NotNull PhpTypedElement expression) {
                final Project project = holder.getProject();
                PhpType result        = null;
                if (expression instanceof FieldReference) { /* fields has no type hints, hence private only */
                    final PsiElement resolved = OpenapiResolveUtil.resolveReference((FieldReference) expression);
                    if (resolved instanceof Field) {
                        final Field referencedField = (Field) resolved;
                        if (referencedField.getModifier().isPrivate()) {
                            result = OpenapiResolveUtil.resolveType(referencedField, project);
                        }
                    }
                } else if (expression instanceof MethodReference) { /* requires implicit return type declaration */
                    final PsiElement resolved = OpenapiResolveUtil.resolveReference((FunctionReference) expression);
                    if (resolved instanceof Function) {
                        final Function referencedFunction = (Function) resolved;
                        final PsiElement returnedType     = OpenapiElementsUtil.getReturnType(referencedFunction);
                        if (returnedType != null) {
                            result = OpenapiResolveUtil.resolveType(referencedFunction, project);
                        }
                    }
                } else {
                    result = OpenapiResolveUtil.resolveType(expression, project);
                }
                return result == null ? PhpType.EMPTY : result.filterUnknown();
            }

            private boolean isWeakTypedParameter(@NotNull Variable variable) {
                boolean result       = false;
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

    private static final class ReplaceWithArgumentFix implements LocalQuickFix {
        private static final String title = "Remove unnecessary casting";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement().getParent();
            if (expression instanceof UnaryExpression && !project.isDisposed()) {
                final PsiElement argument = ((UnaryExpression) expression).getValue();
                if (argument != null) {
                    expression.replace(argument.copy());
                }
            }
        }
    }
}
