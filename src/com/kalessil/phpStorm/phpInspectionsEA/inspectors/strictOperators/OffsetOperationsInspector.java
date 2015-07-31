package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.ArrayIndex;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPlatformResolverUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

/**
 * @author Denis Ryabov
 * @author Vladimir Reznichenko
 */
public class OffsetOperationsInspector extends BasePhpInspection {
    private static final String strProblemUseSquareBrackets = "Please use square brackets instead of curvy for deeper analysis.";
    private static final String strProblemInvalidIndex = "Wrong index type (%t% is incompatible)";

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
                final boolean isArrayAccessSupported = true;
                final boolean isIndexCanBeObject     = false;
                // TODO: next step - analyse container capabilities (offsetSet)

                // ensure index is one of (string, float, bool, null)
                // TODO: hash-elements e.g. array initialization
                ArrayIndex indexHolder = expression.getIndex();
                if (null != indexHolder) {
                    PhpPsiElement indexValue = indexHolder.getValue();
                    if (null != indexValue) {
                        // resolve types with custom resolver, native gives type sets which not comparable properly
                        HashSet<String> types = new HashSet<String>();
                        TypeFromPlatformResolverUtil.resolveExpressionType(indexValue, types);

                        // now check if any type provided and check sets validity
                        if (types.size() > 0) {
                            // clean typ to keep only incompatible once
                            cleanResolvedTypeToKeepIncompatible(types);
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

    private void cleanResolvedTypeToKeepIncompatible(@NotNull HashSet<String> types) {
        types.remove(Types.strString);
        types.remove(Types.strFloat);
        types.remove(Types.strInteger);
        types.remove(Types.strBoolean);
        types.remove(Types.strNull);
        types.remove(Types.strMixed);
        types.remove(Types.strStatic);
    }

}
