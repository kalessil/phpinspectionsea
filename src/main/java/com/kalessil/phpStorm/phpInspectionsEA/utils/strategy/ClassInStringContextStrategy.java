package com.kalessil.phpStorm.phpInspectionsEA.utils.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpIndexUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPsiResolvingUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypesSemanticsUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;

final public class ClassInStringContextStrategy {
    public static boolean apply (
        @Nullable PsiElement objNonStringOperand,
        ProblemsHolder holder,
        PsiElement objExpression,
        String strClassHasNoToStringMessage
    ) {
        if (null == objNonStringOperand) {
            return false;
        }

        PhpIndex objIndex = PhpIndex.getInstance(holder.getProject());
        Function objScope = ExpressionSemanticUtil.getScope(objNonStringOperand);

        HashSet<String> objResolvedTypes = new HashSet<>();
        TypeFromPsiResolvingUtil.resolveExpressionType(objNonStringOperand, objScope, objIndex, objResolvedTypes);
        if (!TypesSemanticsUtil.isNullableObjectInterface(objResolvedTypes)) {
            return false;
        }

        /* collect classes to check if __toString() is there */
        LinkedList<PhpClass> listClasses = new LinkedList<>();
        for (String strClass : objResolvedTypes) {
            if (strClass.charAt(0) == '\\') {
                listClasses.addAll(PhpIndexUtil.getObjectInterfaces(strClass, objIndex, false));
            }
        }

        /* check methods, error on first one violated requirements */
        for (PhpClass objClass : listClasses) {
            if (null == objClass.findMethodByName("__toString")) {
                String strError = strClassHasNoToStringMessage.replace("%class%", objClass.getFQN());
                holder.registerProblem(objExpression, strError, ProblemHighlightType.ERROR);

                listClasses.clear();
                return true;
            }

        }

        /* terminate inspection, php will call __toString() */
        listClasses.clear();
        return true;
    }
}
