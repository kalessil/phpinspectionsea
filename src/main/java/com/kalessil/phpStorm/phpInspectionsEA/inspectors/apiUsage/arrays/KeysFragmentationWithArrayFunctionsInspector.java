package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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

public class KeysFragmentationWithArrayFunctionsInspector extends PhpInspection {
    private static final String message = "Result keys set might be fragmented, wrapping with 'array_values(...)' is recommended.";

    final private static Set<String> targetFunctions = new HashSet<>();
    static {
        targetFunctions.add("array_filter");
        targetFunctions.add("array_unique");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "KeysFragmentationWithArrayFunctionsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Array keys set fragmentation";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && targetFunctions.contains(functionName)) {
                    final PsiElement[] arguments = reference.getParameters();
                    final PsiElement parent      = reference.getParent();
                    if (arguments.length > 0) {
                        if (OpenapiTypesUtil.isAssignment(parent)) {
                            final PsiElement candidate = ((AssignmentExpression) parent).getVariable();
                            if (candidate instanceof Variable) {
                                final Function scope      = ExpressionSemanticUtil.getScope(reference);
                                final GroupStatement body = scope == null ? null : ExpressionSemanticUtil.getGroupStatement(scope);
                                if (body != null) {
                                    final String variableName = ((Variable) candidate).getName();
                                    for (final PsiElement usage : PsiTreeUtil.findChildrenOfAnyType(body, ArrayAccessExpression.class, FunctionReference.class)) {
                                        if (usage instanceof ArrayAccessExpression) {
                                            final ArrayAccessExpression access = (ArrayAccessExpression) usage;
                                            final PsiElement container         = access.getValue();
                                            if (container instanceof Variable) {
                                                final String containerName = ((Variable) container).getName();
                                                if (containerName.equals(variableName)) {
                                                    final ArrayIndex index = access.getIndex();
                                                    if (index != null && OpenapiTypesUtil.isNumber(index.getValue())) {
                                                        holder.registerProblem(reference.getFirstChild(), message);
                                                        break;
                                                    }
                                                }
                                            }
                                        } else if (OpenapiTypesUtil.isFunctionReference(usage)) {
                                            final PsiElement[] usedArguments = ((FunctionReference) usage).getParameters();
                                            if (usedArguments.length > 0) {
                                                final boolean isTarget = Arrays.stream(usedArguments)
                                                        .filter(a   -> a instanceof Variable)
                                                        .anyMatch(a -> ((Variable) a).getName().equals(variableName));
                                                if (isTarget && this.isInPotentiallyBuggyCall(usedArguments[0].getParent())) {
                                                    holder.registerProblem(reference.getFirstChild(), message);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (this.isInPotentiallyBuggyCall(parent)) {
                            holder.registerProblem(reference.getFirstChild(), message);
                        }
                    }
                }
            }

            private boolean isInPotentiallyBuggyCall(@NotNull PsiElement parent) {
                boolean result = false;
                if (parent instanceof ParameterList) {
                    final PsiElement grandParent = parent.getParent();
                    if (OpenapiTypesUtil.isFunctionReference(grandParent)) {
                        final String parentCallName = ((FunctionReference) grandParent).getName();
                        result = parentCallName != null && parentCallName.equals("json_encode");
                    }
                }
                return result;
            }
        };
    }
}
