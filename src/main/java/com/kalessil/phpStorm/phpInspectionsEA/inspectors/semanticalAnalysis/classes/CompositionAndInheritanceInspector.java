package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompositionAndInheritanceInspector extends BasePhpInspection {
    private static final String messageAbstract = "The class needs to be abstract (since it has children).";
    private static final String messageGeneric  = "The class needs to be either final (for aggregation) or abstract (for inheritance).";

    // Inspection options.
    public boolean FORCE_FINAL_OR_ABSTRACT = false;

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
                    if (FORCE_FINAL_OR_ABSTRACT) {
                        this.report(clazz);
                    } else {
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
                                        this.report(clazz);
                                    }
                                    methods.clear();
                                }
                                contracts.clear();
                            }
                        } else {
                            if (parent.isAbstract()) {
                                this.report(clazz);
                            }
                        }
                    }
                }
            }

            private void report(@NotNull PhpClass clazz) {
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(clazz);
                if (nameNode != null) {
                    final PhpIndex index = PhpIndex.getInstance(holder.getProject());
                    if (OpenapiResolveUtil.resolveChildClasses(clazz.getFQN(), index).isEmpty()) {
                        holder.registerProblem(nameNode, messageGeneric);
                    } else {
                        holder.registerProblem(nameNode, messageAbstract);
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component)
            -> component.addCheckbox("Require all classes be final/abstract", FORCE_FINAL_OR_ABSTRACT, (isSelected) -> FORCE_FINAL_OR_ABSTRACT = isSelected)
        );
    }
}
