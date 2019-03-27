package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class SlowArrayOperationsInLoopInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s(...)' is used in a loop and is a resources greedy construction.";

    @NotNull
    public String getShortName() {
        return "SlowArrayOperationsInLoopInspection";
    }

    private static final Set<String> functionsSet = new HashSet<>();
    static {
        functionsSet.add("array_merge");
        functionsSet.add("array_merge_recursive");
        functionsSet.add("array_replace");
        functionsSet.add("array_replace_recursive");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(reference))              { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionsSet.contains(functionName)) {
                    PsiElement parent = reference.getParent();
                    if (parent instanceof AssignmentExpression) {
                        final AssignmentExpression assignment = (AssignmentExpression) parent;
                        /* false-positives: return/break as last group statement expression */
                        boolean canLoop = true;
                        if (OpenapiTypesUtil.isStatementImpl(parent = parent.getParent())) {
                            final PsiElement groupCandidate = parent.getParent();
                            if (groupCandidate instanceof GroupStatement) {
                                final PsiElement last = ExpressionSemanticUtil.getLastStatement((GroupStatement) groupCandidate);
                                canLoop               = !(last instanceof PhpBreak) && !(last instanceof PhpReturn);
                            }
                        }
                        while (canLoop && parent != null && !(parent instanceof PhpFile) && !(parent instanceof Function)) {
                            if (OpenapiTypesUtil.isLoop(parent)) {
                                final PsiElement container = assignment.getVariable();
                                if (container != null) {
                                    final boolean modifiesSameContainer = Stream.of(reference.getParameters())
                                            .anyMatch(a -> OpenapiEquivalenceUtil.areEqual(container, a));
                                    if (modifiesSameContainer) {
                                        holder.registerProblem(reference, String.format(messagePattern, functionName));
                                        return;
                                    }
                                }
                            }
                            parent = parent.getParent();
                        }
                    }
                }
            }
        };
    }
}