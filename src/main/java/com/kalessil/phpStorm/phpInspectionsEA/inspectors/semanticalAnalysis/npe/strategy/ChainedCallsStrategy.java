package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiPsiSearchUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ChainedCallsStrategy {
    private static final String message = "Null pointer exception may occur here.";

    public static void apply(@NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement operator = OpenapiPsiSearchUtil.findResolutionOperator(reference);
        final PsiElement base     = operator == null ? null : reference.getFirstPsiChild();
        if (base instanceof FunctionReference && PhpTokenTypes.ARROW == operator.getNode().getElementType()) {
            final PhpType types = ((FunctionReference) base).getType().global(holder.getProject()).filterUnknown();
            for (final String resolvedType : types.getTypes()) {
                final String type = Types.getType(resolvedType);
                if (type.equals(Types.strNull) || type.equals(Types.strVoid)) {
                    holder.registerProblem(operator, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    break;
                }
            }
        }
    }
}
