package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class SlowArrayOperationsInLoopInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s%(...)' is used in a loop and is a resources greedy construction.";

    @NotNull
    public String getShortName() {
        return "SlowArrayOperationsInLoopInspection";
    }

    private static final HashSet<String> functionsSet = new HashSet<>();
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
                final String functionName = reference.getName();
                if (functionName == null || !functionsSet.contains(functionName)) {
                    return;
                }
                PsiElement parent = reference.getParent();
                if (!(parent instanceof AssignmentExpression)) {
                    /* let's focus on assignment expressions */
                    return;
                }

                while (parent != null && !(parent instanceof PhpFile)) {
                    /* terminate if reached callable */
                    if (parent instanceof Function) {
                        return;
                    }

                    if (OpenapiTypesUtil.isLoop(parent)) {
                        /* loop test is positive, check pattern */
                        final PhpPsiElement objContainer = ((AssignmentExpression) reference.getParent()).getVariable();
                        if (null == objContainer) {
                            return;
                        }

                        /* pattern itself: container overridden */
                        for (final PsiElement objParameter : reference.getParameters()) {
                            if (OpeanapiEquivalenceUtil.areEqual(objContainer, objParameter)) {
                                final String message = messagePattern.replace("%s%", functionName);
                                holder.registerProblem(reference, message);

                                return;
                            }
                        }
                    }

                    parent = parent.getParent();
                }
            }
        };
    }
}