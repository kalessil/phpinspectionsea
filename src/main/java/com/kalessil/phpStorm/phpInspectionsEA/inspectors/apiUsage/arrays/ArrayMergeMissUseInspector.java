package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ArrayMergeMissUseInspector extends BasePhpInspection {
    private static final String messageArrayPush  = "'array_push()' would fit more here (it also faster).";

    @NotNull
    public String getShortName() {
        return "ArrayMergeMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("array_merge")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length >= 2) {
                        /* case 1: `... = array_merge(..., [])` */
                        if (arguments.length == 2 && arguments[1] instanceof ArrayCreationExpression) {
                            final PsiElement[] elements = arguments[1].getChildren();
                            if (elements.length > 0 && Arrays.stream(elements).anyMatch(e -> !(e instanceof ArrayHashElement))) {
                                final PsiElement parent = reference.getParent();
                                if (OpenapiTypesUtil.isAssignment(parent)) {
                                    final PsiElement container = ((AssignmentExpression) parent).getVariable();
                                    if (container != null && OpeanapiEquivalenceUtil.areEqual(container, arguments[0])) {
                                        holder.registerProblem(parent, messageArrayPush);
                                    }
                                }
                            }
                        }

                        /* case 2: `array_merge(..., array_merge(), ...)` */
                    }
                }
            }
        };
    }
}
