package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class MagicMethodsValidityInspector extends BasePhpInspection {
    private static final String strProblemUseSplAutoloading = "Common recommendation is to use spl_autoload_register(...) instead";

    private static final PhpType stringType      = new PhpType();
    private static final PhpType arrayOrNullType = new PhpType();
    static {
        stringType.add(PhpType.STRING);

        arrayOrNullType.add(PhpType.NULL);
        arrayOrNullType.add(PhpType.ARRAY);
    }

    @NotNull
    public String getShortName() {
        return "MagicMethodsValidityInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                String strMethodName = method.getName();
                if (StringUtil.isEmpty(strMethodName) || !strMethodName.startsWith("__") || null == method.getNameIdentifier()) {
                    return;
                }

                if (strMethodName.equals("__construct")) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    CanNotReturnTypeStrategy.apply(method, holder);
                    NormallyCallsParentMethodStrategy.apply(method, holder);

                    return;
                }

                if (
                    strMethodName.equals("__destruct") ||
                    strMethodName.equals("__clone")
                ) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    CanNotReturnTypeStrategy.apply(method, holder);
                    CanNotTakeArgumentsStrategy.apply(method, holder);
                    NormallyCallsParentMethodStrategy.apply(method, holder);

                    return;
                }

                if (
                    strMethodName.equals("__get") ||
                    strMethodName.equals("__isset") ||
                    strMethodName.equals("__unset")
                ) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    MustBePublicStrategy.apply(method, holder);
                    TakesExactAmountOfArgumentsStrategy.apply(1, method, holder);
                    CanNotTakeArgumentsByReferenceStrategy.apply(method, holder);

                    return;
                }

                if (
                    strMethodName.equals("__set") ||
                    strMethodName.equals("__call")
                ) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    MustBePublicStrategy.apply(method, holder);
                    CanNotTakeArgumentsByReferenceStrategy.apply(method, holder);
                    TakesExactAmountOfArgumentsStrategy.apply(2, method, holder);

                    return;
                }

                if (strMethodName.equals("__callStatic")) {
                    MustBePublicStrategy.apply(method, holder);
                    CanNotTakeArgumentsByReferenceStrategy.apply(method, holder);
                    TakesExactAmountOfArgumentsStrategy.apply(2, method, holder);
                    MustBeStaticStrategy.apply(method, holder);

                    return;
                }

                if (strMethodName.equals("__toString")) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    CanNotTakeArgumentsStrategy.apply(method, holder);
                    MustBePublicStrategy.apply(method, holder);
                    MustNotThrowExceptionsStrategy.apply(method, holder);
                    MustReturnSpecifiedTypeStrategy.apply(stringType, method, holder);

                    return;
                }

                if (strMethodName.equals("__debugInfo")) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    CanNotTakeArgumentsStrategy.apply(method, holder);
                    MustBePublicStrategy.apply(method, holder);
                    MustReturnSpecifiedTypeStrategy.apply(arrayOrNullType, method, holder);

                    return;
                }

                if (strMethodName.equals("__invoke")) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    MustBePublicStrategy.apply(method, holder);

                    return;
                }

                if (strMethodName.equals("__autoload")) {
                    holder.registerProblem(method.getNameIdentifier(), strProblemUseSplAutoloading, ProblemHighlightType.LIKE_DEPRECATED);
                    TakesExactAmountOfArgumentsStrategy.apply(1, method, holder);

                    //noinspection UnnecessaryReturnStatement
                    return;
                }
            }
        };
    }
}
