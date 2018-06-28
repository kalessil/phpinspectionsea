package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class SimpleXmlLoadFileUsageInspector extends BasePhpInspection {
    private static final String message = "This can be affected by a PHP bug #62577 (https://bugs.php.net/bug.php?id=62577)";

    @NotNull
    public String getShortName() {
        return "SimpleXmlLoadFileUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("simplexml_load_file")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > 0) {
                        final List<String> fragments = Arrays.stream(arguments)
                                .map(PsiElement::getText)
                                .collect(Collectors.toList());
                        final String file         = fragments.remove(0);
                        final String xmlArguments = fragments.isEmpty() ? "" : ", " + String.join(", ", fragments);
                        final String replacement  = String.format("simplexml_load_string(file_get_contents(%s)%s)", file, xmlArguments);
                        holder.registerProblem(reference, message, new LoadStringFix(replacement));
                    }
                }
            }
        };
    }

    private static final class LoadStringFix extends UseSuggestedReplacementFixer {
        private static final String title = "Replace with a similar call";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        LoadStringFix(@NotNull String expression) {
            super(expression);
        }
    }
}
