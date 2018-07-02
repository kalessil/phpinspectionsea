package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.deprecations;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class GetClassUsageInspector extends BasePhpInspection {

    private static final String message = "'get_class(...)' does not accept null as argument in PHP 7.2+ versions.";

    @NotNull
    public String getShortName() {
        return "GetClassUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final Project project      = holder.getProject();
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP710) >= 0) {
                    final String functionName = reference.getName();
                    if (functionName != null && functionName.equals("get_class")) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 1 && arguments[0] instanceof PhpTypedElement) {
                            final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) arguments[0], project);
                            if (resolved != null) {
                                final boolean hasNull = resolved.filterUnknown().getTypes().stream()
                                        .anyMatch(t -> Types.getType(t).equals(Types.strNull));
                                if ((hasNull || isNullableParameter(arguments[0])) && !isNullabilityChecked(arguments[0])) {
                                    holder.registerProblem(reference, message);
                                }
                            }
                        }
                    }
                }
            }

            private boolean isNullabilityChecked(@NotNull PsiElement expression) {
                /* workaround for https://youtrack.jetbrains.com/issue/WI-38622 */
                boolean result      = false;
                final Function scope = ExpressionSemanticUtil.getScope(expression);
                if (scope != null) {
                    final GroupStatement body        = ExpressionSemanticUtil.getGroupStatement(scope);
                    final List<PsiElement> allUsages = PsiTreeUtil.findChildrenOfType(body, expression.getClass()).stream()
                            .filter(e -> OpenapiEquivalenceUtil.areEqual(e, expression))
                            .collect(Collectors.toList());
                    for (final PsiElement candidate : allUsages.subList(0, allUsages.indexOf(expression))) {
                        final PsiElement parent = candidate.getParent();
                        if (parent instanceof PhpEmpty || parent instanceof PhpIsset) {
                            result = true;
                        } else if (parent instanceof BinaryExpression) {
                            final BinaryExpression binary = (BinaryExpression) parent;
                            final IElementType operator   = binary.getOperationType();
                            if (operator == PhpTokenTypes.kwINSTANCEOF) {
                                result = true;
                            } else if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operator)) {
                                final PsiElement second = OpenapiElementsUtil.getSecondOperand(binary, candidate);
                                result                  = PhpLanguageUtil.isNull(second);
                            }
                        } else if (ExpressionSemanticUtil.isUsedAsLogicalOperand(candidate)) {
                            result = true;
                        }
                        /* break loop when null check being found */
                        if (result) {
                            break;
                        }
                    }
                    allUsages.clear();
                }
                return result;
            }

            private boolean isNullableParameter(@NotNull PsiElement expression) {
                boolean result = false;
                if (expression instanceof Variable) {
                    final Function scope = ExpressionSemanticUtil.getScope(expression);
                    if (scope != null) {
                        final String name = ((Variable) expression).getName();
                        result            = Arrays.stream(scope.getParameters()).anyMatch(parameter ->
                                name.equals(parameter.getName()) && PhpLanguageUtil.isNull(parameter.getDefaultValue())
                        );
                    }
                }
                return result;
            }
        };
    }
}
