package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.PhpExit;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ExitUsageCorrectnessInspector extends PhpInspection {
    private static final String messagePattern = "Exit statuses should be in the range 0 to 254, %s is given.";

    @NotNull
    @Override
    public String getShortName() {
        return "ExitUsageCorrectnessInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'exit' usage correctness";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpExit(@NotNull PhpExit statement) {
                if (this.shouldSkipAnalysis(statement, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final PsiElement argument = statement.getArgument();
                if (argument != null) {
                    final Set<PsiElement> values = PossibleValuesDiscoveryUtil.discover(argument);
                    if (!values.isEmpty()) {
                        for (final PsiElement value : values) {
                            if (OpenapiTypesUtil.isNumber(value)) {
                                boolean isTarget;
                                int code;
                                try {
                                    code     = Integer.parseInt(value.getText());
                                    isTarget = code < 0 || code > 255;
                                } catch (final NumberFormatException wrongFormat) {
                                    code     = -1;
                                    isTarget = false;
                                }
                                if (isTarget) {
                                    holder.registerProblem(
                                            statement,
                                            String.format(ReportingUtil.wrapReportedMessage(messagePattern), code)
                                    );
                                    break;
                                }
                            }
                        }
                        values.clear();
                    }
                }
            }
        };
    }
}
