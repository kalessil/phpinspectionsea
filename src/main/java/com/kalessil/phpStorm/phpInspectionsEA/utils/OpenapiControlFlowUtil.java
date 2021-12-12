package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpInstruction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

final public class OpenapiControlFlowUtil {
    @NotNull
    public static List<PhpAccessVariableInstruction> getFollowingVariableAccessInstructions(@NotNull PhpInstruction point, @NotNull final CharSequence variableName) {
        final List<PhpAccessVariableInstruction> instructions               = new ArrayList<>();
        final Map<PsiElement, PhpAccessInstruction.Access> processedAnchors = new HashMap<>();
        for (final PhpAccessVariableInstruction entry: PhpControlFlowUtil.getFollowingVariableAccessInstructions(point, variableName, false)) {
            final PsiElement anchor                  = entry.getAnchor().getParent();
            final PhpAccessInstruction.Access access = entry.getAccess();
            if (! processedAnchors.containsKey(anchor) || ! processedAnchors.get(anchor).equals(access)) {
                processedAnchors.put(anchor, access);
                instructions.add(entry);
            }
        }
        processedAnchors.clear();
        return instructions;
    }
}
