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
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
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
    @Override
    public String getShortName() {
        return "TraitsMethodsConflictsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Traits methods conflicts resolution";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (this.shouldSkipAnalysis(clazz, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                if (clazz.getTraitNames().length > 1) {
                    final Map<PhpClass, ClassReference> usedTraits = this.extractTraits(clazz);
                    final Set<String> skipList                     = this.extractMethodsToSkip(clazz);

                    final Set<String> classMethods            = Arrays.stream(clazz.getOwnMethods()).map(PhpNamedElement::getName).collect(Collectors.toSet());
                    final Map<String, PhpClass> traitsMethods = new HashMap<>();
                    for (final Map.Entry<PhpClass, ClassReference> pair : usedTraits.entrySet()) {
                        final PhpClass currentClass = pair.getKey();
                        for (final Method method : currentClass.getOwnMethods()) {
                            final String methodName = method.getName();
                            if (! classMethods.contains(methodName) && ! method.isAbstract()) {
                                final PsiElement classCandidate = ExpressionSemanticUtil.getBlockScope(method);
                                if (classCandidate instanceof PhpClass) {
                                    if (traitsMethods.containsKey(methodName)) {
                                        final String origin    = traitsMethods.get(methodName).getFQN();
                                        final boolean isSolved = skipList.contains(method.getFQN()) || skipList.contains(origin + '.' + methodName);
                                        if (!isSolved) {
                                            holder.registerProblem(
                                                    pair.getValue(),
                                                    String.format(ReportingUtil.wrapReportedMessage(messagePattern), methodName, origin)
                                            );
                                        }
                                    }
                                    traitsMethods.putIfAbsent(methodName, currentClass);
                                }
                            }
                        }
                    }
                    traitsMethods.clear();
                    classMethods.clear();

                    usedTraits.clear();
                    skipList.clear();
                }
            }

            private Set<String> extractMethodsToSkip(@NotNull PhpClass clazz) {
                final Set<String> solved = new HashSet<>();
                for (final PsiElement child : clazz.getChildren()) {
                    if (child instanceof PhpUseList) {
                        for (final PhpTraitUseRule rule : PsiTreeUtil.findChildrenOfType(child, PhpTraitUseRule.class)) {
                            final PsiElement[] arguments = rule.getChildren();
                            if (arguments.length > 0) {
                                if (arguments[0] instanceof MethodReference) {
                                    final PsiElement resolved = OpenapiResolveUtil.resolveReference((MethodReference) arguments[0]);
                                    if (resolved instanceof Method) {
                                        solved.add(((Method) resolved).getFQN());
                                    }
                                }
                            }
                        }
                    }
                }
                return solved;
            }

            private Map<PhpClass, ClassReference> extractTraits(@NotNull PhpClass clazz) {
                final Map<PhpClass, ClassReference> traits = new LinkedHashMap<>();
                for (final PsiElement child : clazz.getChildren()) {
                    if (child instanceof PhpUseList) {
                        for (final ClassReference reference : PsiTreeUtil.findChildrenOfType(child, ClassReference.class)) {
                            final PsiElement context = reference.getParent();
                            if (context instanceof PhpUse) {
                                final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                                if (resolved instanceof PhpClass) {
                                    traits.put((PhpClass) resolved, reference);
                                }
                            }
                        }
                    }
                }
                return traits;
            }
        };
    }
}
