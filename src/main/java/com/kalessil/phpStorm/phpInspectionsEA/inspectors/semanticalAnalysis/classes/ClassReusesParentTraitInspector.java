package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.jetbrains.php.lang.psi.elements.PhpUseList;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ClassReusesParentTraitInspector extends BasePhpInspection {
    private static final String patternDirectDuplication   = "'%s' is already used in this same class.";
    private static final String patternIndirectDuplication = "'%s' is already used in '%s'.";

    @NotNull
    public String getShortName() {
        return "ClassReusesParentTraitInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (this.shouldSkipAnalysis(clazz, StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE)) { return; }

                if (!clazz.isInterface() && clazz.hasTraitUses()) {
                    final Set<PhpClass> classes = InterfacesExtractUtil.getCrawlInheritanceTree(clazz, true);
                    if (!classes.isEmpty()) {
                        final Map<PhpClass, List<String>> registry = new HashMap<>();
                        classes.forEach(c -> this.collectTraits(c, registry));

                        final Set<String> processed = new HashSet<>();
                        final List<String> traits   = registry.get(clazz);
                        for (final Map.Entry<PhpClass, List<String>> entry : registry.entrySet()) {
                            final PhpClass subject = entry.getKey();
                            if (subject == clazz)  {
                                /* case 1: direct traits duplication */
                                entry.getValue().stream().filter(candidate  -> !processed.contains(candidate))
                                        .forEach(candidate -> {
                                            final boolean hasDuplicates = traits.stream().filter(trait -> trait.equals(candidate)).count() > 1L;
                                            if (hasDuplicates && processed.add(candidate)) {
                                                final PsiElement target = this.resolveReportingTarget(clazz, candidate);
                                                if (target != null) {
                                                    holder.registerProblem(
                                                            target,
                                                            String.format(patternDirectDuplication, candidate)
                                                    );
                                                }
                                            }
                                        });
                            } else {
                                /* case 2: indirect traits duplication */
                                entry.getValue().stream().filter(candidate  -> !processed.contains(candidate))
                                        .forEach(candidate -> {
                                            final boolean hasDuplicates = traits.stream().anyMatch(trait -> trait.equals(candidate));
                                            if (hasDuplicates && processed.add(candidate)) {
                                                final PsiElement target = this.resolveReportingTarget(clazz, candidate);
                                                if (target != null) {
                                                    holder.registerProblem(
                                                            target,
                                                            String.format(patternIndirectDuplication, candidate, subject.getFQN())
                                                    );
                                                }
                                            }
                                        });
                            }
                        }
                        processed.clear();

                        registry.values().forEach(List::clear);
                        registry.clear();
                        classes.clear();
                    }
                }
            }

            @Nullable
            private PsiElement resolveReportingTarget(@NotNull PhpClass clazz, @NotNull String trait) {
                final List<PsiElement> candidates = new ArrayList<>();
                for (final PsiElement candidate : clazz.getChildren()) {
                    if (candidate instanceof PhpUseList) {
                        for (final PhpUse use : ((PhpUseList) candidate).getDeclarations()) {
                            final PsiElement reference = use.getFirstChild();
                            if (reference instanceof ClassReference) {
                                if (trait.equals(((ClassReference) reference).getFQN())) {
                                    candidates.add(reference);
                                }
                            }
                        }
                    }
                }
                final PsiElement target = !candidates.isEmpty() ? candidates.get(candidates.size() - 1): null;
                candidates.clear();
                return target;
            }

            private void collectTraits(@NotNull PhpClass clazz, @NotNull Map<PhpClass, List<String>> storage) {
                if (!clazz.isInterface() && !storage.containsKey(clazz) && clazz.hasTraitUses()) {
                    storage.computeIfAbsent(clazz, (key) -> new ArrayList<>());
                    for (final PhpClass trait : this.resolveImplementedTraits(clazz)) {
                        storage.get(clazz).add(trait.getFQN());
                        this.collectTraits(trait, storage);
                    }
                }
            }

            @NotNull
            private List<PhpClass> resolveImplementedTraits(@NotNull PhpClass clazz) {
                try {
                    /* precise alternative to OpenapiResolveUtil.resolveImplementedTraits: clazz.getTraits() skips duplicates */
                    final List<PhpClass> traits = new ArrayList<>();
                    for (final PsiElement candidate : clazz.getChildren()) {
                        if (candidate instanceof PhpUseList) {
                            for (final PhpUse use : ((PhpUseList) candidate).getDeclarations()) {
                                final PsiElement reference = use.getFirstChild();
                                if (reference instanceof ClassReference) {
                                    final PsiElement resolved = OpenapiResolveUtil.resolveReference((ClassReference) reference);
                                    if (resolved instanceof PhpClass) {
                                        traits.add((PhpClass) resolved);
                                    }
                                }
                            }
                        }
                    }
                    return traits;
                } catch (final Throwable error) {
                    if (error instanceof ProcessCanceledException) {
                        throw error;
                    }
                    return new ArrayList<>();
                }
            }
        };
    }
}
