package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class MagicMethodsValidityInspector extends BasePhpInspection {
    private static final String messageUseSplAutoloading = "Prefer 'spl_autoload_register(...)' instead.";
    private static final String messageNotMagic          = "Only magic methods should start with '__'.";

    private static final PhpType arrayType         = new PhpType();
    private static final PhpType stringType        = new PhpType();
    private static final PhpType arrayOrNullType   = new PhpType();
    private static final Set<String> knownNonMagic = new HashSet<>();
    static {
        arrayType.add(PhpType.ARRAY);

        stringType.add(PhpType.STRING);

        arrayOrNullType.add(PhpType.NULL);
        arrayOrNullType.add(PhpType.ARRAY);

        knownNonMagic.add("__inject");
        knownNonMagic.add("__prepare");
        knownNonMagic.add("__toArray");
        knownNonMagic.add("__");
    }

    @NotNull
    public String getShortName() {
        return "MagicMethodsValidityInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                final PhpClass clazz      = method.getContainingClass();
                final String methodName   = method.getName();
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
                if (null == clazz || null == nameNode || method.isAbstract() || !methodName.startsWith("__")) {
                    return;
                }

                if (methodName.equals("__construct")) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    CanNotReturnTypeStrategy.apply(method, holder);
                    NormallyCallsParentMethodStrategy.apply(method, holder);

                    return;
                }

                if (
                    methodName.equals("__destruct") ||
                    methodName.equals("__clone")
                ) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    CanNotReturnTypeStrategy.apply(method, holder);
                    CanNotTakeArgumentsStrategy.apply(method, holder);
                    NormallyCallsParentMethodStrategy.apply(method, holder);

                    return;
                }

                if (
                    methodName.equals("__get") ||
                    methodName.equals("__isset") ||
                    methodName.equals("__unset")
                ) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    MustBePublicStrategy.apply(method, holder);
                    TakesExactAmountOfArgumentsStrategy.apply(1, method, holder);
                    CanNotTakeArgumentsByReferenceStrategy.apply(method, holder);
                    HasAlsoMethodStrategy.apply(method, "__set", holder);

                    return;
                }

                if (
                    methodName.equals("__set") ||
                    methodName.equals("__call")
                ) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    MustBePublicStrategy.apply(method, holder);
                    CanNotTakeArgumentsByReferenceStrategy.apply(method, holder);
                    TakesExactAmountOfArgumentsStrategy.apply(2, method, holder);

                    if (methodName.equals("__set")) {
                        HasAlsoMethodStrategy.apply(method, "__isset", holder);
                        HasAlsoMethodStrategy.apply(method, "__get", holder);
                    }

                    return;
                }

                if (methodName.equals("__callStatic")) {
                    MustBeStaticStrategy.apply(method, holder);
                    MustBePublicStrategy.apply(method, holder);
                    CanNotTakeArgumentsByReferenceStrategy.apply(method, holder);
                    TakesExactAmountOfArgumentsStrategy.apply(2, method, holder);

                    return;
                }

                if (methodName.equals("__toString")) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    CanNotTakeArgumentsStrategy.apply(method, holder);
                    MustBePublicStrategy.apply(method, holder);
                    MustNotThrowExceptionsStrategy.apply(method, holder);
                    MustReturnSpecifiedTypeStrategy.apply(stringType, method, holder);

                    return;
                }

                if (methodName.equals("__debugInfo")) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    CanNotTakeArgumentsStrategy.apply(method, holder);
                    MustBePublicStrategy.apply(method, holder);
                    MustReturnSpecifiedTypeStrategy.apply(arrayOrNullType, method, holder);
                    MinimalPhpVersionStrategy.apply(method, holder, PhpLanguageLevel.PHP560);

                    return;
                }

                if (methodName.equals("__set_state")) {
                    MustBeStaticStrategy.apply(method, holder);
                    MustBePublicStrategy.apply(method, holder);
                    TakesExactAmountOfArgumentsStrategy.apply(1, method, holder);
                    MustReturnSpecifiedTypeStrategy.apply((new PhpType()).add(clazz.getFQN()), method, holder);

                    return;
                }

                if (methodName.equals("__invoke")) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    MustBePublicStrategy.apply(method, holder);

                    return;
                }

                if (methodName.equals("__wakeup")) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    MustBePublicStrategy.apply(method, holder);
                    CanNotTakeArgumentsStrategy.apply(method, holder);
                    CanNotReturnTypeStrategy.apply(method, holder);

                    return;
                }

                if (methodName.equals("__sleep")) {
                    CanNotBeStaticStrategy.apply(method, holder);
                    MustBePublicStrategy.apply(method, holder);
                    CanNotTakeArgumentsStrategy.apply(method, holder);
                    MustReturnSpecifiedTypeStrategy.apply(arrayType, method, holder);

                    return;
                }

                if (methodName.equals("__autoload")) {
                    holder.registerProblem(nameNode, messageUseSplAutoloading, ProblemHighlightType.LIKE_DEPRECATED);
                    TakesExactAmountOfArgumentsStrategy.apply(1, method, holder);

                    return;
                }

                if (!knownNonMagic.contains(methodName)) {
                    holder.registerProblem(nameNode, messageNotMagic);
                }
            }
        };
    }
}
