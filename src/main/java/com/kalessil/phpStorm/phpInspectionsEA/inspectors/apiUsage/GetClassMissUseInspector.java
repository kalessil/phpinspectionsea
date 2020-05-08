package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpSwitch;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

public class GetClassMissUseInspector extends PhpInspection {
    private static final String message = "Might not work properly with child classes. Consider using instanceof construct instead.";

    @NotNull
    @Override
    public String getShortName() {
        return "GetClassMissUseInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'get_class(...)' misused";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("get_class")) {
                    final boolean isTarget = reference.getParent() instanceof PhpSwitch && this.isFromRootNamespace(reference);
                    if (isTarget) {
                        holder.registerProblem(
                                reference,
                                MessagesPresentationUtil.prefixWithEa(message)
                        );
                    }
                }
            }
        };
    }
}
