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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class IsEmptyFunctionUsageInspector extends BasePhpInspection {
    private static final String strProblemDescriptionDoNotUse =
            "'empty(...)' is not type safe and brings N-path complexity due to multiple types supported." +
            " Consider refactoring this code.";

    private static final String strProblemDescriptionUseCount = "Use 'count($...) === 0' construction instead";
    private static final String strProblemDescriptionUseNullComparison = "Probably it should be 'null === $...'";

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
                    PhpIndex objIndex = PhpIndex.getInstance(holder.getProject());
                    LinkedList<String> objResolvedTypes = new LinkedList<>();
                    TypeFromPsiResolvingUtil.resolveExpressionType(arrValues[0], objIndex, objResolvedTypes);

                    /** get unique values only, TODO: HashSet */
                    List<String> listUniqueSignatures = new ArrayList<>(new HashSet<>(objResolvedTypes));
                    objResolvedTypes.clear();

                    /** debug */
                    /*String strResolvedTypes = "Resolved: ";
                    String strGlue = "";
                    for (String strOneType : listUniqueSignatures) {
                        strResolvedTypes += strGlue + strOneType;
                        strGlue = "|";
                    }
                    holder.registerProblem(emptyExpression, strResolvedTypes, ProblemHighlightType.LIKE_DEPRECATED);*/
                    /** /debug  */


                    /** Case 1: empty(array) - hidden logic */
                    if (listUniqueSignatures.size() == 1 && listUniqueSignatures.get(0).equals(Types.strArray)) {
                        holder.registerProblem(emptyExpression, strProblemDescriptionUseCount, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        return;
                    }

                    /** Case 2: empty(object) - error */
                    boolean areVariantsObjectInterfaces = (listUniqueSignatures.size() > 0);
                    for (String strOneType : listUniqueSignatures) {
                        areVariantsObjectInterfaces = (
                            areVariantsObjectInterfaces && (
                                (
                                    /** class or interface is given */
                                    strOneType.charAt(0) == '\\' && !strOneType.equals(Types.strClassNotResolved)
                                )
                                ||
                                (
                                    /** null among other types, so we can pass through */
                                    (listUniqueSignatures.size() >= 2) && strOneType.equals(Types.strNull)
                                )
                            )

                        );
                    }
                    if (areVariantsObjectInterfaces) {
                        holder.registerProblem(emptyExpression, strProblemDescriptionUseNullComparison, ProblemHighlightType.ERROR);
                        return;
                    }
                }

                holder.registerProblem(emptyExpression, strProblemDescriptionDoNotUse, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
