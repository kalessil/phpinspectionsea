package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SuspiciousFunctionCallsInspector extends PhpInspection {
    private static final String message = "This call compares the same string with itself, this can not be right.";

    private static final Set<String> targetFunctions = new HashSet<>();
    static {
        targetFunctions.add("strcmp");
        targetFunctions.add("strncmp");
        targetFunctions.add("strcasecmp");
        targetFunctions.add("strncasecmp");
        targetFunctions.add("strnatcmp");
        targetFunctions.add("strnatcasecmp");
        targetFunctions.add("substr_compare");
        targetFunctions.add("hash_equals");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "SuspiciousFunctionCallsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Suspicious function calls";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && targetFunctions.contains(functionName)) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length >= 2 && arguments[0] != null && arguments[1] != null) {
                        final boolean isTarget = OpenapiEquivalenceUtil.areEqual(arguments[0], arguments[1]);
                        if (isTarget) {
                            holder.registerProblem(reference, message);
                        }
                    }
                }
            }
        };
    }
}
