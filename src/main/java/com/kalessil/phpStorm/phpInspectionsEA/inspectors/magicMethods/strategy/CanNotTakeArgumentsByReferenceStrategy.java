package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;

public class CanNotTakeArgumentsByReferenceStrategy {
    private static final String strProblemDescription = "%m% cannot accept arguments by reference.";

    static public void apply(final Method method, final ProblemsHolder holder) {
        for (final Parameter parameter : method.getParameters()) {
            if (parameter.isPassByRef()) {
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
                if (nameNode != null) {
                    holder.registerProblem(
                            nameNode,
                            MessagesPresentationUtil.prefixWithEa(strProblemDescription.replace("%m%", method.getName()))
                    );
                    return;
                }
            }
        }
    }
}
