package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.PhpEmpty;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPsiResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypesSemanticsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class IsEmptyFunctionUsageInspector extends BasePhpInspection {
    private static final String strProblemDescriptionDoNotUse = "'empty(...)' counts too much values as empty, consider refactoring with type sensitive checks";
    private static final String strProblemDescriptionUseCount = "'count($...) === 0' construction shall be used instead";
    private static final String strProblemDescriptionUseNullComparison = "Probably it should be 'null === $...' construction used";
    
    @NotNull
    public String getShortName() {
        return "IsEmptyFunctionUsageInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpEmpty(PhpEmpty emptyExpression) {
                PhpExpression[] arrValues = emptyExpression.getVariables();
                if (arrValues.length == 1) {
                    PsiElement objParameterToInspect = ExpressionSemanticUtil.getExpressionTroughParenthesis(arrValues[0]);
                    if (objParameterToInspect instanceof ArrayAccessExpression) {
                        /** currently php docs lacks of array structure notations, skip it */
                        return;
                    }


                    /** extract types */
                    PhpIndex objIndex = PhpIndex.getInstance(holder.getProject());
                    Function objScope = ExpressionSemanticUtil.getScope(emptyExpression);
                    HashSet<String> objResolvedTypes = new HashSet<String>();
                    TypeFromPsiResolvingUtil.resolveExpressionType(objParameterToInspect, objScope, objIndex, objResolvedTypes);

                    /** Case 1: empty(array) - hidden logic - empty array */
                    if (this.isArrayType(objResolvedTypes)) {
                        objResolvedTypes.clear();
                        holder.registerProblem(emptyExpression, strProblemDescriptionUseCount, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        return;
                    }

                    /** case 2: nullable classes, int, float, resource */
                    if (this.isNullableCoreType(objResolvedTypes) || TypesSemanticsUtil.isNullableObjectInterface(objResolvedTypes)) {
                        objResolvedTypes.clear();
                        holder.registerProblem(emptyExpression, strProblemDescriptionUseNullComparison, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        return;
                    }

                    objResolvedTypes.clear();
                }

                holder.registerProblem(emptyExpression, strProblemDescriptionDoNotUse, ProblemHighlightType.WEAK_WARNING);
            }


            /** check if only array type possible */
            private boolean isArrayType(HashSet<String> resolvedTypesSet) {
                return resolvedTypesSet.size() == 1 && resolvedTypesSet.contains(Types.strArray);
            }

            /** check if nullable int, float, resource */
            private boolean isNullableCoreType(HashSet<String> resolvedTypesSet) {
                //noinspection SimplifiableIfStatement
                if (resolvedTypesSet.size() != 2 || !resolvedTypesSet.contains(Types.strNull)) {
                    return false;
                }

                return  resolvedTypesSet.contains(Types.strInteger) ||
                        resolvedTypesSet.contains(Types.strFloat) ||
                        resolvedTypesSet.contains(Types.strResource);
            }
        };
    }
}
