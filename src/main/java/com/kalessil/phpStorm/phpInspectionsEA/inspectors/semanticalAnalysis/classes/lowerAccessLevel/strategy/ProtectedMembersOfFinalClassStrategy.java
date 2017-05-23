package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpClassMember;
import com.jetbrains.php.lang.psi.elements.PhpModifierList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        if (isTargetContext && !isOverride(subject, clazz)) {
            final PsiElement modifier = getProtectedModifier(subject);
            if (modifier != null) {
                holder.registerProblem(modifier, message, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }

    @Nullable
    private static PsiElement getProtectedModifier(final PhpClassMember subject) {
        final PsiElement expression      = (subject instanceof Field) ? subject.getParent() : subject;
        final PhpModifierList list       = PsiTreeUtil.findChildOfType(expression, PhpModifierList.class);
        final LeafPsiElement[] modifiers = PsiTreeUtil.getChildrenOfType(list, LeafPsiElement.class);
        PsiElement result                = null;
        if (modifiers != null) {
            for (final LeafPsiElement modifier : modifiers) {
                if (modifier.getText().equalsIgnoreCase("protected")) {
                    result = modifier;
                    break;
                }
            }
        }
        return result;
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
