package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
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

public class GetClassUsageInspector extends BasePhpInspection {

    private static final String message = "'get_class(...)' does not accept null in PHP 7.2+ versions.";

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
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("get_class")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1) {
                        final PhpType resolved = OpenapiResolveUtil.resolveType(reference, reference.getProject());
                        if (resolved != null) {
                            final boolean hasNull = resolved.getTypes().stream().anyMatch(t -> Types.getType(t).equals(Types.strNull));
                            if (hasNull) {
                                holder.registerProblem(reference, message);
                            }
                        }
                    }
                }
            }
        };
    }
}
