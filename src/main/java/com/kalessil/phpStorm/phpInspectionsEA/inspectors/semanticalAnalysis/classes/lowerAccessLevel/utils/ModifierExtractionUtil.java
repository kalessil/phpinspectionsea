package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClassMember;
import com.jetbrains.php.lang.psi.elements.PhpModifierList;
import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ModifierExtractionUtil {
    @Nullable
    public static PsiElement getProtectedModifier(final PhpClassMember subject) {
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
}
