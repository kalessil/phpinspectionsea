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
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnqualifiedReferenceInspector extends BasePhpInspection {
    private static final String messagePattern = "Using '\\%t%' would enable some of opcache optimizations";

    private static Set<String> falsePositives = new HashSet<>();
    static {
        falsePositives.add("true");
        falsePositives.add("TRUE");
        falsePositives.add("false");
        falsePositives.add("FALSE");
        falsePositives.add("null");
        falsePositives.add("NULL");

        falsePositives.add("class");
        falsePositives.add("__LINE__");
        falsePositives.add("__FILE__");
        falsePositives.add("__DIR__");
        falsePositives.add("__FUNCTION__");
        falsePositives.add("__CLASS__");
        falsePositives.add("__TRAIT__");
        falsePositives.add("__METHOD__");
        falsePositives.add("__NAMESPACE__");
    }

    @NotNull
    public String getShortName() {
        return "UnqualifiedReferenceInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                analyze(reference);
            }
            public void visitPhpConstantReference(ConstantReference reference) {
                analyze(reference);
            }

            private void analyze(PhpReference reference) {
                /* makes sense only with PHP7+ opcache */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(reference.getProject()).getLanguageLevel();
                if (!phpVersion.isAtLeast(PhpLanguageLevel.PHP700)) {
                    return;
                }

                /* constructs structure expectations */
                final String referenceName = reference.getName();
                if (null == referenceName || !reference.getImmediateNamespaceName().isEmpty()) {
                    return;
                }
                final PhpNamespace ns = PsiTreeUtil.findChildOfType(reference.getContainingFile(), PhpNamespace.class);
                if (null == ns) {
                    return;
                }
                if (reference instanceof ConstantReference && falsePositives.contains(referenceName)) {
                    return;
                }

                /* resolve the constant/function, report if it's from the root NS */
                final PsiElement resolved = reference.resolve();
                final boolean isFunction  = resolved instanceof Function;
                if (isFunction || resolved instanceof Constant) {
                    final String fqn = ((PhpNamedElement) resolved).getFQN();
                    if (fqn.length() != 1 + referenceName.length() || !fqn.equals("\\" + referenceName)) {
                        return;
                    }

                    final String message = messagePattern.replace("%t%", referenceName + (isFunction ? "(...)" : ""));
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
            if (target instanceof FunctionReference || target instanceof ConstantReference) {
                final PsiElement rootNs   = PhpPsiElementFactory.createNamespaceReference(project, "\\", false);
                final PsiElement nameNode = target.getFirstChild();
                nameNode.getParent().addBefore(rootNs, nameNode);
            }
        }
    }
}
