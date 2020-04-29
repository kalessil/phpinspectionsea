package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
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
                        final Set<PsiElement> fromVariants = PossibleValuesDiscoveryUtil.discover(arguments[0]);
                        if (fromVariants.size() == 1) {
                            final PsiElement from = fromVariants.iterator().next();
                            if (OpenapiTypesUtil.isNumber(from)) {
                                final Set<PsiElement> toVariants = PossibleValuesDiscoveryUtil.discover(arguments[1]);
                                if (toVariants.size() == 1) {
                                    final PsiElement to = toVariants.iterator().next();
                                    if (OpenapiTypesUtil.isNumber(to)) {
                                        boolean isTarget;
                                        try {
                                            isTarget = Long.parseLong(to.getText()) < Long.parseLong(from.getText());
                                        } catch (final NumberFormatException wrongFormat) {
                                            isTarget = false;
                                        }
                                        if (isTarget) {
                                            holder.registerProblem(
                                                    reference,
                                                    MessagesPresentationUtil.prefixWithEa(message)
                                            );
                                        }
                                    }
                                }
                                toVariants.clear();
                            }
                        }
                        fromVariants.clear();
                    }
                }
            }
        };
    }
}