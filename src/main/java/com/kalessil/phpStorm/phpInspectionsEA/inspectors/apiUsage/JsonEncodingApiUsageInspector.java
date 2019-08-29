package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
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

public class JsonEncodingApiUsageInspector extends BasePhpInspection {
    // Inspection options.
    public boolean HARDEN_DECODING_RESULT_TYPE = true;
    public boolean DECODE_AS_ARRAY             = true;
    public boolean DECODE_AS_OBJECT            = false;
    public boolean HARDEN_ERRORS_HANDLING      = true;

    private static final String messageResultType = "Please specify the second argument (clarifies decoding into array or object).";

    @NotNull
    @Override
    public String getShortName() {
        return "JsonEncodingApiUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "JSON encoding API usage";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("json_decode")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (HARDEN_DECODING_RESULT_TYPE && arguments.length == 1) {
                        final String replacement = String.format(
                                "%sjson_decode(%s, %s)",
                                reference.getImmediateNamespaceName(),
                                arguments[0].getText(),
                                DECODE_AS_ARRAY ? "true" : "false"
                        );
                        holder.registerProblem(reference, messageResultType, DECODE_AS_ARRAY ? new DecodeIntoArrayFix(replacement) : new DecodeIntoObjectFix(replacement));
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
                component.addCheckbox("Harden decoding return type", HARDEN_DECODING_RESULT_TYPE, (isSelected) -> HARDEN_DECODING_RESULT_TYPE = isSelected); ;
                component.delegateRadioCreation(radio -> {
                    radio.addOption("Prefer decoding as array", DECODE_AS_ARRAY, (isSelected) -> DECODE_AS_ARRAY = isSelected);
                    radio.addOption("Prefer decoding as object", DECODE_AS_OBJECT, (isSelected) -> DECODE_AS_OBJECT = isSelected);
                });
                component.addCheckbox("Harden errors handling", HARDEN_ERRORS_HANDLING, (isSelected) -> HARDEN_ERRORS_HANDLING = isSelected); ;
        });
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
