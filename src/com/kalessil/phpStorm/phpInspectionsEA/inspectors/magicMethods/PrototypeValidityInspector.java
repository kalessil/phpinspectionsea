package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class PrototypeValidityInspector extends BasePhpInspection {
    /**
     * static error messages here
     */

    @NotNull
    public String getShortName() {
        return "PrototypeValidityInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                String strMethodName = method.getName();
                if (StringUtil.isEmpty(strMethodName) || !strMethodName.startsWith("__")) {
                    return;
                }

                if (strMethodName.equals("__construct")) {
                    CanNotBeStaticStrategy.apply();
                    CanNotReturnTypeStrategy.apply();

                    return;
                }

                if (
                    strMethodName.equals("__destruct") ||
                    strMethodName.equals("__clone")
                ) {
                    CanNotBeStaticStrategy.apply();
                    CanNotReturnTypeStrategy.apply();
                    CanNotTakeArgumentsStrategy.apply();

                    return;
                }

                if (
                    strMethodName.equals("__get") ||
                    strMethodName.equals("__isset") ||
                    strMethodName.equals("__unset")
                ) {
                    CanNotBeStaticStrategy.apply();
                    MustBePublicStrategy.apply();
                    TakesExactAmountOfArgumentsStrategy.apply(1);
                    CanNotTakeArgumentsByReferenceStrategy.apply();

                    return;
                }

                if (
                    strMethodName.equals("__set") ||
                    strMethodName.equals("__call")
                ) {
                    CanNotBeStaticStrategy.apply();
                    MustBePublicStrategy.apply();
                    CanNotTakeArgumentsByReferenceStrategy.apply();
                    TakesExactAmountOfArgumentsStrategy.apply(2);

                    return;
                }

                if (strMethodName.equals("__callStatic")) {
                    MustBePublicStrategy.apply();
                    CanNotTakeArgumentsByReferenceStrategy.apply();
                    TakesExactAmountOfArgumentsStrategy.apply(2);
                    MustBeStaticStrategy.apply();

                    return;
                }

                if (strMethodName.equals("__toString")) {
                    CanNotBeStaticStrategy.apply();
                    CanNotTakeArgumentsStrategy.apply();
                    MustBePublicStrategy.apply();
                    MustNotThrowExceptionsStrategy.apply();
                    MustReturnSpecifiedTypeStrategy.apply("string");

                    return;
                }

                if (strMethodName.equals("__debugInfo")) {
                    CanNotBeStaticStrategy.apply();
                    CanNotTakeArgumentsStrategy.apply();
                    MustBePublicStrategy.apply();
                    MustReturnSpecifiedTypeStrategy.apply("array|null");

                    return;
                }

                if (strMethodName.equals("__invoke")) {
                    CanNotBeStaticStrategy.apply();
                    MustBePublicStrategy.apply();

                    return;
                }

                if (strMethodName.equals("__autoload")) {
                    TakesExactAmountOfArgumentsStrategy.apply(1);
                    // deprecate - use spl_autoload_register instead

                    return;
                }
            }
        };
    }
}
