package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class UnusedGotoLabelInspector extends BasePhpInspection {
    private static final String message = "The label is not used";

    @NotNull
    public String getShortName() {
        return "UnusedGotoLabelInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                visitPhpFunction(method);
            }

            public void visitPhpFunction(Function function) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(function);
                if (null == body || 0 == ExpressionSemanticUtil.countExpressionsInGroup(body)) {
                    return;
                }

                /* extract labels */
                final Collection<PhpGotoLabel> labels = PsiTreeUtil.findChildrenOfType(body, PhpGotoLabel.class);
                if (0 == labels.size()) {
                    return;
                }
                final Map<String, PhpGotoLabel> existingLabels = new HashMap<>();
                for (PhpGotoLabel labelExpression : labels) {
                    final String label = labelExpression.getName();
                    if (label.length() > 0) {
                        existingLabels.put(label, labelExpression);
                    }
                }
                labels.clear();

                /* process goto statements and drop used from existing */
                final Collection<PhpGoto> refs = PsiTreeUtil.findChildrenOfType(body, PhpGoto.class);
                if (refs.size() > 0) {
                    for (PhpGoto gotoExpression : refs) {
                        final String label = gotoExpression.getName();
                        if (null != label && label.length() > 0) {
                            existingLabels.remove(label);
                        }
                    }
                    refs.clear();
                }

                /* report unused labels */
                if (existingLabels.size() > 0) {
                    for (PhpGotoLabel label : existingLabels.values()) {
                        holder.registerProblem(label, message, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                    }
                    existingLabels.clear();
                }
            }
        };
    }
}
