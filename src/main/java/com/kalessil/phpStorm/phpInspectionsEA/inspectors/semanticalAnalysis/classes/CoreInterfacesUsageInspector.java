package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class CoreInterfacesUsageInspector extends BasePhpInspection {
    private static final String patternsNoAlternatives   = "Provokes fatal error: '%s' can not be implemented by user classes.";
    private static final String patternsWithAlternatives = "Provokes fatal error: '%s' should be replaced with one of %s instead.";

    private final static Map<String, List<String>> mapping = new HashMap<>();
    static {
        mapping.put("\\Traversable", Arrays.asList("\\Iterator", "\\IteratorAggregate"));
        mapping.put("\\Throwable",   Arrays.asList("\\Exception", "\\Error"));
        mapping.put("\\DateTimeInterface", null);
    }

    @NotNull
    public String getShortName() {
        return "CoreInterfacesUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (this.shouldSkipAnalysis(clazz, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final List<ClassReference> implementedInterfaces = clazz.getImplementsList().getReferenceElements();
                if (!implementedInterfaces.isEmpty()) {
                    for (final ClassReference reference : implementedInterfaces) {
                        final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                        if (resolved instanceof PhpClass) {
                            final String classFQN = ((PhpClass) resolved).getFQN();
                            if (mapping.containsKey(classFQN)) {
                                final List<String> alternatives = mapping.get(classFQN);

                                final String message;
                                if (alternatives == null) {
                                    message = String.format(patternsNoAlternatives, classFQN);
                                } else {
                                    message = String.format(patternsWithAlternatives, classFQN, alternatives.toString());
                                }
                                holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);

                                break;
                            }
                        }
                    }
                }
            }
        };
    }
}
