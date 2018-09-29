package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class StreamSelectUsageInspector extends BasePhpInspection {
    private static final String message = "Might cause high CPU usage connectivity issues (documentation advices using 200000 here, 200 ms).";

    @NotNull
    public String getShortName() {
        return "StreamSelectUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.isContainingFileSkipped(reference)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("stream_select")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 5) {
                        final boolean isTarget = OpenapiTypesUtil.isNumber(arguments[3]) &&
                                                 arguments[3].getText().equals("0");
                        if (isTarget) {
                            final Set<PsiElement> variants = PossibleValuesDiscoveryUtil.discover(arguments[4]);
                            if (variants.size() == 1) {
                                final PsiElement number = variants.iterator().next();
                                if (OpenapiTypesUtil.isNumber(number)) {
                                    try {
                                        if (Long.valueOf(number.getText()) < 200000) {
                                            holder.registerProblem(arguments[3], message);
                                        }
                                    } catch (final NumberFormatException failure) {
                                        return;
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
