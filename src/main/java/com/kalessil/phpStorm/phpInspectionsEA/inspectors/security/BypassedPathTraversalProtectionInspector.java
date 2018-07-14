package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class BypassedPathTraversalProtectionInspector extends LocalInspectionTool {

    @NotNull
    public String getShortName() {
        return "BypassedPathTraversalProtectionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("str_replace")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length >= 3) {
                        final Set<String> second = ExpressionSemanticUtil.resolveAsString(arguments[1]);
                        if (!second.isEmpty() && second.stream().anyMatch(String::isEmpty)) {
                            final Set<String> first = ExpressionSemanticUtil.resolveAsString(arguments[0]);
                            if (!first.isEmpty()) {
                                // ../ or ..\
                            }
                            first.clear();
                        }
                        second.clear();
                    }
                }
            }
        };
    }
}
