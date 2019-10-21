package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class MustReturnSpecifiedTypeStrategy {
    private static final String messagePattern = "%s must return %s.";

    static public void apply(@NotNull PhpType allowedTypes, @NotNull Method method, @NotNull ProblemsHolder holder) {
        final Collection<PhpReturn> returns = PsiTreeUtil.findChildrenOfType(method, PhpReturn.class);
        if (returns.isEmpty()) {
            final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
            if (nameNode != null) {
                final PhpType withoutStatic = allowedTypes.filter((new PhpType()).add(Types.strStatic));
                holder.registerProblem(
                        nameNode,
                        String.format(ReportingUtil.wrapReportedMessage(messagePattern), method.getName(), withoutStatic.toString()),
                        ProblemHighlightType.ERROR
                );
            }
        } else {
            final PhpType withoutStatic = allowedTypes.filter((new PhpType()).add(Types.strStatic));
            final Project project       = holder.getProject();
            for (final PhpReturn expression : returns) {
                final PsiElement returnValue = ExpressionSemanticUtil.getExpressionTroughParenthesis(ExpressionSemanticUtil.getReturnValue(expression));
                if (returnValue instanceof PhpTypedElement) {
                    /* previously we had an issue with https://youtrack.jetbrains.com/issue/WI-31249 here */
                    final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) returnValue, project);
                    if (resolved != null) {
                        final PhpType normalized = resolved.filterUnknown();
                        /* case: resolve has failed or resolved types are compatible */
                        if (normalized.isEmpty() || PhpType.isSubType(normalized, allowedTypes)) {
                            continue;
                        }
                        /* case: closure or anonymous class */
                        else if (method != ExpressionSemanticUtil.getScope(expression)) {
                            continue;
                        }
                    }
                }
                holder.registerProblem(
                        expression,
                        String.format(ReportingUtil.wrapReportedMessage(messagePattern), method.getName(), withoutStatic.toString()),
                        ProblemHighlightType.ERROR
                );
            }
            returns.clear();
        }
    }
}
