package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.parameters.strategy;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.Parameter;
import org.jetbrains.annotations.NotNull;

final public class InstanceofChecksCorrectnessStrategy {

    public static boolean apply(@NotNull Parameter parameter, @NotNull PsiElement context) {
        boolean result = false;
        if (context instanceof BinaryExpression) {
            final BinaryExpression binary = (BinaryExpression) context;
            if (binary.getOperationType() == PhpTokenTypes.kwINSTANCEOF && binary.getLeftOperand() == context) {
                        /* TODO: parameter type analyze */
            }
        }
        return result;
    }

}
