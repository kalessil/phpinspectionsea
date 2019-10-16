package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class StreamSelectUsageInspector extends PhpInspection {
    private static final String message = "Might cause high CPU usage and connectivity issues (documentation advices using 200000 here, 200 ms).";

    @NotNull
    @Override
    public String getShortName() {
        return "StreamSelectUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'stream_select(...)' usage correctness";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("stream_select")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 5) {
                        final PsiElement seconds = arguments[3];
                        final boolean isTarget   = OpenapiTypesUtil.isNumber(seconds) && seconds.getText().equals("0");
                        if (isTarget) {
                            final PsiElement microseconds  = arguments[4];
                            final Set<PsiElement> variants = PossibleValuesDiscoveryUtil.discover(microseconds);
                            if (variants.size() == 1) {
                                final PsiElement threshold = variants.iterator().next();
                                if (OpenapiTypesUtil.isNumber(threshold)) {
                                    try {
                                        if (Long.parseLong(threshold.getText()) < 200000) {
                                            holder.registerProblem(
                                                    microseconds,
                                                    ReportingUtil.wrapReportedMessage(message)
                                            );
                                        }
                                    } catch (final NumberFormatException failure) {
                                        // do nothing
                                    }
                                }
                            }
                            variants.clear();
                        }
                    }
                }
            }
        };
    }
}
