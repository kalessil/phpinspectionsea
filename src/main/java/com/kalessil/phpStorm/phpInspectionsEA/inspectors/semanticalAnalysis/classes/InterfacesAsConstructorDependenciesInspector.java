package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InterfacesAsConstructorDependenciesInspector extends BasePhpInspection {
    @NotNull
    public String getShortName() {
        return "InterfacesAsConstructorDependenciesInspection";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                final String methodName = method.getName();
                if (methodName.equals("__construct") /* or setter */) {
                    final Parameter[] parameters = method.getParameters();
                    if (parameters.length > 0) {
                        final PhpIndex index = PhpIndex.getInstance(method.getProject());
                        for (final Parameter parameter : parameters) {
                            final PhpType type = parameter.getDeclaredType().filterPrimitives().filterUnknown();
                            if (type.size() == 1) {
                                this.analyze(
                                        parameter,
                                        OpenapiResolveUtil.resolveClassesByFQN(type.getTypes().iterator().next(), index)
                                );
                            }
                        }
                    }
                }
            }

            private void analyze(@NotNull Parameter parameter, @NotNull Collection<PhpClass> classes) {
                classes.removeIf(clazz -> clazz.isInterface() || clazz.isAbstract());
                if (classes.size() == 1) {
                    final PhpClass[] interfaces = classes.iterator().next().getImplementedInterfaces();
                    if (interfaces.length > 0) {

                    }
                }
                classes.clear();
            }
        };
    }
}
