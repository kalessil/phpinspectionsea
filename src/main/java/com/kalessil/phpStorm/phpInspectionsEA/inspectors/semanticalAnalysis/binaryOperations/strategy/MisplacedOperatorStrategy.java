package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class MisplacedOperatorStrategy {
    private static final String message = "This operator is probably misplaced.";

    private static final Set<IElementType> operations = new HashSet<>();
    static {
        operations.add(PhpTokenTypes.opEQUAL);
        operations.add(PhpTokenTypes.opNOT_EQUAL);
        operations.add(PhpTokenTypes.opIDENTICAL);
        operations.add(PhpTokenTypes.opNOT_IDENTICAL);
        operations.add(PhpTokenTypes.opGREATER);
        operations.add(PhpTokenTypes.opGREATER_OR_EQUAL);
        operations.add(PhpTokenTypes.opLESS);
        operations.add(PhpTokenTypes.opLESS_OR_EQUAL);
    }

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        final PsiElement parent = expression.getParent().getParent();
        /* basic operator filter */
        if (parent instanceof FunctionReference && operations.contains(expression.getOperationType())) {
            final PsiElement[] params  = ((FunctionReference) parent).getParameters();
            final PsiElement candidate = params.length > 0 ? params[params.length - 1] : null;
            /* BO should be the last parameter of a call in logical contexts */
            if (candidate == expression && ExpressionSemanticUtil.isUsedAsLogicalOperand(parent)) {
                final PsiElement resolved = ((FunctionReference) parent).resolve();
                if (resolved instanceof Function) {
                    /* resolve the last parameter types: if bool not defined implicitly, continue */
                    final Function function   = (Function) resolved;
                    final Parameter parameter = (function).getParameters()[params.length - 1];
                    final Set<String> types   = new HashSet<>();
                    for (final String type : parameter.getType().filterUnknown().getTypes()) {
                        types.add(Types.getType(type));
                    }
                    if (!types.contains(Types.strBoolean)) {
                        final PsiElement rightOperand = expression.getRightOperand();
                        if (rightOperand instanceof PhpTypedElement) {
                            final Project project      = holder.getProject();
                            final PhpType allowedTypes = function.getType().global(project).filterUnknown();
                            final PhpType operandTypes
                                    = ((PhpTypedElement) rightOperand).getType().global(project).filterUnknown();
                            if (allowedTypes.getTypes().containsAll(operandTypes.getTypes())) {
                                holder.registerProblem(expression.getOperation(), message, ProblemHighlightType.GENERIC_ERROR);
                                return true;
                            }
                        }
                    }
                    types.clear();
                }
            }
        }

        return false;
    }
}
