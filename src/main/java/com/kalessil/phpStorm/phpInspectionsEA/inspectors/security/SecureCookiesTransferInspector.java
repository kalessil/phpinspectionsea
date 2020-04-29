package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SecureCookiesTransferInspector extends LocalInspectionTool {
    private static final String pattern = "It's not recommended to rely on $secure and $httponly defaults (apply QF to see how to harden the call).";

    @NotNull
    @Override
    public String getShortName() {
        return "SecureCookiesTransferInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Secure cookies transfer";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_SECURITY)) { return; }

                final String functionName = reference.getName();
                if (functionName != null) {
                    if (functionName.equals("session_set_cookie_params") && this.isTargetCall(reference, 1)) {
                        this.analyze(reference, 3, 4);
                    } else if (functionName.equals("setcookie") && this.isTargetCall(reference, 3)) {
                        this.analyze(reference, 5, 6);
                    }
                }
            }

            private boolean isTargetCall(@NotNull FunctionReference reference, int optionsArrayPosition) {
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length == optionsArrayPosition && PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP730)) {
                    final PsiElement optionsCandidate = arguments[optionsArrayPosition - 1];
                    if (optionsCandidate instanceof PhpTypedElement) {
                        final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) optionsCandidate, holder.getProject());
                        if (resolved != null && !resolved.hasUnknown()) {
                            return resolved.getTypes().stream().noneMatch(t -> Types.getType(t).equals(Types.strArray));
                        }
                    }
                }

                return true;
            }

            private void analyze(@NotNull FunctionReference reference, int secureFlagPosition, int httpOnlyFlagPosition) {
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length > 0 && arguments.length < httpOnlyFlagPosition) {
                    /* generate fragments */
                    final List<String> fragments = new ArrayList<>(Collections.nCopies(httpOnlyFlagPosition + 1, ""));
                    fragments.set(secureFlagPosition - 3, "ini_get('session.cookie_lifetime')");
                    fragments.set(secureFlagPosition - 2, "ini_get('session.cookie_path')");
                    fragments.set(secureFlagPosition - 1, "ini_get('session.cookie_domain')");
                    fragments.set(secureFlagPosition,     "ini_get('session.cookie_secure')");
                    fragments.set(httpOnlyFlagPosition,   "ini_get('session.cookie_httponly')");
                    for (int index = 0; index < arguments.length; ++index) {
                        fragments.set(index, arguments[index].getText());
                    }
                    holder.registerProblem(
                            reference,
                            MessagesPresentationUtil.prefixWithEa(pattern),
                            new AddArgumentsFix(String.format("%s(%s)", reference.getName(), String.join(", ", fragments)))
                    );
                    fragments.clear();
                }
            }
        };
    }

    private static final class AddArgumentsFix extends UseSuggestedReplacementFixer {
        private static final String title = "Add missing arguments";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        AddArgumentsFix(@NotNull String expression) {
            super(expression);
        }
    }
}
