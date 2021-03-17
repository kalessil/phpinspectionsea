package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;

public class MustBeStaticStrategy {
    private static final String strProblemDescription = "%m% must be static.";

    static public void apply(final Method method, final ProblemsHolder holder) {
        if (!method.isStatic()) {
            final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
            if (nameNode != null) {
                holder.registerProblem(
                        nameNode,
                        MessagesPresentationUtil.prefixWithEa(strProblemDescription.replace("%m%", method.getName()))
                );
            }
        }
    }
}
