package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.refactoring.PhpRefactoringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPsiResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

/**
 * @author Denis Ryabov
 * @author Vladimir Reznichenko
 */
public class OffsetOperationsInspector extends BasePhpInspection {
    private static final String strProblemUseSquareBrackets = "Please use square brackets instead of curvy for deeper analysis.";
    private static final String strProblemInvalidIndex = "Wrong index type (%t% is resolved)";

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

                        // resolve types with custom resolver, native gives type sets which not comparable properly
                        // TODO: wrap native resolver and normalize to be compatible with our wrapper
                        HashSet<String> types = new HashSet<String>();
                        TypeFromPsiResolvingUtil.resolveExpressionType(
                                indexValue,
                                ExpressionSemanticUtil.getScope(expression),
                                PhpIndex.getInstance(holder.getProject()),
                                types
                        );

                        // now check if any type provided and check sets validity
                        if (types.size() > 0) {
                            // remove valid types
                            types.remove(Types.strString);
                            types.remove(Types.strFloat);
                            types.remove(Types.strInteger);
                            types.remove(Types.strBoolean);
                            types.remove(Types.strNull);
                            types.remove(Types.strMixed);
                            types.remove(Types.strClassNotResolved);
                            types.remove(Types.strResolvingAbortedOnPsiLevel);

                            if (types.size() > 0) {
                                String strError = strProblemInvalidIndex.replace("%t%", types.iterator().next());
                                holder.registerProblem(indexValue, strError, ProblemHighlightType.GENERIC_ERROR);
                            }
                        }
                        types.clear();
                    }
                }
            }
        };
    }
}
