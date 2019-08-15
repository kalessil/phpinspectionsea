package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.Field;
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

final public class ModifierExtractionUtil {
    @Nullable
    public static PsiElement getProtectedModifier(@NotNull PhpClassMember subject) {
        final LeafPsiElement[] modifiers = PsiTreeUtil.getChildrenOfType(
                PsiTreeUtil.findChildOfType((subject instanceof Field) ? subject.getParent() : subject, PhpModifierList.class),
                LeafPsiElement.class
        );
        if (modifiers != null && modifiers.length > 0) {
            for (final LeafPsiElement modifier : modifiers) {
                if (modifier.getNode().getElementType() == PhpTokenTypes.kwPROTECTED) {
                    return modifier;
                }
            }
        }
        return null;
    }
}
