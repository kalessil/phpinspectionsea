package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnqualifiedFunctionReferenceInspector extends BasePhpInspection {
    private static final String messagePattern = "Using '\\%f%(...)' would enable some of opcache optimizations";

    @NotNull
    public String getShortName() {
        return "UnqualifiedFunctionReferenceInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* makes sense only with PHP7+ opcache */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(reference.getProject()).getLanguageLevel();
                if (phpVersion.compareTo(PhpLanguageLevel.PHP700) < 0) {
                    return;
                }

                /* constructs structure expectations */
                final String functionName = reference.getName();
                if (null == functionName || !reference.getImmediateNamespaceName().isEmpty()) {
                    return;
                }
                final PhpNamespace ns = PsiTreeUtil.findChildOfType(reference.getContainingFile(), PhpNamespace.class);
                if (null == ns) {
                    return;
                }

                /* resolve the reference, report if it's from the root NS */
                final PsiElement function = reference.resolve();
                if (function instanceof Function) {
                    final String fqn = ((Function) function).getFQN();
                    if (fqn.length() != 1 + functionName.length() || !fqn.equals("\\" + functionName)) {
                        return;
                    }

                    final String message = messagePattern.replace("%f%", functionName);
                    holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Use the qualified reference";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target instanceof FunctionReference) {
                final PsiElement ns       = PhpPsiElementFactory.createNamespaceReference(project, "\\", false);
                final PsiElement nameNode = target.getFirstChild();
                nameNode.getParent().addBefore(ns, nameNode);
            }
        }
    }
}
