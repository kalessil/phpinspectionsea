package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
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

public class KeysFragmentationWithArrayFilterInspector extends BasePhpInspection {
    private static final String message = "Result keys set might be fragmented, wrapping with 'array_values(...)' is recommended.";

    @NotNull
    public String getShortName() {
        return "KeysFragmentationWithArrayFilterInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("array_filter")) {
                    final PsiElement[] arguments = reference.getParameters();
                    final PsiElement parent      = reference.getParent();
                    if (arguments.length > 0 && OpenapiTypesUtil.isAssignment(parent)) {
                        final PsiElement candidate = ((AssignmentExpression) parent).getVariable();
                        if (candidate instanceof Variable) {
                            final Function scope      = ExpressionSemanticUtil.getScope(reference);
                            final GroupStatement body = scope == null ? null : ExpressionSemanticUtil.getGroupStatement(scope);
                            if (body != null) {
                                final String variableName = ((Variable) candidate).getName();
                                for (final ArrayAccessExpression access : PsiTreeUtil.findChildrenOfType(body, ArrayAccessExpression.class)) {
                                    final PsiElement container = access.getValue();
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
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
