package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils.ExpressionCostEstimateUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class OpenapiResolveUtil {
    private static final Map<String, PhpType> replacingFunctions = new HashMap<>();
    static {
        replacingFunctions.put("preg_filter", new PhpType().add(PhpType.NULL));
        replacingFunctions.put("preg_replace_callback_array", new PhpType().add(PhpType.NULL));
        replacingFunctions.put("preg_replace_callback", new PhpType().add(PhpType.NULL));
        replacingFunctions.put("preg_replace", new PhpType().add(PhpType.NULL));
    }

    @Nullable
    static public PsiElement resolveReference(@NotNull PsiReference reference) {
        try {
            return reference.resolve();
        } catch (final Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return null;
        }
    }

    @Nullable
    static public PhpType resolveType(@NotNull PhpTypedElement expression, @NotNull Project project) {
        PhpType result = null;
        try {
            if (expression instanceof FunctionReference) {
                /* resolve function and get it's type or fallback to empty type */
                final FunctionReference reference = (FunctionReference) expression;
                final PsiElement function         = resolveReference(reference);
                result = function instanceof Function
                        ? ((Function) function).getType().global(project)
                        : new PhpType();
                if (!result.isEmpty() && !(function instanceof Method)) {
                    /* some of signatures needs to be patched */
                    final String functionName = reference.getName();
                    if (functionName != null && replacingFunctions.containsKey(functionName)) {
                        result = result.filter(replacingFunctions.get(functionName));
                    }
                }
            } else if (expression instanceof ArrayAccessExpression) {
                /* `_GET[...] & co` gets resolved with missing string type */
                final PsiElement globalCandidate = ((ArrayAccessExpression) expression).getValue();
                if (globalCandidate instanceof Variable) {
                    final String variableName = ((Variable) globalCandidate).getName();
                    if (ExpressionCostEstimateUtil.predefinedVars.contains(variableName)) {
                        result = new PhpType().add(PhpType.STRING).add(PhpType.ARRAY);
                    }
                }
            } else if (expression instanceof BinaryExpression) {
                final BinaryExpression binary = (BinaryExpression) expression;
                final IElementType operator   = binary.getOperationType();
                if (operator == PhpTokenTypes.opPLUS || operator == PhpTokenTypes.opMINUS || operator == PhpTokenTypes.opMUL) {
                    /* workaround for https://youtrack.jetbrains.com/issue//WI-37466 & co */
                    boolean hasFloat      = true;
                    boolean hasArray      = false;
                    final PsiElement left = ExpressionSemanticUtil.getExpressionTroughParenthesis(binary.getLeftOperand());
                    if (left instanceof PhpTypedElement) {
                        final PhpType leftType = resolveType((PhpTypedElement) left, project);
                        if (leftType != null) {
                            final Set<String> leftTypes = new HashSet<>();
                            leftType.getTypes().forEach(type -> leftTypes.add(Types.getType(type)));
                            hasFloat = leftTypes.contains(Types.strFloat) || leftTypes.contains(Types.strNumber);
                            hasArray = leftTypes.contains(Types.strArray);
                            leftTypes.clear();
                            if (!hasFloat || (!hasArray && operator == PhpTokenTypes.opPLUS)) {
                                final PsiElement right
                                        = ExpressionSemanticUtil.getExpressionTroughParenthesis(binary.getRightOperand());
                                if (right instanceof PhpTypedElement) {
                                    final PhpType rightType = resolveType((PhpTypedElement) right, project);
                                    if (rightType != null) {
                                        final Set<String> rightTypes = new HashSet<>();
                                        rightType.getTypes().forEach(type -> rightTypes.add(Types.getType(type)));
                                        hasFloat = hasFloat ||
                                                   rightTypes.contains(Types.strFloat) || leftTypes.contains(Types.strNumber);
                                        hasArray = (hasArray && !OpenapiTypesUtil.isNumber(right)) ||
                                                   rightTypes.contains(Types.strArray);
                                        rightTypes.clear();
                                    }
                                }
                            }
                        }
                    }
                    result = hasFloat
                            ? new PhpType().add(PhpType.FLOAT)
                            : new PhpType().add(PhpType.INT);
                    result = hasArray
                            ? new PhpType().add(PhpType.ARRAY)
                            : result;
                } else if (operator == PhpTokenTypes.opCOALESCE) {
                    /* workaround for https://youtrack.jetbrains.com/issue/WI-37013 & co */
                    final PsiElement left  = binary.getLeftOperand();
                    final PsiElement right = binary.getRightOperand();
                    result                 = PhpType.EMPTY;
                    if (left instanceof PhpTypedElement && right instanceof PhpTypedElement) {
                        final PhpType leftType = resolveType((PhpTypedElement) left, project);
                        if (leftType != null && !leftType.filterUnknown().isEmpty()) {
                            final PhpType rightType = resolveType((PhpTypedElement) right, project);
                            if (rightType != null && !rightType.filterUnknown().isEmpty()) {
                                result = new PhpType().add(leftType.filterNull()).add(rightType);
                            }
                        }
                    }
                }
            } else if (expression instanceof TernaryExpression) {
                final TernaryExpression ternary = (TernaryExpression) expression;
                if (ternary.isShort()) {
                    final PsiElement left  = ternary.getTrueVariant();
                    final PsiElement right = ternary.getFalseVariant();
                    result                 = PhpType.EMPTY;
                    if (left instanceof PhpTypedElement && right instanceof PhpTypedElement) {
                        final PhpType leftType = resolveType((PhpTypedElement) left, project);
                        if (leftType != null && !leftType.filterUnknown().isEmpty()) {
                            final PhpType rightType = resolveType((PhpTypedElement) right, project);
                            if (rightType != null && !rightType.filterUnknown().isEmpty()) {
                                result = new PhpType().add(leftType.filterNull()).add(rightType);
                            }
                        }
                    }
                }
            } else if (expression instanceof UnaryExpression) {
                final UnaryExpression unary = (UnaryExpression) expression;
                if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opBIT_NOT)) {
                    final PsiElement argument = unary.getValue();
                    if (argument instanceof PhpTypedElement) {
                        result = resolveType((PhpTypedElement) argument, project);
                    }
                }
            }
            /* default behaviour */
            result = result == null
                    ? expression.getType().global(project)
                    : result;
        } catch (final Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            result = null;
        }
        return result;
    }

    @NotNull
    static public Collection<PhpClass> resolveClassesByFQN(@NotNull String name, @NotNull PhpIndex index) {
        try {
            return index.getClassesByFQN(name);
        } catch (final Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return new ArrayList<>();
        }
    }

    @NotNull
    static private Collection<PhpClass> resolveInterfacesByFQN(@NotNull String name, @NotNull PhpIndex index) {
        try {
            return index.getInterfacesByFQN(name);
        } catch (final Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return new ArrayList<>();
        }
    }

    @NotNull
    static public List<PhpClass> resolveClassesAndInterfacesByFQN(@NotNull String name, @NotNull PhpIndex index) {
        final Collection<PhpClass> classes    = resolveClassesByFQN(name, index);
        final Collection<PhpClass> interfaces = resolveInterfacesByFQN(name, index);
        final List<PhpClass> result           = new ArrayList<>(classes.size() + interfaces.size());
        result.addAll(classes);
        result.addAll(interfaces);
        return result;
    }

    @NotNull
    static public Collection<PhpClass> resolveChildClasses(@NotNull String clazz, @NotNull PhpIndex index) {
        try {
            return index.getAllSubclasses(clazz);
        } catch (final Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return new ArrayList<>();
        }
    }

    @Nullable
    static public PhpClass resolveSuperClass(@NotNull PhpClass clazz) {
        try {
            return clazz.getSuperClass();
        } catch (final Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return null;
        }
    }

    @NotNull
    static public List<PhpClass> resolveImplementedInterfaces(@NotNull PhpClass clazz) {
        try {
            final PhpClass[] interfaces = clazz.getImplementedInterfaces();
            return interfaces == null
                    ? new ArrayList<>()
                    : new ArrayList<>(Arrays.asList(interfaces));
        } catch (final Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return new ArrayList<>();
        }
    }

    @Nullable
    static public Method resolveMethod(@NotNull PhpClass clazz, @NotNull String methodName) {
        try {
            return clazz.findMethodByName(methodName);
        } catch (final Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return null;
        }
    }

    @Nullable
    static public Field resolveField(@NotNull PhpClass clazz, @NotNull String fieldName) {
        try {
            return clazz.findFieldByName(fieldName, false);
        } catch (final Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return null;
        }
    }
}
