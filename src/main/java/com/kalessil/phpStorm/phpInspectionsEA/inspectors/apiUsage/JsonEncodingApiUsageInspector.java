package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
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

    private static final String messageResultType     = "Please specify the second argument (clarifies decoding into array or object).";
    private static final String messageErrorsHandling = "Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.";

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
                if (functionName != null) {
                    if (functionName.equals("json_decode") && this.isFromRootNamespace(reference)) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (HARDEN_DECODING_RESULT_TYPE && arguments.length == 1) {
                            final String replacement = String.format(
                                    "%sjson_decode(%s, %s)",
                                    reference.getImmediateNamespaceName(),
                                    arguments[0].getText(),
                                    DECODE_AS_ARRAY ? "true" : "false"
                            );
                            holder.registerProblem(
                                    reference,
                                    ReportingUtil.wrapReportedMessage(messageResultType),
                                    DECODE_AS_ARRAY ? new DecodeIntoArrayFix(replacement) : new DecodeIntoObjectFix(replacement)
                            );
                        }
                        if (HARDEN_ERRORS_HANDLING && arguments.length > 0 && PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP730)) {
                            final boolean hasFlag = arguments.length >= 4 && PsiTreeUtil.findChildrenOfType(reference, ConstantReference.class).stream().anyMatch(r -> "JSON_THROW_ON_ERROR".equals(r.getName()));
                            if (!hasFlag) {
                                final String replacement = String.format(
                                        "%sjson_decode(%s, %s, %s, %s)",
                                        reference.getImmediateNamespaceName(),
                                        arguments[0].getText(),
                                        arguments.length > 1 ? arguments[1].getText() : (HARDEN_DECODING_RESULT_TYPE && DECODE_AS_ARRAY ? "true" : "false"),
                                        arguments.length > 2 ? arguments[2].getText() : "512",
                                        arguments.length > 3 ? "JSON_THROW_ON_ERROR | " + arguments[3].getText() : "JSON_THROW_ON_ERROR"
                                );
                                holder.registerProblem(
                                        reference,
                                        ReportingUtil.wrapReportedMessage(messageErrorsHandling),
                                        new HardenErrorsHandlingFix(replacement)
                                );
                            }
                        }
                    } else if (functionName.equals("json_encode") && this.isFromRootNamespace(reference)) {
                        if (HARDEN_ERRORS_HANDLING && PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP730)) {
                            final PsiElement[] arguments = reference.getParameters();
                            final boolean hasFlag        = arguments.length >= 2 && PsiTreeUtil.findChildrenOfType(reference, ConstantReference.class).stream().anyMatch(r -> "JSON_THROW_ON_ERROR".equals(r.getName()));
                            if (!hasFlag && arguments.length > 0) {
                                final String replacement = String.format(
                                        "%sjson_encode(%s, %s, %s)",
                                        reference.getImmediateNamespaceName(),
                                        arguments[0].getText(),
                                        arguments.length > 1 ? "JSON_THROW_ON_ERROR | " + arguments[1].getText() : "JSON_THROW_ON_ERROR",
                                        arguments.length > 2 ? arguments[2].getText() : "512"
                                );
                                holder.registerProblem(
                                        reference,
                                        ReportingUtil.wrapReportedMessage(messageErrorsHandling),
                                        new HardenErrorsHandlingFix(replacement)
                                );
                            }
                        }
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
                component.addCheckbox("Harden decoding return type", HARDEN_DECODING_RESULT_TYPE, (isSelected) -> HARDEN_DECODING_RESULT_TYPE = isSelected);
                component.delegateRadioCreation(radio -> {
                    radio.addOption("Prefer decoding as array", DECODE_AS_ARRAY, (isSelected) -> DECODE_AS_ARRAY = isSelected);
                    radio.addOption("Prefer decoding as object", DECODE_AS_OBJECT, (isSelected) -> DECODE_AS_OBJECT = isSelected);
                });
                component.addCheckbox("Harden errors handling", HARDEN_ERRORS_HANDLING, (isSelected) -> HARDEN_ERRORS_HANDLING = isSelected);
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

    private static final class HardenErrorsHandlingFix extends UseSuggestedReplacementFixer {
        private static final String title = "Harden errors handling";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        HardenErrorsHandlingFix(@NotNull String expression) {
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
