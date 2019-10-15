package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.dateTime;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class DateIntervalSpecificationInspector extends PhpInspection {
    private static final String message = "Date interval specification seems to be invalid.";

    private static final Pattern regexDateTimeAlike;
    private static final Pattern regexRegular;
    static {
        regexDateTimeAlike = Pattern.compile("^P\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$");
        regexRegular       = Pattern.compile("^P((\\d+Y)?(\\d+M)?(\\d+D)?(\\d+W)?)?(T(?=\\d)(\\d+H)?(\\d+M)?(\\d+S)?)?$");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "DateIntervalSpecificationInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Date interval specification validity";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpNewExpression(@NotNull NewExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                /* before inspecting check parameters amount */
                final PsiElement[] params = expression.getParameters();
                if (params.length == 1) {
                    final ClassReference classReference = expression.getClassReference();
                    final String classFQN               = classReference == null ? null : classReference.getFQN();
                    if (classFQN == null || !classFQN.equals("\\DateInterval")) { /* TODO: child classes support */
                        return;
                    }

                    /* now try getting string literal and test against valid patterns */
                    final StringLiteralExpression pattern = ExpressionSemanticUtil.resolveAsStringLiteral(params[0]);
                    if (pattern != null && pattern.getFirstPsiChild() == null) {
                        final String input = pattern.getContents();
                        if (!regexRegular.matcher(input).find() && !regexDateTimeAlike.matcher(input).find()) {
                            holder.registerProblem(pattern, ReportingUtil.wrapReportedMessage(message));
                        }
                    }
                }
            }
        };
    }
}
