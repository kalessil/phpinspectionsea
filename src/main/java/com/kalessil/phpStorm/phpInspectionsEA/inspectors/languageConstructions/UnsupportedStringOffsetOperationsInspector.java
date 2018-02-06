package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
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
    private static final String message = "Provokes a PHP Fatal error (cannot use string offset as an array).";

    @NotNull
    public String getShortName() {
        return "UnsupportedStringOffsetOperationsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpArrayAccessExpression(@NotNull ArrayAccessExpression expression) {
                final Project project      = holder.getProject();
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP710) >= 0) {
                    final PsiElement candidate = expression.getValue();
                    if (candidate instanceof Variable || candidate instanceof FieldReference) {
                        final PsiElement parent = expression.getParent();
                        if (parent instanceof ArrayAccessExpression) {
                            /* identify context */
                            PsiElement target = parent;
                            while (target.getParent() instanceof ArrayAccessExpression) {
                                target = target.getParent();
                            }
                            PsiElement context      = target.getParent();
                            boolean isTargetContext = false;
                            if (context instanceof AssignmentExpression) {
                                isTargetContext = ((AssignmentExpression) context).getValue() != target;
                            } else if (OpenapiTypesUtil.is(context, PhpElementTypes.ARRAY_VALUE)) {
                                final PsiElement array = context.getParent();
                                if ((context = array.getParent()) instanceof AssignmentExpression) {
                                    isTargetContext = ((AssignmentExpression) context).getValue() != array;
                                }
                            }
                            /* check types if context identified as target one */
                            if (isTargetContext) {
                                final PhpType type = OpenapiResolveUtil.resolveType((PhpTypedElement) candidate, project);
                                if (type != null) {
                                    final boolean isTarget = type.filterUnknown().getTypes().stream()
                                            .anyMatch(t -> Types.getType(t).equals(Types.strString));
                                    if (isTarget) {
                                        holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR);
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
