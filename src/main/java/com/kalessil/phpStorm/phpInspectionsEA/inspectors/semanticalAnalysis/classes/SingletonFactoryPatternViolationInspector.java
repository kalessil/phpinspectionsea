package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;


import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpModifier;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

public class SingletonFactoryPatternViolationInspector extends BasePhpInspection {
    // Inspection options.
    public boolean SUGGEST_OVERRIDING_CLONE = false;

    private static final String messageClone                = "Singleton should also override __clone to prevent copying the instance";
    private static final String messageFactoryOrSingleton   = "Ensure that one of public getInstance/create*/from* methods are defined.";
    private static final String messageSingletonConstructor = "Singleton constructor should not be public (normally it's private).";

    @NotNull
    public String getShortName() {
        return "SingletonFactoryPatternViolationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                /* general class structure/type expectations */
                if (clazz.isTrait() || clazz.isInterface()) {
                    return;
                }
                final Method constructor  = clazz.getOwnConstructor();
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(clazz);
                if (null == constructor || null == nameNode) {
                    return;
                }
                final PhpModifier.Access constructorAccessModifiers = constructor.getAccess();

                final Method getInstance     = clazz.findOwnMethodByName("getInstance");
                final boolean hasGetInstance = (null != getInstance && getInstance.getAccess().isPublic());
                if (hasGetInstance) {
                    if (SUGGEST_OVERRIDING_CLONE && null == clazz.findOwnMethodByName("__clone")) {
                        holder.registerProblem(nameNode, messageClone, ProblemHighlightType.WEAK_WARNING);
                    }

                    if (constructorAccessModifiers.isPublic()){
                        /* private ones already covered with other inspections */
                        holder.registerProblem(nameNode, messageSingletonConstructor, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }

                    return;
                }

                /* ignore private in factories; private once covered by other inspection */
                if (!constructorAccessModifiers.isProtected()) {
                    return;
                }
                for (Method ownMethod: clazz.getOwnMethods()) {
                    final String methodName = ownMethod.getName();
                    if (methodName.startsWith("create") || methodName.startsWith("from")) {
                        return;
                    }
                }

                holder.registerProblem(nameNode, messageFactoryOrSingleton, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.createCheckbox("Suggest overriding __clone", SUGGEST_OVERRIDING_CLONE, (isSelected) -> SUGGEST_OVERRIDING_CLONE = isSelected);
        });
    }
}
