package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPlatformResolverUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public class MustReturnSpecifiedTypeStrategy {
    private static final String messagePattern = "%m% must return %t%.";

    static public void apply(@NotNull PhpType allowedTypes, @NotNull Method method, @NotNull ProblemsHolder holder) {
        final String message = messagePattern.replace("%m%", method.getName()).replace("%t%", allowedTypes.toString());
        final Collection<PhpReturn> returnStatements = PsiTreeUtil.findChildrenOfType(method, PhpReturn.class);
        if (0 == returnStatements.size()) {
            final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
            if (null != nameNode) {
                holder.registerProblem(nameNode, message, ProblemHighlightType.ERROR);
            }

            return;
        }

        for (PhpReturn returnExpression : returnStatements) {
            PhpExpression returnValue        = ExpressionSemanticUtil.getReturnValue(returnExpression);
            PsiElement returnValueExpression = ExpressionSemanticUtil.getExpressionTroughParenthesis(returnValue);
            if (returnValueExpression instanceof PhpTypedElement) {
                final HashSet<String> resolvedArgumentType = new HashSet<>();
                TypeFromPlatformResolverUtil.resolveExpressionType(returnValueExpression, resolvedArgumentType);

                /*
                 * create type out of strings, resolved by plugin component handling magic in IDE internals,
                 * @see https://youtrack.jetbrains.com/issue/WI-31249
                 */
                final PhpType argumentType = new PhpType();
                if (resolvedArgumentType.size() > 0) {
                    resolvedArgumentType.forEach(argumentType::add);
                    resolvedArgumentType.clear();
                }

                if (PhpType.isSubType(argumentType, allowedTypes) || method != ExpressionSemanticUtil.getScope(returnExpression)) {
                    /* safe escape path for legal cases */
                    continue;
                }
            }

            holder.registerProblem(returnExpression, message, ProblemHighlightType.ERROR);
        }
        returnStatements.clear();
    }
}
