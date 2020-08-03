package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;

public class MinimalPhpVersionStrategy {
    private static final String strProblemDescription = "%m% is introduced only in version %v%, hence it's unused.";

    static public void apply(final Method method, final ProblemsHolder holder, final PhpLanguageLevel neededVersion) {
        final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
        if (nameNode != null && PhpLanguageLevel.get(holder.getProject()).below(neededVersion)) {
            holder.registerProblem(
                    nameNode,
                    MessagesPresentationUtil.prefixWithEa(strProblemDescription.replace("%m%", method.getName()).replace("%v%", neededVersion.getVersion())),
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL
            );
        }
    }
}
