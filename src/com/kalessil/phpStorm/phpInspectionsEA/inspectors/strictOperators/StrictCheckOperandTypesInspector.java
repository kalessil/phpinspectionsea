package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPsiResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class StrictCheckOperandTypesInspector extends BasePhpInspection {
    private static final String strProblemDescriptionArrayAccess = "Index-based access to not array (type is %t%).";
    private static final String strProblemDescriptionAssignment = "Type of variable (%t1%) doesn't match type of assigning value (%t2%).";
    private static final String strProblemDescriptionTernary = "Different types of true (%t1%) and false (%t2%) branches of ternary expression.";
    private static final String strProblemDescriptionUnaryPlus = "Unary plus detected before %t% type operand. Use direct type conversion instead.";
    private static final String strProblemDescriptionUnaryMinus = "Unary minus detected before %t% type operand.";
    private static final String strProblemDescriptionUnaryBitwiseNot = "Unary bitwise-not detected before %t% type operand.";
    private static final String strProblemDescriptionIncrement = "Increment/decrement of %t% type operand.";
    private static final String strProblemDescriptionBinaryPlus = "Wrong types in '+' operation (%t1% + %t2%).";
    private static final String strProblemDescriptionBinaryArithmetic = "Not numeric types in arithmetic operation (%t1% and %t2%).";
    private static final String strProblemDescriptionBinaryMod = "Not integer types in '%' operation (%t1% % %t2%).";
    private static final String strProblemDescriptionBinaryBitwise = "Not integer types in bitwise operation (%t1% and %t2%).";
    private static final String strProblemDescriptionBinaryLogical = "Not boolean types in logical operation (%t1%, %t2%).";
    private static final String strProblemDescriptionBinaryShift = "Not integer types in bit-shift operation (%t1% and %t2%).";
    private static final String strProblemDescriptionConcat = "Not string types in string concatenation operation (%t1% . %t2%).";
    private static final String strProblemDescriptionComparisonString = "Use binary-safe strcmp function instead of direct strings comparison.";
    private static final String strProblemDescriptionComparison = "Not numeric types in comparison (%t1% and %t2%).";
    private static final String strProblemDescriptionEqualityFloat = "Possible machine precision roundoff in float comparison. It's better to compare absolute difference with a small number.";
    private static final String strProblemDescriptionEquality = "Different types in comparison (%t1% and %t2%).";
    private static final String strProblemDescriptionArrayIndex = "Array key should be either integer or string (not %t%).";

    @NotNull
    public String getShortName() {
        return "StrictCheckOperandTypesInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpUnaryExpression(final UnaryExpression expr) {
                if (expr.getOperation() == null) {
                    return;
                }

                final HashSet<String> type = resolveType(expr);
                final String operation = expr.getOperation().getText();
                switch (operation) {
                    case "+":
                        inspectUnaryPlus(expr, type);
                        break;
                    case "-":
                        inspectUnaryMinus(expr, type);
                        break;
                    case "~":
                        inspectUnaryBitwiseNot(expr, type);
                        break;
                    case "++":
                    case "--":
                        inspectUnaryIncrement(expr, type);
                        break;
                    case "!":
                    case "not":
                        // unary logical not
                        // skipped as it's quite common to check `if (!$var)`
                        break;
                }
            }

            public void visitPhpBinaryExpression(final BinaryExpression expr) {
                if (expr.getOperation() == null) {
                    return;
                }

                final String operation = expr.getOperation().getText();

                final PsiElement leftOp = expr.getLeftOperand();
                final PsiElement rightOp = expr.getRightOperand();
                final HashSet<String> leftT = resolveType(leftOp);
                final HashSet<String> rightT = resolveType(rightOp);

                switch (operation) {
                    case "+":
                        inspectBinaryPlus(expr, leftT, rightT);
                        break;
                    case "-":
                    case "*":
                    case "/":
                    case "**":
                        inspectBinaryArithmetic(expr, leftT, rightT);
                        break;
                    case "%":
                        inspectBinaryMod(expr, leftT, rightT);
                        break;
                    case "&":
                    case "|":
                    case "^":
                        inspectBinaryBitwise(expr, leftT, rightT);
                        break;
                    case "&&":
                    case "and":
                    case "||":
                    case "or":
                    case "xor":
                        inspectBinaryLogical(expr, leftT, rightT);
                        break;
                    case "<<":
                    case ">>":
                        inspectBinaryShift(expr, leftT, rightT);
                        break;
                    case ".":
                        inspectBinaryConcat(expr, leftT, rightT);
                        break;
                    case "<":
                    case "<=":
                    case ">":
                    case ">=":
                    case "<=>":
                        inspectBinaryComparison(expr, leftT, rightT);
                        break;
                    case "==":
                    case "===":
                    case "!=":
                    case "!==":
                    case "<>":
                        inspectBinaryEquality(expr, leftT, rightT);
                        break;
                }
            }

            public void visitPhpSelfAssignmentExpression(final SelfAssignmentExpression expr) {
                if (expr.getOperation() == null) {
                    return;
                }

                final String operation = expr.getOperation().getText();

                final PsiElement leftOp = expr.getFirstPsiChild();
                final PsiElement rightOp = expr.getValue();
                final HashSet<String> leftT = resolveType(leftOp);
                final HashSet<String> rightT = resolveType(rightOp);

                switch (operation) {
                    case "+=":
                        inspectBinaryPlus(expr, leftT, rightT);
                        break;
                    case "-=":
                    case "*=":
                    case "/=":
                    case "**=":
                        inspectBinaryArithmetic(expr, leftT, rightT);
                        break;
                    case "%=":
                        inspectBinaryMod(expr, leftT, rightT);
                        break;
                    case "&=":
                    case "|=":
                    case "^=":
                        inspectBinaryBitwise(expr, leftT, rightT);
                        break;
                    case "&&=":
                    case "||=":
                        inspectBinaryLogical(expr, leftT, rightT);
                        break;
                    case "<<=":
                    case ">>=":
                        inspectBinaryShift(expr, leftT, rightT);
                        break;
                    case ".=":
                        inspectBinaryConcat(expr, leftT, rightT);
                        break;
                }
            }

            public void visitPhpArrayAccessExpression(final ArrayAccessExpression expr) {
                final PhpPsiElement value = expr.getValue();
                final HashSet<String> type = resolveType(value);
                if (isArray(type)) {
                    return;
                }
                final String strWarning = strProblemDescriptionArrayAccess
                        .replace("%t%", formatType(type));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            public void visitPhpArrayIndex(final ArrayIndex index) {
                final PhpPsiElement key = index.getValue();
                if (key instanceof PhpExpression) {
                    inspectArrayIndex((PhpExpression) key);
                }
            }

            public void visitPhpExpression(final PhpExpression expr) {
                if (expr instanceof ArrayHashElement) {
                    final PhpPsiElement key = ((ArrayHashElement) expr).getKey();
                    if (key instanceof PhpExpression) {
                        inspectArrayIndex((PhpExpression) key);
                    }
                }
            }

            public void visitPhpAssignmentExpression(final AssignmentExpression expr) {
                final PhpPsiElement var = expr.getVariable();
                final PhpPsiElement value = expr.getValue();
                final HashSet<String> varType = resolveType(var);
                final HashSet<String> valueType = resolveType(value);
                if (isMixed(varType) || sameTypes(varType, valueType)) {
                    return;
                }
                final String strWarning = strProblemDescriptionAssignment
                        .replace("%t1%", formatType(varType))
                        .replace("%t2%", formatType(valueType));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            public void visitPhpTernaryExpression(final TernaryExpression expr) {
                final PsiElement first = expr.getTrueVariant();
                final PsiElement second = expr.getFalseVariant();
                final HashSet<String> firstType = resolveType(first);
                final HashSet<String> secondType = resolveType(second);
                if (sameTypes(firstType, secondType)) {
                    return;
                }
                final String strWarning = strProblemDescriptionTernary
                        .replace("%t1%", formatType(firstType))
                        .replace("%t2%", formatType(secondType));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private HashSet<String> resolveType(final PsiElement expr) {
                final PhpIndex objIndex = PhpIndex.getInstance(holder.getProject());
                final Function objScope = ExpressionSemanticUtil.getScope(expr);
                final HashSet<String> objResolvedTypes = new HashSet<String>();
                TypeFromPsiResolvingUtil.resolveExpressionType(expr, objScope, objIndex, objResolvedTypes);
                objResolvedTypes.remove(Types.strResolvingAbortedOnPsiLevel);
                objResolvedTypes.remove(Types.strClassNotResolved);
                if (objResolvedTypes.isEmpty()) {
                    objResolvedTypes.add(Types.strMixed);
                }
                return objResolvedTypes;
            }

            private boolean sameTypes(final HashSet<String> type1, final HashSet<String> type2) {
                final HashSet<String> type = new HashSet<String>(type1);
                type.retainAll(type2);
                return !type.isEmpty();
            }

            private boolean isInt(final HashSet<String> type) {
                return type.contains(Types.strInteger);
            }

            private boolean isNumeric(final HashSet<String> type) {
                return type.contains(Types.strInteger) || type.contains(Types.strFloat);
            }

            private boolean isString(final HashSet<String> type) {
                return type.contains(Types.strString);
            }

            private boolean isBoolean(final HashSet<String> type) {
                return type.contains(Types.strBoolean);
            }

            private boolean isArray(final HashSet<String> type) {
                return type.contains(Types.strArray);
            }

            private boolean isNull(final HashSet<String> type) {
                return type.contains(Types.strNull);
            }

            private boolean isMixed(final HashSet<String> type) {
                return type.contains(Types.strMixed);
            }

            private String formatType(final HashSet<String> type) {
                switch (type.size()) {
                    case 0:
                        return "unknown";
                    case 1:
                        return type.iterator().next();
                    default:
                        final StringBuilder sb = new StringBuilder();
                        for (final String s : type) {
                            sb.append(s);
                            sb.append('|');
                        }
                        return sb.delete(sb.length() - 1, sb.length()).toString();
                }
            }

            private void inspectUnaryPlus(final UnaryExpression expr, final HashSet<String> type) {
                final String strWarning = strProblemDescriptionUnaryPlus
                        .replace("%t%", formatType(type));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectUnaryMinus(final UnaryExpression expr, final HashSet<String> type) {
                if (isNumeric(type)) {
                    return;
                }
                final String strWarning = strProblemDescriptionUnaryMinus
                        .replace("%t%", formatType(type));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectUnaryBitwiseNot(final UnaryExpression expr, final HashSet<String> type) {
                if (isInt(type)) {
                    return;
                }
                final String strWarning = strProblemDescriptionUnaryBitwiseNot
                        .replace("%t%", formatType(type));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectUnaryIncrement(final UnaryExpression expr, final HashSet<String> type) {
                if (isInt(type)) {
                    return;
                }
                final String strWarning = strProblemDescriptionIncrement
                        .replace("%t%", formatType(type));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectBinaryPlus(final PhpExpression expr, final HashSet<String> leftT, final HashSet<String> rightT) {
                if (isNumeric(leftT) && isNumeric(rightT)) {
                    // numeric addition
                    return;
                }
                if (isArray(leftT) && isArray(rightT)) {
                    // array merging
                    return;
                }
                final String strWarning = strProblemDescriptionBinaryPlus
                        .replace("%t1%", formatType(leftT))
                        .replace("%t2%", formatType(rightT));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectBinaryArithmetic(final PhpExpression expr, final HashSet<String> leftT, final HashSet<String> rightT) {
                if (isNumeric(leftT) && isNumeric(rightT)) {
                    return;
                }
                final String strWarning = strProblemDescriptionBinaryArithmetic
                        .replace("%t1%", formatType(leftT))
                        .replace("%t2%", formatType(rightT));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectBinaryMod(final PhpExpression expr, final HashSet<String> leftT, final HashSet<String> rightT) {
                if (isInt(leftT) && isInt(rightT)) {
                    return;
                }
                final String strWarning = strProblemDescriptionBinaryMod
                        .replace("%t1%", formatType(leftT))
                        .replace("%t2%", formatType(rightT));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectBinaryBitwise(final PhpExpression expr, final HashSet<String> leftT, final HashSet<String> rightT) {
                if (isInt(leftT) && isInt(rightT)) {
                    return;
                }
                final String strWarning = strProblemDescriptionBinaryBitwise
                        .replace("%t1%", formatType(leftT))
                        .replace("%t2%", formatType(rightT));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectBinaryShift(final PhpExpression expr, final HashSet<String> leftT, final HashSet<String> rightT) {
                if (isInt(leftT) && isInt(rightT)) {
                    return;
                }
                final String strWarning = strProblemDescriptionBinaryShift
                        .replace("%t1%", formatType(leftT))
                        .replace("%t2%", formatType(rightT));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectBinaryLogical(final PhpExpression expr, final HashSet<String> leftT, final HashSet<String> rightT) {
                if (isBoolean(leftT) && isBoolean(rightT)) {
                    return;
                }
                final String strWarning = strProblemDescriptionBinaryLogical
                        .replace("%t1%", formatType(leftT))
                        .replace("%t2%", formatType(rightT));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectBinaryConcat(final PhpExpression expr, final HashSet<String> leftT, final HashSet<String> rightT) {
                if (isString(leftT) && isString(rightT)) {
                    return;
                }
                final String strWarning = strProblemDescriptionConcat
                        .replace("%t1%", formatType(leftT))
                        .replace("%t2%", formatType(rightT));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectBinaryComparison(final BinaryExpression expr, final HashSet<String> leftT, final HashSet<String> rightT) {
                if (isNumeric(leftT) && isNumeric(rightT)) {
                    return;
                }
                final String strWarning;
                if (isString(leftT) && isString(rightT)) {
                    strWarning = strProblemDescriptionComparisonString;
                } else {
                    strWarning = strProblemDescriptionComparison
                            .replace("%t1%", formatType(leftT))
                            .replace("%t2%", formatType(rightT));
                }
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectBinaryEquality(final BinaryExpression expr, final HashSet<String> leftT, final HashSet<String> rightT) {
                if (isInt(leftT) && isInt(rightT)) {
                    return;
                }
                if (isNumeric(leftT) && isNumeric(rightT)) {
                    holder.registerProblem(expr, strProblemDescriptionEqualityFloat, ProblemHighlightType.WEAK_WARNING);
                    return;
                }
                if (sameTypes(leftT, rightT)) {
                    return;
                }
                if (isNull(leftT) || isNull(rightT)) {
                    // allow null values
                    return;
                }
                final String strWarning = strProblemDescriptionEquality
                        .replace("%t1%", formatType(leftT))
                        .replace("%t2%", formatType(rightT));
                holder.registerProblem(expr, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectArrayIndex(final PhpExpression key) {
                final HashSet<String> type = resolveType(key);
                if (isInt(type) || isString(type)) {
                    return;
                }
                final String strWarning = strProblemDescriptionArrayIndex
                        .replace("%t%", formatType(type));
                holder.registerProblem(key, strWarning, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
