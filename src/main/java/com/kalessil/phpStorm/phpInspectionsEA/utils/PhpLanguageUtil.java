package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class PhpLanguageUtil {
    private static Pattern zeroNumberPattern = Pattern.compile("0?\\.0+");

    public static boolean isNull(@Nullable PsiElement expression) {
        if (expression instanceof ConstantReference) {
            final String name = ((ConstantReference) expression).getName();
            return name != null && name.equalsIgnoreCase("null");
        }
        return false;
    }

    public static boolean isTrue(@Nullable PsiElement expression) {
        if (expression instanceof ConstantReference) {
            final String name = ((ConstantReference) expression).getName();
            return name != null && name.equalsIgnoreCase("true");
        }
        return false;
    }

    public static boolean isFalse(@Nullable PsiElement expression) {
        if (expression instanceof ConstantReference) {
            final String name = ((ConstantReference) expression).getName();
            return name != null && name.equalsIgnoreCase("false");
        }
        return false;
    }

    public static boolean isBoolean(@Nullable PsiElement expression) {
        if (expression instanceof ConstantReference) {
            final String name = ((ConstantReference) expression).getName();
            return name != null && (name.equalsIgnoreCase("false") || name.equalsIgnoreCase("true"));
        }
        return false;
    }

    public static boolean isFalsyValue(@Nullable PsiElement expression) {
        if (expression instanceof StringLiteralExpression) {
            final StringLiteralExpression literal = (StringLiteralExpression) expression;
            if (literal.getFirstPsiChild() == null) {
                final String content = literal.getContents();
                return content.isEmpty() || content.equals("0");
            }
        } else if (expression instanceof ConstantReference) {
            return isFalse(expression) || isNull(expression);
        } else if (expression instanceof ArrayCreationExpression) {
            return expression.getChildren().length == 0;
        } else if (expression != null && OpenapiTypesUtil.isNumber(expression)) {
            final String content = expression.getText();
            return content.equals("0") || zeroNumberPattern.matcher(content).matches();
        }
        return false;
    }
}
