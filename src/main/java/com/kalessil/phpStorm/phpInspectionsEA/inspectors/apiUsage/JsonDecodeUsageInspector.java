package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class JsonDecodeUsageInspector extends BasePhpInspection {
    // Inspection options.
    public boolean DECODE_AS_ARRAY  = true;
    public boolean DECODE_AS_OBJECT = false;

    private static final String message = "Please specify the second argument (clarifies decoding into array or object).";

    @NotNull
    public String getShortName() {
        return "JsonDecodeUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("json_decode")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1) {
                        final String replacement = String.format(
                                "%sjson_decode(%s, %s)",
                                reference.getImmediateNamespaceName(),
                                arguments[0].getText(),
                                DECODE_AS_ARRAY ? "true" : "false"
                        );
                        holder.registerProblem(
                                reference,
                                message,
                                DECODE_AS_ARRAY ? new DecodeIntoArrayFix(replacement) : new DecodeIntoObjectFix(replacement)
                        );
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
                component.delegateRadioCreation(radio -> {
                    radio.addOption("Prefer decoding as array", DECODE_AS_ARRAY, (isSelected) -> DECODE_AS_ARRAY = isSelected);
                    radio.addOption("Prefer decoding as object", DECODE_AS_OBJECT, (isSelected) -> DECODE_AS_OBJECT = isSelected);
                })
        );
    }

    private static final class DecodeIntoArrayFix extends UseSuggestedReplacementFixer {
        private static final String title = "Decode into an array";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        DecodeIntoArrayFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class DecodeIntoObjectFix extends UseSuggestedReplacementFixer {
        private static final String title = "Decode into an object";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        DecodeIntoObjectFix(@NotNull String expression) {
            super(expression);
        }
    }
}
