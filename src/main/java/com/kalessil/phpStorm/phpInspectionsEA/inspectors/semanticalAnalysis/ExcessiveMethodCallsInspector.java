package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.Statement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ExcessiveMethodCallsInspector extends BasePhpInspection {
    private static final String messageSequential = "Same as in the previous call, consider introducing a local variable instead.";

    @NotNull
    public String getShortName() {
        return "ExcessiveMethodCallsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                /* TODO: no UTs */
                if (reference.getFirstChild() instanceof MethodReference) {
                    final PsiElement parent = reference.getParent();
                    if (OpenapiTypesUtil.isStatementImpl(parent)) {
                        final PsiElement previous = ((Statement) parent).getPrevPsiSibling();
                        if (previous == null && parent.getParent() instanceof GroupStatement) {
                            /* TODO: loops */
                        } else if (OpenapiTypesUtil.isStatementImpl(previous)) {
                            /* case: sequential calls */
                            final PsiElement candidate = previous.getFirstChild();
                            if (candidate instanceof MethodReference && candidate.getFirstChild() instanceof MethodReference) {
                                final MethodReference previousBase = (MethodReference) candidate.getFirstChild();
                                final MethodReference currentBase  = (MethodReference) reference.getFirstChild();
                                if (OpeanapiEquivalenceUtil.areEqual(currentBase, previousBase)) {
                                    holder.registerProblem(currentBase, messageSequential);
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
