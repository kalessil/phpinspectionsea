package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class TraitsMethodsConflictsInspector extends BasePhpInspection {
    private static final String messagePattern = "Provokes a fatal error ('%s' method collision, see '%s').";

    @NotNull
    public String getShortName() {
        return "TraitsMethodsConflictsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(clazz))                  { return; }

                final PhpClass[] traits = clazz.getTraits();
                if (traits.length > 1) {
                    final Set<String> classMethods = Arrays.stream(clazz.getOwnMethods())
                            .map(PhpNamedElement::getName)
                            .collect(Collectors.toSet());
                    final Map<String, PhpClass> traitsMethods = new HashMap<>();
                    for (final PhpClass trait : traits) {
                        for (final Method method : trait.getOwnMethods()) {
                            final String methodName = method.getName();
                            if (!classMethods.contains(methodName)) {
                                if (traitsMethods.containsKey(methodName)) {
                                    this.report(clazz, methodName, trait, traitsMethods.get(methodName));
                                }
                                traitsMethods.putIfAbsent(methodName, trait);
                            }
                        }
                    }
                    traitsMethods.clear();
                    classMethods.clear();
                }
            }

            private void report(
                    @NotNull PhpClass clazz,
                    @NotNull String methodName,
                    @NotNull PhpClass trigger,
                    @NotNull PhpClass provider
            ) {
                for (final PsiElement child: clazz.getChildren()) {
                    if (child instanceof PhpUseList) {
                        for (final ClassReference reference : PsiTreeUtil.findChildrenOfType(child, ClassReference.class)) {
                            final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                            if (resolved instanceof PhpClass && trigger == resolved) {
                                holder.registerProblem(
                                        reference,
                                        String.format(messagePattern, methodName, provider.getFQN())
                                );
                                return;
                            }
                        }
                    }
                }
            }
        };
    }
}
