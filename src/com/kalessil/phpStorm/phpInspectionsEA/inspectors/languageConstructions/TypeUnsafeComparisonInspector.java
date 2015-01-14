package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpIndexUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPsiResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedList;

public class TypeUnsafeComparisonInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Hardening to type safe '===', '!==' will cover/point to types casting issues";
    private static final String strProblemDescriptionSafeToReplace = "Safely use '... === ...', '... !== ...' constructions instead";
    private static final String strProblemDescriptionMissingToStringMethod = "Class %class% must implement __toString()";

    @NotNull
    public String getDisplayName() {
        return "Type compatibility: type unsafe equality operators";
    }

    @NotNull
    public String getShortName() {
        return "TypeUnsafeComparisonInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(BinaryExpression expression) {
                PsiElement objOperation = expression.getOperation();
                if (null == objOperation) {
                    return;
                }

                /**  */
                final IElementType operationType = objOperation.getNode().getElementType();
                if (operationType != PhpTokenTypes.opEQUAL && operationType != PhpTokenTypes.opNOT_EQUAL) {
                    return;
                }

                this.triggerProblem(expression);
            }

            /** generates more specific warnings for given expression */
            private void triggerProblem(BinaryExpression objExpression) {
                PsiElement objLeftOperand = objExpression.getLeftOperand();
                PsiElement objRightOperand = objExpression.getRightOperand();
                if (
                    objRightOperand instanceof StringLiteralExpression ||
                    objLeftOperand instanceof StringLiteralExpression
                ) {
                    PsiElement objNonStringOperand;
                    String strLiteralValue;
                    if (objRightOperand instanceof StringLiteralExpression) {
                        strLiteralValue = ((StringLiteralExpression) objRightOperand).getContents();
                        objNonStringOperand = objLeftOperand;
                    } else {
                        strLiteralValue = ((StringLiteralExpression) objLeftOperand).getContents();
                        objNonStringOperand = objRightOperand;
                    }


                    /** resolve 2nd operand type, if class ensure __toString is implemented */
                    objNonStringOperand = ExpressionSemanticUtil.getExpressionTroughParenthesis(objNonStringOperand);
                    if (null != objNonStringOperand) {
                        PhpIndex objIndex = PhpIndex.getInstance(holder.getProject());
                        Function objScope = ExpressionSemanticUtil.getScope(objNonStringOperand);

                        HashSet<String> objResolvedTypes = new HashSet<>();
                        TypeFromPsiResolvingUtil.resolveExpressionType(objNonStringOperand, objScope, objIndex, objResolvedTypes);
                        if (this.isNullableObjectInterface(objResolvedTypes)) {
                            /** collect classes to check if __toString() is there */
                            LinkedList<PhpClass> listClasses = new LinkedList<>();
                            for (String strClass : objResolvedTypes) {
                                if (strClass.charAt(0) == '\\') {
                                    listClasses.addAll(PhpIndexUtil.getObjectInterfaces(strClass, objIndex));
                                }
                            }

                            /** check methods, error on first one violated requirements */
                            for (PhpClass objClass : listClasses) {
                                if (null == objClass.findMethodByName("__toString")) {
                                    String strError = strProblemDescriptionMissingToStringMethod.replace("%class%", objClass.getFQN());
                                    holder.registerProblem(objExpression, strError, ProblemHighlightType.ERROR);

                                    listClasses.clear();
                                    return;
                                }

                            }

                            /** terminate inspection, php will call __toString() */
                            listClasses.clear();
                            return;
                        }
                    }


                    if (strLiteralValue.length() > 0 && !strLiteralValue.matches("^[0-9\\+\\-]+$")) {
                        holder.registerProblem(objExpression, strProblemDescriptionSafeToReplace, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        return;
                    }
                }

                holder.registerProblem(objExpression, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }

            /** check if nullable object interfaces */
            /** TODO: move to utils */
            private boolean isNullableObjectInterface(HashSet<String> resolvedTypesSet) {
                int intCountTypesToInspect = resolvedTypesSet.size();
                if (resolvedTypesSet.contains(Types.strClassNotResolved)) {
                    --intCountTypesToInspect;
                }
                if (resolvedTypesSet.contains(Types.strNull)) {
                    --intCountTypesToInspect;
                }
                /** ensure we still have variants left */
                if (intCountTypesToInspect == 0) {
                    return false;
                }

                /** work through types, ensure it's null or classes references */
                for (String strTypeToInspect : resolvedTypesSet) {
                    /** skip core types, but null */
                    if (strTypeToInspect.charAt(0) != '\\' && !strTypeToInspect.equals(Types.strNull)) {
                        return false;
                    }
                }

                return true;
            }

        };
    }
}