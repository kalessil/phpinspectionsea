package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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

public class IncorrectRandomRangeInspector extends BasePhpInspection {
    private static final String message = "The range is not defined properly.";

    private static final Set<String> functions = new HashSet<>();
    static {
        functions.add("mt_rand");
        functions.add("random_int");
        functions.add("rand");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "IncorrectRandomRangeInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Incorrect random generation range";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functions.contains(functionName)) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 2) {
                        final PsiElement from = arguments[0];
                        final PsiElement to   = arguments[1];
                        if (OpenapiTypesUtil.isNumber(to) && OpenapiTypesUtil.isNumber(from)) {
                            boolean isTarget;
                            try {
                                isTarget = Integer.parseInt(to.getText()) < Integer.parseInt(from.getText());
                            } catch (final NumberFormatException wrongFormat) {
                                isTarget = false;
                            }
                            if (isTarget) {
                                holder.registerProblem(reference, message);
                            }
                        }
                    }
                }
            }
        };
    }
}