package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpClassMember;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.fixers.MakePrivateFixer;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.utils.ModifierExtractionUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ProtectedMembersOfFinalClassStrategy {
    private static final String message = "Since the class is final, the member can be declared private.";

    public static void apply(@NotNull PhpClassMember subject, @NotNull ProblemsHolder holder) {
        final PhpClass clazz          = subject.getContainingClass();
        final boolean isTargetContext = clazz != null && clazz.isFinal() && subject.getModifier().isProtected();
        if (isTargetContext && clazz.getSuperClass() == null &&!isOverride(subject, clazz)) {
            final PsiElement modifier = ModifierExtractionUtil.getProtectedModifier(subject);
            if (modifier != null) {
                holder.registerProblem(modifier, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new MakePrivateFixer(modifier));
            }
        }
    }

    private static boolean isOverride(@NotNull PhpClassMember member, @NotNull PhpClass clazz) {
        boolean result        = false;
        final PhpClass parent = clazz.getSuperClass();
        if (null != parent) {
            final String memberName = member.getName();
            final PhpClassMember parentMember
                    = member instanceof Field
                        ? parent.findFieldByName(memberName, ((Field) member).isConstant())
                        : parent.findMethodByName(memberName);
            result = parentMember != null;
        }
        return result;
    }
}
