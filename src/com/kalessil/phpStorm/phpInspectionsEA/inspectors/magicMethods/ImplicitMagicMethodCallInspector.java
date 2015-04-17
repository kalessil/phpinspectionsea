package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class ImplicitMagicMethodCallInspector extends BasePhpInspection {
    private static final String strProblemDescription      = "Implicit magic method calls shall be avoided as these methods used by PHP internals.";
    private static final String strProblemUseStringCasting = "Please use (string) %o% instead";

    private HashSet<String> methods = null;
    private HashSet<String> getMethods() {
        if (null == methods) {
            methods = new HashSet<String>();

            methods.add("__construct");
            methods.add("__destruct");
            methods.add("__call");
            methods.add("__callStatic");
            methods.add("__get");
            methods.add("__set");
            methods.add("__isset");
            methods.add("__unset");
            methods.add("__sleep");
            methods.add("__wakeup");
            methods.add("__toString");
            methods.add("__invoke");
            methods.add("__set_state");
            methods.add("__clone");
            methods.add("__debugInfo");
        }

        return methods;
    }

    @NotNull
    public String getShortName() {
        return "ImplicitMagicMethodCallInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethodReference(MethodReference reference) {
                String strMethodName = reference.getName();
                if (!StringUtil.isEmpty(strMethodName) && getMethods().contains(strMethodName)) {
                    String strReferenceObject = reference.getFirstChild().getText().trim();
                    if (!strReferenceObject.equals("$this") && !strReferenceObject.equals("parent")) {
                        String strMessage = strProblemDescription;
                        if (strMethodName.equals("__toString")) {
                            strMessage = strProblemUseStringCasting.replace("%o%", strReferenceObject);
                        }

                        holder.registerProblem(reference, strMessage, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
        };
    }
}
