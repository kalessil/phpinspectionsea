package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.parameters.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class InstanceOfCorrectnessStrategy {
    final private static String messageNotObject      = "It seems to be always false (no object types).";
    final private static String messageSameClass      = "It seems to be always true (same object type).";
    final private static String messageUnrelatedClass = "It seems to be always false (classes are not related).";
    final private static String patternCompareNull    = "'%s' can be used instead.";

    public static boolean apply(@NotNull ProblemsHolder holder, @NotNull Set<String> parameterTypes, @NotNull BinaryExpression binary) {
        boolean result         = false;
        final PsiElement left  = binary.getLeftOperand();
        final PsiElement right = binary.getRightOperand();
        if (left != null && right instanceof ClassReference) {
            final boolean isObject = parameterTypes.stream().anyMatch(t -> t.startsWith("\\") || t.equals(Types.strObject));
            if (isObject) {
                final PsiElement resolved = OpenapiResolveUtil.resolveReference((ClassReference) right);
                if (resolved instanceof PhpClass) {
                    final int typesCount         = parameterTypes.size();
                    final PhpClass resolvedClass = (PhpClass) resolved;
                    if (parameterTypes.contains(resolvedClass.getFQN())) {
                        if (typesCount == 1) {
                            holder.registerProblem(binary, messageSameClass);
                            result = true;
                        } else if (typesCount == 2 && parameterTypes.contains(Types.strNull)) {
                            final PsiElement parent  = binary.getParent();
                            final PsiElement target  = parent instanceof UnaryExpression ? parent : binary;
                            final String replacement = String.format("%s %s null", left.getText(), binary == target ? "!==" : "===");
                            holder.registerProblem(
                                    target,
                                    String.format(patternCompareNull, replacement),
                                    new CompareToNullFix(replacement)
                            );
                            result = true;
                        }
                    } else {
                        if (typesCount == 1) {
                            final Collection<PhpClass> classes = OpenapiResolveUtil.resolveClassesByFQN(
                                    parameterTypes.iterator().next(),
                                    PhpIndex.getInstance(holder.getProject())
                            );
                            if (classes.size() == 1) {
                                final Set<PhpClass> parents = InterfacesExtractUtil.getCrawlInheritanceTree(resolvedClass, true);
                                if (!parents.contains(classes.iterator().next())) {
                                    holder.registerProblem(binary, messageUnrelatedClass);
                                    result = true;
                                }
                            }
                        }
                    }
                }
            } else {
                holder.registerProblem(binary, messageNotObject);
                result = true;
            }
        }
        return result;
    }

    private static final class CompareToNullFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use null comparison instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        CompareToNullFix(@NotNull String expression) {
            super(expression);
        }
    }
}
