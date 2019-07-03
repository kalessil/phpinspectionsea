package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ContractViolationInspector extends PhpInspection {
    private static final String messagePattern = "The public methods %s are not part of the class contracts. The contract might be incomplete.";

    @NotNull
    public String getShortName() {
        return "ContractViolationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (this.shouldSkipAnalysis(clazz, StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE)) { return; }

                if (!clazz.isInterface() && !clazz.isTrait()) {
                    final List<String> ownMethods = Arrays.stream(clazz.getOwnMethods())
                            .filter(method -> method.getAccess().isPublic())
                            .map(PhpNamedElement::getName)
                            .collect(Collectors.toList());
                    if (!ownMethods.isEmpty()) {
                        final Set<String> contractsMethods = this.getContractsMethods(clazz);
                        if (!contractsMethods.isEmpty()) {
                            final PhpClass parent         = OpenapiResolveUtil.resolveSuperClass(clazz);
                            final List<String> violations = ownMethods.stream()
                                    /* method is missing in contracts */
                                    .filter(own -> !contractsMethods.contains(own))
                                    /* method is missing in parent class */
                                    .filter(own -> parent == null || parent.findOwnMethodByName(own) == null)
                                    .collect(Collectors.toList());
                            if (!violations.isEmpty()) {
                                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(clazz);
                                if (nameNode != null) {
                                    Collections.sort(violations);
                                    holder.registerProblem(nameNode, String.format(messagePattern, String.join(", ", violations)));
                                }
                                violations.clear();
                            }
                            contractsMethods.clear();
                        }
                        ownMethods.clear();
                    }
                }
            }

            Set<String> getContractsMethods(@NotNull PhpClass clazz) {
                final Set<String> contractsMethods = new HashSet<>();
                InterfacesExtractUtil.getCrawlInheritanceTree(clazz, false).forEach(contract ->
                        Arrays.stream(contract.getOwnMethods())
                                .filter(method -> method.getAccess().isPublic())
                                .forEach(method -> contractsMethods.add(method.getName())));
                return contractsMethods;
            }
        };
    }
}
