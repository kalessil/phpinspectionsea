package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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

public class ImplicitMagicMethodCallInspector extends BasePhpInspection {
    // Inspection options.
    public boolean SUGGEST_USING_STRING_CASTING = false;

    private static final String message              = "Implicit magic method calls should be avoided as these methods are used by PHP internals.";
    private static final String patternStringCasting = "Please use '(string) %o%' instead.";

    private static final Set<String> methods = new HashSet<>();
    static {
        methods.add("__construct");
        methods.add("__destruct");
        methods.add("__call");
        methods.add("__callStatic");
        methods.add("__get");
        methods.add("__set");
        methods.add("__isset");
        methods.add("__unset");
        methods.add("__sleep");
        methods.add("__wakeup");
        methods.add("__toString");
        methods.add("__invoke");
        methods.add("__set_state");
        methods.add("__clone");
        // methods.add("__debugInfo");
    }

    @NotNull
    public String getShortName() {
        return "ImplicitMagicMethodCallInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                final String methodName = reference.getName();
                if (methodName != null && methods.contains(methodName)) {
                    /* Pattern 1: direct calls ob objects */
                    final String referenceObject = reference.getFirstChild().getText().trim();
                    if (!referenceObject.equals("$this") && !this.isTestContext(reference)) {
                        if (!(reference.getFirstChild() instanceof ClassReference) && !referenceObject.equals("parent")) {
                            /* __toString is a special case */
                            if (SUGGEST_USING_STRING_CASTING && methodName.equals("__toString")) {
                                final String message = patternStringCasting.replace("%o%", referenceObject);
                                holder.registerProblem(reference, message, new UseStringCastingLocalFix());

                                return;
                            }
                            /* allow calling __toString, as a developer don't want hints on this */
                            if (!SUGGEST_USING_STRING_CASTING && methodName.equals("__toString")) {
                                return;
                            }

                            /* generally reported cases */
                            holder.registerProblem(reference, message);
                            return;
                        }

                        /* Pattern 2: internal calls inside class methods */
                        final Function method = ExpressionSemanticUtil.getScope(reference);
                        if (null != method && !method.getName().equals(methodName)) {
                            /* allow __construct inside unserialize */
                            if (methodName.equals("__construct") && method.getName().equals("unserialize")) {
                                return;
                            }
                            /* allow calling __toString, as a developer don't want hints on this */
                            if (!SUGGEST_USING_STRING_CASTING && methodName.equals("__toString")) {
                                return;
                            }

                            holder.registerProblem(reference, message);
                        }
                    }
                }
            }
        };
    }

    private static final class UseStringCastingLocalFix implements LocalQuickFix {
        private static final String title = "Use string casting";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof MethodReference) {
                final PsiElement replacement = PhpPsiElementFactory.createFromText(project, UnaryExpression.class, "(string) null");
                ((UnaryExpression) replacement).getValue().replace(expression.getFirstChild().copy());

                expression.replace(replacement);
            }
        }
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component)
            -> component.addCheckbox("Suggest using (string)...", SUGGEST_USING_STRING_CASTING, (isSelected) -> SUGGEST_USING_STRING_CASTING = isSelected));
    }
}
