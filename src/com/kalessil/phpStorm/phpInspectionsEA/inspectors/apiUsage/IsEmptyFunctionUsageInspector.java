package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpEmpty;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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
                    /** extract types */
                    HashSet<String> objResolvedTypes = new HashSet<>();
                    TypeFromPsiResolvingUtil.resolveExpressionType(arrValues[0], PhpIndex.getInstance(holder.getProject()), objResolvedTypes);

                    /** Case 1: empty(array) - hidden logic - empty array */
                    if (objResolvedTypes.size() == 1 && objResolvedTypes.iterator().next().equals(Types.strArray)) {
                        holder.registerProblem(emptyExpression, strProblemDescriptionUseCount, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                        objResolvedTypes.clear();
                        return;
                    }

                    /** Case 2: empty(object) - hidden logic - can be not initialized */
                    final boolean hasSeveralVariants = (objResolvedTypes.size() >= 2);
                    boolean areVariantsObjectInterfaces = (objResolvedTypes.size() > 0);
                    for (String strOneType : objResolvedTypes) {
                        final boolean isObjectInterfaceGiven = (strOneType.charAt(0) == '\\' && !strOneType.equals(Types.strClassNotResolved));
                        final boolean isNullAmongTypes       = (hasSeveralVariants && strOneType.equals(Types.strNull));

                        areVariantsObjectInterfaces = (areVariantsObjectInterfaces && (isObjectInterfaceGiven || isNullAmongTypes));
                    }
                    if (areVariantsObjectInterfaces) {
                        holder.registerProblem(emptyExpression, strProblemDescriptionUseNullComparison, ProblemHighlightType.ERROR);

                        objResolvedTypes.clear();
                        return;
                    }

                    objResolvedTypes.clear();
                }

                holder.registerProblem(emptyExpression, strProblemDescriptionDoNotUse, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
