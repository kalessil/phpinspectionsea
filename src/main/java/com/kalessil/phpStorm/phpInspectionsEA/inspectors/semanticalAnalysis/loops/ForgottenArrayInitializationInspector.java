package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

public class ForgottenArrayInitializationInspector extends BasePhpInspection {
    private static final String message = "The array initialization is missing, please place it at a proper place.";

    @NotNull
    public String getShortName() {
        return "ForgottenArrayInitializationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpArrayAccessExpression(@NotNull ArrayAccessExpression expression) {
                final ArrayIndex index = expression.getIndex();
                if (index != null && index.getValue() == null) {
                    final Function scope = ExpressionSemanticUtil.getScope(expression);
                    if (scope != null) {
                        /* identify nesting level */
                        int nestingLevel  = 0;
                        PsiElement parent = expression.getParent();
                        while (parent != null && parent != scope) {
                            if (OpenapiTypesUtil.isLoop(parent) && ++nestingLevel >= 2) {
                                break;
                            }
                            parent = parent.getParent();
                        }

                        /* target 2+ nesting levels */
                        if (nestingLevel >= 2) {
                            final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(scope);
                            final PsiElement base     = expression.getValue();
                            if (base != null && body != null) {
                                for (final PsiElement candidate : PsiTreeUtil.findChildrenOfType(body, base.getClass())) {
                                    /* match itself */
                                    if (candidate == base) {
                                        break;
                                    }
                                    /* a value has been written */
                                    final PsiElement context = candidate.getParent();
                                    if (context instanceof AssignmentExpression) {
                                        final AssignmentExpression assignment = (AssignmentExpression) context;
                                        if (assignment.getValue() != base && OpenapiEquivalenceUtil.areEqual(candidate, base)) {
                                            return;
                                        }
                                    }
                                }
                                problemsHolder.registerProblem(expression, message);
                            }
                        }
                    }
                }
            }
        };
    }
}
