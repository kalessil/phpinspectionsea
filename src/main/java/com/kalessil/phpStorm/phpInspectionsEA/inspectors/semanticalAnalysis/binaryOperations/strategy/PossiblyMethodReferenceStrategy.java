package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
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

final public class PossiblyMethodReferenceStrategy {
    private static final String message = "It was probably intended to use '->' here.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        final PsiElement operation = expression.getOperation();
        if (OpenapiTypesUtil.is(operation, PhpTokenTypes.opGREATER)) {
            final PsiElement right = expression.getRightOperand();
            if (OpenapiTypesUtil.isFunctionReference(right)) {
                final String functionName = ((FunctionReference) right).getName();
                if (functionName != null && !functionName.isEmpty()) {
                    final PsiElement left = expression.getLeftOperand();
                    if (left instanceof PhpTypedElement) {
                        final Project project  = holder.getProject();
                        final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) left, project);
                        if (resolved != null) {
                            final PhpIndex index = PhpIndex.getInstance(project);
                            for (final String type : resolved.filterUnknown().getTypes()) {
                                final String normalized = Types.getType(type);
                                if (normalized.startsWith("\\")) {
                                    final boolean hasMethod = OpenapiResolveUtil.resolveClassesAndInterfacesByFQN(normalized, index).stream()
                                            .anyMatch(clazz -> OpenapiResolveUtil.resolveMethod(clazz, functionName) != null);
                                    if (hasMethod) {
                                        holder.registerProblem(
                                                operation,
                                                ReportingUtil.wrapReportedMessage(message)
                                        );
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
