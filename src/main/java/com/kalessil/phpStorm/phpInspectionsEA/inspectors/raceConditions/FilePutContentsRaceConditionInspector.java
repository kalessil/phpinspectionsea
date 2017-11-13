package com.kalessil.phpStorm.phpInspectionsEA.inspectors.raceConditions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
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

public class FilePutContentsRaceConditionInspector extends BasePhpInspection {
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
                final String functionName    = reference.getName();
                final PsiElement[] arguments = reference.getParameters();
                if (functionName != null && arguments.length == 2 && functionName.equals("file_put_contents")) {
                    /* there is no solid patter, hence we are searching test fragments under the hood */
                    final boolean isTarget = this.match(arguments[0], ".php") || this.match(arguments[1], "<?php");
                    if (isTarget) {
                        holder.registerProblem(reference, message);
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
}
