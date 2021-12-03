package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class PhpUnitDeprecationsInspector extends BasePhpInspection {
    // Inspection options.
    public PhpUnitVersion PHP_UNIT_VERSION = PhpUnitVersion.PHPUNIT80;

    private final static String messageDeprecated = "%s is deprecated in favor of %s() since PHPUnit %s.";
    private final static String messageRemoved    = "%s is deprecated since PHPUnit 8.";

    @NotNull
    @Override
    public String getShortName() {
        return "PhpUnitDeprecationsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "PHPUnit: API deprecations";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                final PhpUnitVersion version = PHP_UNIT_VERSION == null ? PhpUnitVersion.PHPUNIT80 : PHP_UNIT_VERSION;
                if (version.atLeast(PhpUnitVersion.PHPUNIT80)) {
                    final String methodName = reference.getName();
                    if (methodName != null && (methodName.equals("assertEquals") || methodName.equals("assertNotEquals"))) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length > 3) {
                            if (arguments.length >= 4 && !arguments[3].getText().isEmpty()) {
                                holder.registerProblem(
                                        arguments[3],
                                        MessagesPresentationUtil.prefixWithEa(String.format(messageDeprecated, "$delta", methodName + "WithDelta", PhpUnitVersion.PHPUNIT80)),
                                        ProblemHighlightType.LIKE_DEPRECATED
                                );
                            }
                            if (arguments.length >= 5 && !arguments[4].getText().isEmpty()) {
                                holder.registerProblem(
                                        arguments[4],
                                        MessagesPresentationUtil.prefixWithEa(String.format(messageRemoved, "$maxDepth")),
                                        ProblemHighlightType.LIKE_DEPRECATED
                                );
                            }
                            if (arguments.length >= 6 && !arguments[5].getText().isEmpty()) {
                                holder.registerProblem(
                                        arguments[5],
                                        MessagesPresentationUtil.prefixWithEa(String.format(messageDeprecated, "$canonicalize", methodName + "Canonicalizing", PhpUnitVersion.PHPUNIT80)),
                                        ProblemHighlightType.LIKE_DEPRECATED
                                );
                            }
                            if (arguments.length >= 7 && !arguments[6].getText().isEmpty()) {
                                holder.registerProblem(
                                        arguments[6],
                                        MessagesPresentationUtil.prefixWithEa(String.format(messageDeprecated, "$ignoreCase", methodName + "IgnoringCase", PhpUnitVersion.PHPUNIT80)),
                                        ProblemHighlightType.LIKE_DEPRECATED
                                );
                            }
                        }
                    }
                }
                if (version.atLeast(PhpUnitVersion.PHPUNIT91)) {
                    final String methodName = reference.getName();
                    if (methodName != null && (methodName.equals("assertFileNotExists") || methodName.equals("assertDirectoryNotExists"))) {
                        final PsiElement nameNode = NamedElementUtil.getNameIdentifier(reference);
                        if (nameNode != null) {
                            final String recommendedName = methodName.replace("NotExist", "DoesNotExist");
                            holder.registerProblem(
                                    nameNode,
                                    MessagesPresentationUtil.prefixWithEa(String.format(messageDeprecated, methodName, recommendedName, PhpUnitVersion.PHPUNIT91)),
                                    ProblemHighlightType.LIKE_DEPRECATED,
                                    new UseRecommendedAssertionFix(recommendedName)
                            );
                        }
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addDropDown("PHPUnit version", PHP_UNIT_VERSION == null ? PhpUnitVersion.PHPUNIT80 : PHP_UNIT_VERSION, (version) -> PHP_UNIT_VERSION = (PhpUnitVersion) version)
        );
    }

    private static final class UseRecommendedAssertionFix implements LocalQuickFix {
        private static final String title = "Use recommended assertion instead";

        final private String suggestedAssertion;

        UseRecommendedAssertionFix(@NotNull String suggestedName) {
            super();
            this.suggestedAssertion = suggestedName;
        }

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement().getParent();
            if (expression instanceof FunctionReference && ! project.isDisposed()) {
                ((FunctionReference) expression).handleElementRename(this.suggestedAssertion);
            }
        }
    }
}
