package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SlowArrayOperationsInLoopInspector extends PhpInspection {
    private static final String messagePattern = "'%s(...)' is used in a loop and is a resources greedy construction.";

    @NotNull
    @Override
    public String getShortName() {
        return "SlowArrayOperationsInLoopInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "!display-name!";
    }

    private static final Set<String> functionsSet = new HashSet<>();
    static {
        functionsSet.add("array_merge");
        functionsSet.add("array_merge_recursive");
        functionsSet.add("array_replace");
        functionsSet.add("array_replace_recursive");
        functionsSet.add("count");
        functionsSet.add("array_unique");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionsSet.contains(functionName)) {
                    final PsiElement parent  = reference.getParent();
                    final PsiElement context = parent instanceof ParameterList ? parent.getParent() : parent;
                    if (this.isTargetContext(context)) {
                        PsiElement current = context.getParent();
                        while (current != null && !(current instanceof PhpFile) && !(current instanceof Function)) {
                            if (OpenapiTypesUtil.isLoop(current)) {
                                if (context instanceof AssignmentExpression) {
                                    if (this.isTargetAssignment((AssignmentExpression) context, reference) && this.isFromRootNamespace(reference)) {
                                        holder.registerProblem(reference, String.format(messagePattern, functionName));
                                    }
                                    return;
                                } else if (context instanceof MethodReference) {
                                    if (this.isTargetReference((MethodReference) context, reference) && this.isFromRootNamespace(reference)) {
                                        holder.registerProblem(reference, String.format(messagePattern, functionName));
                                    }
                                    return;
                                }
                            }
                            current = current.getParent();
                        }
                    }
                }
            }

            private boolean isTargetAssignment(@NotNull AssignmentExpression context, @NotNull FunctionReference reference) {
                final PsiElement container = context.getVariable();
                if (container != null) {
                    return Stream.of(reference.getParameters()).anyMatch(argument -> OpenapiEquivalenceUtil.areEqual(container, argument));
                }
                return false;
            }

            private boolean isTargetReference(@NotNull MethodReference context, @NotNull FunctionReference reference) {
                final String outerMethodName = context.getName();
                if (outerMethodName != null && outerMethodName.startsWith("set")) {
                    final PsiElement[] arguments = context.getParameters();
                    if (arguments.length == 1) {
                        return Stream.of(reference.getParameters()).anyMatch(argument -> {
                            if (argument instanceof MethodReference) {
                                final String innerMethodName = ((MethodReference) argument).getName();
                                if (innerMethodName != null && innerMethodName.startsWith("get")) {
                                    return OpenapiEquivalenceUtil.areEqual(argument.getFirstChild(), context.getFirstChild());
                                }
                            }
                            return false;
                        });
                    }
                }
                return false;
            }

            private boolean isTargetContext(@NotNull PsiElement context) {
                if (context instanceof AssignmentExpression || context instanceof MethodReference) {
                    final PsiElement statementCandidate = context.getParent();
                    if (OpenapiTypesUtil.isStatementImpl(statementCandidate)) {
                        final PsiElement groupCandidate = statementCandidate.getParent();
                        if (groupCandidate instanceof GroupStatement) {
                            final PsiElement last = ExpressionSemanticUtil.getLastStatement((GroupStatement) groupCandidate);
                            return !(last instanceof PhpBreak) && !(last instanceof PhpReturn);
                        }
                    }
                }
                return false;
            }
        };
    }
}