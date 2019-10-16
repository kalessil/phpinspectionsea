package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ConstantConditionsConstantsValuesStrategy {
    private static final String messageAlwaysTrue  = "'%s' seems to be always true.";
    private static final String messageAlwaysFalse = "'%s' seems to be always false.";

    private static final Map<String, Set<String>> possibleValues = new HashMap<>();
    static {
        possibleValues.put("PHP_SAPI",      new HashSet<>(Arrays.asList("aolserver", "apache", "apache2filter", "apache2handler", "caudium", "cgi", "cgi-fcgi", "cli", "cli-server", "continuity", "embed", "fpm-fcgi", "isapi", "litespeed", "milter", "nsapi", "phpdbg", "phttpd", "pi3web", "roxen", "thttpd", "tux", "webjames")));
        possibleValues.put("PHP_OS_FAMILY", new HashSet<>(Arrays.asList("Windows", "BSD", "Darwin", "Solaris", "Linux", "Unknown")));
    }

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result               = false;
        final IElementType operation = expression.getOperationType();
        if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operation)) {
            PsiElement constant = expression.getLeftOperand();
            constant            = constant instanceof ConstantReference ? constant : expression.getRightOperand();
            if (constant instanceof ConstantReference) {
                final String constantName = ((ConstantReference) constant).getName();
                if (constantName != null && possibleValues.containsKey(constantName)) {
                    final PsiElement value = OpenapiElementsUtil.getSecondOperand(expression, constant);
                    if (value instanceof StringLiteralExpression) {
                        final String valueContent = ((StringLiteralExpression) value).getContents();
                        if (!valueContent.isEmpty() && !possibleValues.get(constantName).contains(valueContent)) {
                            final boolean match  = operation == PhpTokenTypes.opIDENTICAL || operation == PhpTokenTypes.opEQUAL;
                            holder.registerProblem(
                                    expression,
                                    String.format(ReportingUtil.wrapReportedMessage(match ? messageAlwaysFalse : messageAlwaysTrue), expression.getText())
                            );
                            result = true;
                        }
                    }
                }
            }
        }
        return result;
    }
}
