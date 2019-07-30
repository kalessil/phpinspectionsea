package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
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

public class TraitsMethodsConflictsInspector extends PhpInspection {
    private static final String messagePattern = "Provokes a fatal error ('%s' method collision, see '%s').";

    @NotNull
    public String getShortName() {
        return "TraitsMethodsConflictsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (this.shouldSkipAnalysis(clazz, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                if (clazz.getTraitNames().length > 1) {
                    final Set<String> classMethods = Arrays.stream(clazz.getOwnMethods())
                            .map(PhpNamedElement::getName)
                            .collect(Collectors.toSet());
                    final Map<String, PhpClass> traitsMethods = new HashMap<>();
                    for (final Map.Entry<PhpClass, ClassReference> pair : this.extractTraits(clazz).entrySet()) {
                        for (final Method method : pair.getKey().getOwnMethods()) {
                            final String methodName = method.getName();
                            if (!classMethods.contains(methodName) && ExpressionSemanticUtil.getBlockScope(method) instanceof PhpClass) {
                                if (traitsMethods.containsKey(methodName)) {
                                    holder.registerProblem(
                                            pair.getValue(),
                                            String.format(messagePattern, methodName, traitsMethods.get(methodName).getFQN())
                                    );
                                }
                                traitsMethods.putIfAbsent(methodName, pair.getKey());
                            }
                        }
                    }
                    traitsMethods.clear();
                    classMethods.clear();
                }
            }

            private Map<PhpClass, ClassReference> extractTraits(@NotNull PhpClass clazz) {
                final Map<PhpClass, ClassReference> traits = new LinkedHashMap<>();
                for (final PsiElement child : clazz.getChildren()) {
                    if (child instanceof PhpUseList) {
                        for (final ClassReference reference : PsiTreeUtil.findChildrenOfType(child, ClassReference.class)) {
                            final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                            if (resolved instanceof PhpClass) {
                                traits.put((PhpClass) resolved, reference);
                            }
                        }
                    }
                }
                return traits;
            }
        };
    }
}
