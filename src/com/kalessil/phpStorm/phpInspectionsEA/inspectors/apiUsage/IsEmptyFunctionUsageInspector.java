package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.PhpEmpty;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPsiResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class IsEmptyFunctionUsageInspector extends BasePhpInspection {
    private static final String strProblemDescriptionDoNotUse =
            "'empty(...)' is not type safe and brings N-path complexity due to multiple types supported." +
            " Consider refactoring this code.";

    private static final String strProblemDescriptionUseCount = "Use 'count($...) === 0' construction instead";
    private static final String strProblemDescriptionUseNullComparison = "Probably it should be 'null === $...' construction used";

    @NotNull
    public String getDisplayName() {
        return "API: 'empty(...)' usage";
    }

    @NotNull
    public String getShortName() {
        return "IsEmptyFunctionUsageInspection";
    }

    @NotNull
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
                    HashSet<String> objResolvedTypes = new HashSet<>();
                    TypeFromPsiResolvingUtil.resolveExpressionType(objParameterToInspect, PhpIndex.getInstance(holder.getProject()), objResolvedTypes);

                    /** Case 1: empty(array) - hidden logic - empty array */
                    if (this.isArrayType(objResolvedTypes)) {
                        objResolvedTypes.clear();

                        holder.registerProblem(emptyExpression, strProblemDescriptionUseCount, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        return;
                    }

                    /** case 2: nullable classes, int, float, resource */
                    if (this.isNullableCoreType(objResolvedTypes) || this.isNullableObjectInterface(objResolvedTypes)) {
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
                return resolvedTypesSet.size() == 1 && resolvedTypesSet.iterator().next().equals(Types.strArray);
            }

            /** check if nullable int, float, resource */
            private boolean isNullableCoreType(HashSet<String> resolvedTypesSet) {
                if (resolvedTypesSet.size() != 2 || !resolvedTypesSet.contains(Types.strNull)) {
                    return false;
                }

                return  resolvedTypesSet.contains(Types.strInteger) ||
                        resolvedTypesSet.contains(Types.strFloat) ||
                        resolvedTypesSet.contains(Types.strResource);
            }

            /** check if nullable object interfaces */
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
