package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.dateTime;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class DateIntervalSpecificationInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Date interval specification seems to be invalid.";
    private static final Pattern regexDateTimeAlike;
    private static final Pattern regexRegular;
    static {
        regexDateTimeAlike = Pattern.compile("^P\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$");
        regexRegular = Pattern.compile("^P((\\d+Y)?(\\d+M)?(\\d+D)?(\\d+W)?)?(T(\\d+H)?(\\d+M)?(\\d+S)?)?$");
    }

    @NotNull
    public String getShortName() {
        return "DateIntervalSpecificationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(
        @NotNull final ProblemsHolder holder,
        final boolean isOnTheFly
    ) {
        return new BasePhpElementVisitor() {
            public void visitPhpNewExpression(final NewExpression expression) {
                /* before inspecting check parameters amount */
                final PsiElement[] params = expression.getParameters();

                if (params.length != 1) {
                    return;
                }

                /* check if it's the target class */
                final ClassReference classReference = expression.getClassReference();

                if (classReference == null) {
                    return;
                }

                /* TODO: child classes support */
                final String classFQN = classReference.getFQN();

                if (!"\\DateInterval".equals(classFQN)) {
                    return;
                }

                /* now try getting string literal and test against valid patterns */
                final StringLiteralExpression pattern = ExpressionSemanticUtil.resolveAsStringLiteral(params[0]);

                if (pattern != null) {
                    final String patternText = pattern.getContents();

                    /* do not process patterns with inline variables */
                    if ((patternText.indexOf('$') >= 0) &&
                        !PsiTreeUtil.findChildrenOfType(pattern, Variable.class).isEmpty()) {
                        return;
                    }

                    final Matcher regexMatcher = regexRegular.matcher(patternText);
                    if (!regexMatcher.find()) {
                        final Matcher matcher = regexDateTimeAlike.matcher(patternText);

                        if (!matcher.find()) {
                            /* report the issue */
                            holder.registerProblem(pattern, strProblemDescription, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                }
            }
        };
    }
}
