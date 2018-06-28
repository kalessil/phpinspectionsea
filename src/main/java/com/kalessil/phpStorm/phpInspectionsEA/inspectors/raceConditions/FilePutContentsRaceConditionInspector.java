package com.kalessil.phpStorm.phpInspectionsEA.inspectors.raceConditions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class FilePutContentsRaceConditionInspector extends BasePhpInspection {
    // Inspection options.
    public boolean REDUCED_SCOPE = true;

    private static final String message  = "A race condition can corrupt the file content. It would be a good idea to use LOCK_EX flag.";

    @NotNull
    public String getShortName() {
        return "FilePutContentsRaceConditionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("file_put_contents")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 2 && !this.isTestContext(reference)) {
                        /* there is no solid pattern, hence we are searching test fragments under the hood */
                        if (!REDUCED_SCOPE || this.match(arguments[0], ".php") || this.match(arguments[1], "<?php")) {
                            holder.registerProblem(reference, message, new AddLockExFlagFix());
                        }
                    }
                }
            }

            private boolean match(@NotNull PsiElement argument, @NotNull String fragment) {
                boolean result = false;
                final Set<PsiElement> variants = PossibleValuesDiscoveryUtil.discover(argument);
                if (!variants.isEmpty()) {
                    result = variants.stream().anyMatch(variant -> variant.getText().contains(fragment));
                    variants.clear();
                }
                return result;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component)
            -> component.addCheckbox("Report only php-code generation", REDUCED_SCOPE, (isSelected) -> REDUCED_SCOPE = isSelected)
        );
    }


    private static final class AddLockExFlagFix implements LocalQuickFix {
        private static final String title = "Add LOCK_EX as an argument";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference && !project.isDisposed()) {
                final FunctionReference call = (FunctionReference) expression;
                final ParameterList target   = call.getParameterList();
                if (target != null) {
                    final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, "f(null, null, LOCK_EX)");
                    final ParameterList implant         = replacement.getParameterList();
                    if (implant != null) {
                        final PsiElement[] placeholders = replacement.getParameters();
                        final PsiElement[] arguments    = call.getParameters();
                        placeholders[0].replace(arguments[0]);
                        placeholders[1].replace(arguments[1]);
                        target.replace(implant);
                    }
                }
            }
        }
    }
}
