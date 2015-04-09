package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods;

import com.intellij.codeInspection.ProblemsHolder;
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

                //__construct:
                    CanNotBeStaticStrategy.apply();
                    CanNotReturnTypeStrategy.apply();

                //__destruct, __clone:
                    CanNotBeStaticStrategy.apply();
                    CanNotReturnTypeStrategy.apply();
                    CanNotTakeArgumentsStrategy.apply();

                //__get, __isset, __unset:
                    CanNotBeStaticStrategy.apply();
                    MustBePublicStrategy.apply();
                    TakesExactAmountOfArgumentsStrategy.apply(1);
                    CanNotTakeArgumentsByReferenceStrategy.apply();

                //__set, __call:
                    CanNotBeStaticStrategy.apply();
                    MustBePublicStrategy.apply();
                    CanNotTakeArgumentsByReferenceStrategy.apply();
                    TakesExactAmountOfArgumentsStrategy.apply(2);

                //__callStatic:
                    MustBePublicStrategy.apply();
                    CanNotTakeArgumentsByReferenceStrategy.apply();
                    TakesExactAmountOfArgumentsStrategy.apply(2);
                    MustBeStaticStrategy.apply();

                //__toString:
                    CanNotBeStaticStrategy.apply();
                    CanNotTakeArgumentsStrategy.apply();
                    MustBePublicStrategy.apply();
                    MustNotThrowExceptionsStrategy.apply();
                    MustReturnSpecifiedTypeStrategy.apply("string");

                //__debugInfo:
                    CanNotBeStaticStrategy.apply();
                    CanNotTakeArgumentsStrategy.apply();
                    MustBePublicStrategy.apply();
                    MustReturnSpecifiedTypeStrategy.apply("array|null");

                //__invoke:
                    CanNotBeStaticStrategy.apply();
                    MustBePublicStrategy.apply();

                //__autoload:
                    TakesExactAmountOfArgumentsStrategy.apply(1);
                    // deprecate - use spl_autoload_register instead
            }
        };
    }
}
