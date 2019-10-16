package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompositionAndInheritanceInspector extends PhpInspection {
    private static final String messageAbstract = "The class needs to be abstract (since it has children).";
    private static final String messageGeneric  = "The class needs to be either final (for aggregation) or abstract (for inheritance).";

    // Inspection options.
    public boolean FORCE_FINAL_OR_ABSTRACT = false;

    @NotNull
    @Override
    public String getShortName() {
        return "CompositionAndInheritanceInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Composition and inheritance";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (this.shouldSkipAnalysis(clazz, StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE)) { return; }

                final boolean hasNeededModifiers = clazz.isFinal() || clazz.isAbstract();
                if (!hasNeededModifiers && ! clazz.isInterface() && ! clazz.isTrait() && ! clazz.isAnonymous() && ! this.isAnnotated(clazz)) {
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
                        holder.registerProblem(nameNode, ReportingUtil.wrapReportedMessage(messageGeneric));
                    } else {
                        holder.registerProblem(nameNode, ReportingUtil.wrapReportedMessage(messageAbstract));
                    }
                }
            }

            private boolean isAnnotated(@NotNull PhpClass clazz) {
                final PhpDocTag[] tags = PsiTreeUtil.getChildrenOfType(clazz.getDocComment(), PhpDocTag.class);
                return tags != null && Arrays.stream(tags).anyMatch(t -> !t.getName().equals(t.getName().toLowerCase()));
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component)
            -> component.addCheckbox("Require all classes be final/abstract", FORCE_FINAL_OR_ABSTRACT, (isSelected) -> FORCE_FINAL_OR_ABSTRACT = isSelected)
        );
    }
}
