package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.Collection;

public class NormallyCallsParentMethodStrategy {
    private static final String strProblemDescription = "%m% probably missing %c%::%m% call";

    static public void apply(final Method method, final ProblemsHolder holder) {
        String strMethodName = method.getName();
        PhpClass ownerClass = method.getContainingClass();
        if (StringUtil.isEmpty(strMethodName) || null == ownerClass || null == method.getNameIdentifier()) {
            return;
        }

        /* find parent with protected/public method, if not overrides anything, terminate inspection */
        PhpClass parentWithGivenMethod = null;
        for (PhpClass superClass : ownerClass.getSupers()) {
            if (!superClass.isInterface()) {
                Method objMethod = superClass.findOwnMethodByName(strMethodName);
                if (null != objMethod && !objMethod.getAccess().isPrivate()) {
                    parentWithGivenMethod = superClass;
                    break;
                }
            }
        }
        if (null == parentWithGivenMethod) {
            return;
        }

        /* find all calls inside methods */
        Collection<MethodReference> callStatements = PsiTreeUtil.findChildrenOfType(method, MethodReference.class);

        /* check if any of them calls parent */
        boolean isParentCallFound = false;
        for (MethodReference call : callStatements) {
            String strName = call.getName();
            if (!StringUtil.isEmpty(strName) && strName.equals(strMethodName)) {
                isParentCallFound = true;
                break;
            }
        }

        /* report the issue */
        if (!isParentCallFound) {
            String strMessage = strProblemDescription
                .replace("%m%", method.getName())
                .replace("%c%", parentWithGivenMethod.getName());
            holder.registerProblem(method.getNameIdentifier(), strMessage, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }

        callStatements.clear();
    }
}
