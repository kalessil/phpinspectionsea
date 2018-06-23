package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
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

public class InsufficientTypesControlInspector extends BasePhpInspection {
    private static final String message = "In multiple cases the result can be evaluated as false, consider hardening the check (e.g. '... !== false').";

    private static final Set<String> functions  = new HashSet<>();
    static {
        functions.add("array_search");
        functions.add("array_shift");
        functions.add("array_pop");

        functions.add("strpos");
        functions.add("stripos");
        functions.add("strrpos");
        functions.add("strripos");
        functions.add("strstr");
        functions.add("stristr");
        functions.add("substr");

        functions.add("mb_strpos");
        functions.add("mb_stripos");
        functions.add("mb_strrpos");
        functions.add("mb_strripos");
        functions.add("mb_strstr");
        functions.add("mb_stristr");
        functions.add("mb_substr");
    }

    @NotNull
    public String getShortName() {
        return "InsufficientTypesControlInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functions.contains(functionName) && reference.getParameters().length > 0) {
                    final boolean isTarget = ExpressionSemanticUtil.isUsedAsLogicalOperand(reference);
                    if (isTarget && this.isFromRootNamespace(reference)) {
                        final PsiElement target = NamedElementUtil.getNameIdentifier(reference);
                        if (target != null) {
                            holder.registerProblem(target, message);
                        }
                    }
                }
            }
        };
    }
}
