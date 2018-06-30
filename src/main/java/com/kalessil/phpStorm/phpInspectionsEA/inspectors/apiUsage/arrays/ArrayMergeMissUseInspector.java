package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ArrayMergeMissUseInspector extends BasePhpInspection {
    private static final String messageArrayPush         = "'array_push(...)' would fit more here (it also faster).";
    private static final String messageNestedMerge       = "Inlining nested 'array_merge(...)' in arguments is possible here (it also faster).";
    private static final String messageDuplicateArgument = "This value used as an argument multiple times (this occurrence can be dropped).";

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
                                        final List<String> fragments = new ArrayList<>();
                                        fragments.add(arguments[0].getText());
                                        Arrays.stream(elements).forEach(e -> fragments.add(e.getText()));

                                        final String replacement = String.format("array_push(%s)", String.join(", ", fragments));
                                        holder.registerProblem(parent, messageArrayPush, new UseArrayPushFixer(replacement));

                                        fragments.clear();
                                    }
                                }
                            }
                        }

                        /* case 2: `array_merge(..., array_merge(), ...)` */
                        for (final PsiElement argument : arguments) {
                            if (OpenapiTypesUtil.isFunctionReference(argument)) {
                                final String innerFunctionName = ((FunctionReference) argument).getName();
                                if (innerFunctionName != null && innerFunctionName.equals("array_merge")) {
                                    holder.registerProblem(reference, messageNestedMerge);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseArrayPushFixer extends UseSuggestedReplacementFixer {
        private static final String title = "Use array_push(...) instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseArrayPushFixer(@NotNull String expression) {
            super(expression);
        }
    }
}
