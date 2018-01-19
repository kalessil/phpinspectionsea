package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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
                                if (hasNull || isNullableParameter(arguments[0])) {
                                    holder.registerProblem(reference, message);
                                }
                            }
                        }
                    }
                }
            }

            private boolean isNullableParameter(@NotNull PsiElement expression) {
                boolean result = false;
                if (expression instanceof Variable) {
                    final Function scope = ExpressionSemanticUtil.getScope(expression);
                    if (scope != null) {
                        final String name = ((Variable) expression).getName();
                        result = Arrays.stream(scope.getParameters()).anyMatch(parameter ->
                                name.equals(parameter.getName()) && PhpLanguageUtil.isNull(parameter.getDefaultValue())
                        );
                    }
                }
                return result;
            }
        };
    }
}
