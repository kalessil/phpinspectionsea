package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class InterfacesAsConstructorDependenciesInspector extends PhpInspection {
    private static final String messageMissingContract = "The parameters' class doesn't implement any interfaces (contracts), consider introducing one (extensibility concerns).";
    private static final String messageUseContract     = "The parameters' type should be replaced with an interface (contract) (extensibility concerns).";

    // Inspection options.
    public boolean TOLERATE_MISSING_CONTRACTS = true;

    @NotNull
    @Override
    public String getShortName() {
        return "InterfacesAsConstructorDependenciesInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Interfaces as constructor dependencies";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.shouldSkipAnalysis(method, StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE)) { return; }

                final String methodName = method.getName();
                if (methodName.equals("__construct")) {
                    final Parameter[] parameters = method.getParameters();
                    if (parameters.length > 0) {
                        final PhpIndex index = PhpIndex.getInstance(holder.getProject());
                        for (final Parameter parameter : parameters) {
                            final PhpType type = OpenapiResolveUtil.resolveDeclaredType(parameter).filterPrimitives().filterUnknown();
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
                final List<PhpClass> filtered = classes.stream().filter(clazz -> !clazz.isInterface()).collect(Collectors.toList());
                if (filtered.size() == 1) {
                    final List<PhpClass> contracts = InterfacesExtractUtil.getCrawlInheritanceTree(filtered.iterator().next(), false).stream()
                            .filter(contract -> !contract.getNamespaceName().equals("\\") || contract.getFQN().indexOf('_') != -1)
                            .collect(Collectors.toList());
                    if (!contracts.isEmpty()) {
                        holder.registerProblem(parameter, messageUseContract);
                    } else if (!TOLERATE_MISSING_CONTRACTS) {
                        holder.registerProblem(parameter, messageMissingContract);
                    }
                    contracts.clear();
                }
                filtered.clear();
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component)
                -> component.addCheckbox("Tolerate classes without contracts", TOLERATE_MISSING_CONTRACTS, (value) -> TOLERATE_MISSING_CONTRACTS = value)
        );
    }
}
