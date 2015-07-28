package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.ArrayIndex;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.refactoring.PhpRefactoringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/**
 * @author Denis Ryabov
 * @author Vladimir Reznichenko
 */
public class OffsetOperationsInspector extends BasePhpInspection {
    private static final String strProblemUseSquareBrackets = "Please use square brackets instead of curvy for deeper analysis.";
    private static final String strProblemInvalidIndex = "Wrong index type (%t% is resolved)";

    private static final PhpType validIndexTypesSet = new PhpType();
    static {
        validIndexTypesSet.add(PhpType.STRING);
        validIndexTypesSet.add(PhpType.FLOAT);
        validIndexTypesSet.add(PhpType.INT);
        validIndexTypesSet.add(PhpType.BOOLEAN);
        validIndexTypesSet.add(PhpType.NULL);
        validIndexTypesSet.add(PhpType.STRING);
    }

    @NotNull
    public String getShortName() {
        return "OffsetOperationsInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpArrayAccessExpression(ArrayAccessExpression expression) {
                PsiElement bracketNode = expression.getLastChild();
                if (null == bracketNode) {
                    return;
                }

                // recommend to use [] instead of {}
                if (bracketNode.getText().equals("}")) {
                    holder.registerProblem(expression, strProblemUseSquareBrackets, ProblemHighlightType.WEAK_WARNING);
                    return;
                }

                // ensure value is one of (array, string, \ArrayAccess, \SimpleXMLElement)
                // => general error otherwise

                // ensure index is one of (string, float, bool, null)
                // => same applied to hash-elements e.g. array initialization
                // => general error otherwise
                ArrayIndex indexHolder = expression.getIndex();
                if (null != indexHolder) {
                    PhpPsiElement indexValue = indexHolder.getValue();
                    if (null != indexValue) {
                        PhpType indexValueType =
                                PhpRefactoringUtil.getCompletedType((PhpTypedElement) indexValue, expression.getProject());


                        // now check if any type provided and check sets relation
                        String indexTypeAsString = indexValueType.toString();
                        if (indexTypeAsString.length() > 0 && !PhpType.isSubType(indexValueType, validIndexTypesSet)) {
                            String strError = strProblemInvalidIndex.replace("%t%", indexValueType.toString());
                            holder.registerProblem(indexValue, strError, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                }
            }
        };
    }
}
