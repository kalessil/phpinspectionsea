package com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.UnaryExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class ImplicitMagicMethodCallInspector extends BasePhpInspection {
    private static final String strProblemDescription      = "Implicit magic method calls shall be avoided as these methods are used by PHP internals.";
    private static final String strProblemUseStringCasting = "Please use (string) %o% instead";

    private static HashSet<String> methods;
    static {
            methods = new HashSet<String>(); // TODO: inline instantiation

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
            methods.add("__debugInfo");
    }

    @NotNull
    public String getShortName() {
        return "ImplicitMagicMethodCallInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethodReference(MethodReference reference) {
                final String methodName = reference.getName();
                if (!StringUtil.isEmpty(methodName) && methods.contains(methodName)) {
                    /* Pattern 1: direct calls ob objects */
                    final String strReferenceObject = reference.getFirstChild().getText().trim();
                    if (!strReferenceObject.equals("$this") && !strReferenceObject.equals("parent")) {
                        /* __toString is a special case */
                        if (methodName.equals("__toString")) {
                            final String message = strProblemUseStringCasting.replace("%o%", strReferenceObject);
                            holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new UseStringCastingLocalFix());

                            return;
                        }

                        /* generally reported cases */
                        holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        return;
                    }

                    /* Pattern 2: internal calls inside class methods */
                    final Function method = ExpressionSemanticUtil.getScope(reference);
                    if (null != method && !method.getName().equals(methodName)) {
                        holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
        };
    }

    private static class UseStringCastingLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Use string casting";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof MethodReferenceImpl) {
                final PsiElement replacement = PhpPsiElementFactory.createFromText(project, UnaryExpressionImpl.class, "(string) null");
                //noinspection ConstantConditions - expression is hardcoded so we safe from NPE here
                ((UnaryExpressionImpl) replacement).getValue().replace(expression.getFirstChild().copy());

                expression.replace(replacement);
            }
        }
    }
}
