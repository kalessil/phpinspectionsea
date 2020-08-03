package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class DuplicateArrayKeysInspector extends BasePhpInspection {
    private static final String messageDuplicateKey  = "The key is duplicated (and you should remove the outdated one).";
    private static final String messageDuplicatePair = "The key-value pair is duplicated (and you can safely remove it).";

    @NotNull
    @Override
    public String getShortName() {
        return "DuplicateArrayKeysInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Duplicate array keys";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            @Override
            public void visitPhpArrayCreationExpression(@NotNull ArrayCreationExpression expression) {
                final Map<String, PsiElement> processed = new HashMap<>();
                for (final ArrayHashElement pair : expression.getHashElements()) {
                    final PhpPsiElement key = pair.getKey();
                    if (key instanceof StringLiteralExpression && key.getFirstPsiChild() == null) {
                        final PsiElement value = pair.getValue();
                        if (value != null) {
                            final String literal = ((StringLiteralExpression) key).getContents();
                            if (processed.containsKey(literal)) {
                                final boolean isPairDuplicated = !(value instanceof ArrayCreationExpression) &&
                                                                 OpenapiEquivalenceUtil.areEqual(value, processed.get(literal));
                                if (isPairDuplicated) {
                                    holder.registerProblem(
                                            pair,
                                            MessagesPresentationUtil.prefixWithEa(messageDuplicatePair),
                                            ProblemHighlightType.LIKE_UNUSED_SYMBOL
                                    );
                                } else {
                                    holder.registerProblem(
                                            key,
                                            MessagesPresentationUtil.prefixWithEa(messageDuplicateKey)
                                    );
                                }
                            }
                            processed.put(literal, value);
                        }
                    }
                }
                processed.clear();
            }
        };
    }
}
