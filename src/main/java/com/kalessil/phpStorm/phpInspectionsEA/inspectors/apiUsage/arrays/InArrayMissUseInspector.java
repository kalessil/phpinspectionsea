package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

public class InArrayMissUseInspector extends BasePhpInspection {
    private static final String messageStrictComparison   = "'%v% === %e%' should be used instead.";
    private static final String messageTolerateComparison = "'%v% == %e%' should be used instead.";
    private static final String patternKeyExists          = "'%e%' should be used instead. It is safe to refactor for type-safe code when the indexes are integers/strings only."; // NOTE-TR: It cannot be anything else...

    @NotNull
    public String getShortName() {
        return "InArrayMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* general structure requirements */
                final PsiElement[] params = reference.getParameters();
                final String functionName = reference.getName();
                if ((2 != params.length && 3 != params.length) || StringUtil.isEmpty(functionName) || !functionName.equals("in_array")) {
                    return;
                }

                /* Pattern: array_key_exists equivalence */
                if (params[1] instanceof FunctionReference) {
                    final FunctionReference subcall = (FunctionReference) params[1];
                    final String subcallName        = subcall.getName();
                    if (null != subcallName && subcallName.equals("array_keys")) {
                        /* ensure the subcall is a complete expression */
                        final PsiElement[] subcallParams = subcall.getParameters();
                        if (1 != subcallParams.length) {
                            return;
                        }

                        final String replacement = "array_key_exists(%k%, %a%)"
                            .replace("%a%", subcallParams[0].getText())
                            .replace("%k%", params[0].getText());
                        final String message     = patternKeyExists.replace("%e%", replacement);
                        holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new UseSuggestedReplacementFixer(replacement));

                        return;
                    }
                }

                /* Pattern: comparison equivalence */
                if (params[1] instanceof ArrayCreationExpression) {
                    int itemsCount      = 0;
                    PsiElement lastItem = null;
                    for (PsiElement oneItem : params[1].getChildren()) {
                        if (oneItem instanceof PhpPsiElement) {
                            ++itemsCount;
                            lastItem = oneItem;
                        }
                    }

                    lastItem = lastItem instanceof ArrayHashElement ? ((ArrayHashElement) lastItem).getValue() : lastItem;
                    if (itemsCount <= 1 && null != lastItem) {
                        // final boolean isInverted = false;

                        final boolean isStrict = 3 == params.length && PhpLanguageUtil.isTrue(params[2]);
                        final String message   = (isStrict ? messageStrictComparison : messageTolerateComparison)
                                .replace("%v%", lastItem.getText())
                                .replace("%e%", params[0].getText());
                        holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        // return;
                    }
                }
            }
        };
    }
}
