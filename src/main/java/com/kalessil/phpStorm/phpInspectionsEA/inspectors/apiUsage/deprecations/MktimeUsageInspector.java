package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.deprecations;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class MktimeUsageInspector extends BasePhpInspection {
    private static final String messageUseTime             = "You should use time() function instead (current usage produces a runtime warning).";
    private static final String messageParameterDeprecated = "Parameter 'is_dst' is deprecated and removed in PHP 7.";

    @NotNull
    @Override
    public String getShortName() {
        return "MktimeUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'gmmktime(...)'/'mktime(...)' usage";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && (functionName.equals("mktime") || functionName.equals("gmmktime"))) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 0) {
                        if (this.isFromRootNamespace(reference)) {
                            holder.registerProblem(
                                    reference,
                                    MessagesPresentationUtil.prefixWithEa(messageUseTime),
                                    new UseTimeFunctionLocalFix()
                            );
                        }
                    } else if (arguments.length == 7 && arguments[6] instanceof PhpTypedElement) {
                        if (this.isFromRootNamespace(reference)) {
                            holder.registerProblem(
                                    arguments[6],
                                    MessagesPresentationUtil.prefixWithEa(messageParameterDeprecated),
                                    ProblemHighlightType.LIKE_DEPRECATED
                            );
                        }
                    }
                }
            }
        };
    }

    private static final class UseTimeFunctionLocalFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use time() instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseTimeFunctionLocalFix() {
            super("time()");
        }
    }
}