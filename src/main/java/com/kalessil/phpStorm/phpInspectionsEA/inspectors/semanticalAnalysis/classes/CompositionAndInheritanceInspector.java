package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompositionAndInheritanceInspector extends BasePhpInspection {
    private static final String messageForced   = "The class needs to be either final or abstract.";
    private static final String messageAbstract = "The class needs to be abstract (since it has children).";
    private static final String messageFinal    = "The class needs to be final (for aggregation) or abstract (for inheritance).";

    /* TODO: option: require all classes be final/abstract */

    @NotNull
    public String getShortName() {
        return "CompositionAndInheritanceInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final boolean hasNeededModifiers = clazz.isFinal() || clazz.isAbstract();
                if (!hasNeededModifiers && !clazz.isInterface() && !clazz.isTrait() && !clazz.isAnonymous()) {
                    final PhpClass parent = OpenapiResolveUtil.resolveSuperClass(clazz);
                    if (parent == null) {
                        final List<PhpClass> contracts = OpenapiResolveUtil.resolveImplementedInterfaces(clazz);
                        if (!contracts.isEmpty()) {
                            final Set<String> methods = new HashSet<>();
                            contracts.forEach(c -> c.getMethods().forEach(method -> methods.add(method.getName())));
                            if (!methods.isEmpty()) {
                                final boolean isTarget = Arrays.stream(clazz.getOwnMethods())
                                        .filter(m -> m.getAccess().isPublic())
                                        .map(PhpNamedElement::getName)
                                        .allMatch(methods::contains);
                                if (isTarget) {
                                    final PhpIndex index = PhpIndex.getInstance(holder.getProject());
                                    if (OpenapiResolveUtil.resolveChildClasses(clazz.getFQN(), index).isEmpty()) {
                                        // final
                                    } else {
                                        // abstract
                                    }
                                }
                                methods.clear();
                            }
                            contracts.clear();
                        }
                    } else {
                        if (parent.isAbstract()) {
                            final PhpIndex index = PhpIndex.getInstance(holder.getProject());
                            if (OpenapiResolveUtil.resolveChildClasses(clazz.getFQN(), index).isEmpty()) {
                                // final
                            } else {
                                // abstract
                            }
                        }
                    }
                }
            }
        };
    }
}
