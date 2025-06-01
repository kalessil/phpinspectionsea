package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils.ExpressionCostEstimateUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnsupportedStringOffsetOperationsInspector extends BasePhpInspection {
    private static final String messageOffset = "Could provoke a PHP Fatal error (cannot use string offset as an array).";
    private static final String messagePush   = "Could provoke a PHP Fatal error ([] operator not supported for strings).";

    @NotNull
    @Override
    public String getShortName() {
        return "UnsupportedStringOffsetOperationsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Unsupported string offset operations";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpArrayAccessExpression(@NotNull ArrayAccessExpression expression) {
                final Project project = holder.getProject();
                if (PhpLanguageLevel.get(project).atLeast(PhpLanguageLevel.PHP710)) {
                    PsiElement target          = null;
                    String message             = null;
                    boolean isTargetContext    = false;
                    /* context identification phase */
                    final PsiElement candidate = expression.getValue();
                    if (
                        candidate instanceof Variable ||
                        candidate instanceof FieldReference ||
                        candidate instanceof ArrayAccessExpression
                    ) {
                        /* false-positives: pushing to pre-defined globals */
                        PsiElement possiblyValue = candidate;
                        while (possiblyValue instanceof ArrayAccessExpression) {
                            possiblyValue = ((ArrayAccessExpression) possiblyValue).getValue();
                        }
                        if (possiblyValue instanceof Variable) {
                            final String variableName = ((Variable) possiblyValue).getName();
                            if (ExpressionCostEstimateUtil.predefinedVars.contains(variableName)) {
                                return;
                            }
                        }

                        final PsiElement parent = expression.getParent();
                        /* case 1: unsupported casting to array */
                        if (parent instanceof ArrayAccessExpression) {
                            message = messageOffset;
                            target  = parent;
                            while (target.getParent() instanceof ArrayAccessExpression) {
                                target = target.getParent();
                            }
                            PsiElement context = target.getParent();
                            if (OpenapiTypesUtil.is(context, PhpElementTypes.ARRAY_VALUE)) {
                                context = context.getParent();
                            }
                            if (context instanceof AssignmentExpression) {
                                isTargetContext = ((AssignmentExpression) context).getValue() != target;
                            }
                        }
                        /* case 2: array push operator is not supported by strings */
                        else {
                            final ArrayIndex index = expression.getIndex();
                            if (index == null || index.getValue() == null) {
                                final PsiElement context = expression.getParent();
                                if (context instanceof AssignmentExpression) {
                                    message         = messagePush;
                                    target          = expression;
                                    isTargetContext = ((AssignmentExpression) context).getValue() != expression;
                                }
                            }
                        }
                    }
                    /* type verification and reporting phase */
                    if (isTargetContext && ExpressionSemanticUtil.getScope(target) != null) {
                        final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) candidate, project);
                        if (resolved != null) {
                            final boolean isTarget = resolved.filterUnknown().getTypes().stream().anyMatch(type -> Types.getType(type).equals(Types.strString));
                            if (isTarget) {
                                holder.registerProblem(
                                        target,
                                        MessagesPresentationUtil.prefixWithEa(message),
                                        ProblemHighlightType.GENERIC_ERROR
                                );
                            }
                        }
                    }
                }
            }
        };
    }
}
