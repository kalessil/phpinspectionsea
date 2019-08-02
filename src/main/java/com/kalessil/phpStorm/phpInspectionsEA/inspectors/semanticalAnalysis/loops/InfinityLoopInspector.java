package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
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

public class InfinityLoopInspector extends PhpInspection {
    private static final String message = "Causes infinity loop.";

    final static private Set<String> references = new HashSet<>();
    static {
        references.add("$this");
        references.add("self");
        references.add("static");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "InfinityLoopInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "!display-name!";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.shouldSkipAnalysis(method, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                /* case: buggy getter (field/method names identical => accidental recursion) */
                if (!method.isAbstract()) {
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                    if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) == 1) {
                        /* get the recursion candidate */
                        PsiElement last = ExpressionSemanticUtil.getLastStatement(body);
                        if (last instanceof PhpReturn) {
                            last = ((PhpReturn) last).getArgument();
                        } else if (OpenapiTypesUtil.isStatementImpl(last)) {
                            last = ((Statement) last).getFirstPsiChild();
                        }
                        /* now check for harmful recursion */
                        if (last instanceof MethodReference) {
                            final MethodReference value     = (MethodReference) last;
                            final String returnedMethodName = value.getName();
                            if (returnedMethodName != null && returnedMethodName.equals(method.getName())) {
                                final PhpExpression subject = value.getClassReference();
                                final String reference      = subject == null ? null : subject.getText();
                                if (reference != null && references.contains(reference)) {
                                    holder.registerProblem(value, message);
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
