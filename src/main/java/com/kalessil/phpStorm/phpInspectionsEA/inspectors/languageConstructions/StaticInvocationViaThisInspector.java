package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiPsiSearchUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class StaticInvocationViaThisInspector extends BasePhpInspection {
    // Inspection options.
    public boolean RESPECT_PHPUNIT_STANDARDS = true;

    private static final String messageThisUsed       = "'static::%m%(...)' should be used instead.";
    private static final String messageExpressionUsed = "'...::%m%(...)' should be used instead.";

    @NotNull
    public String getShortName() {
        return "StaticInvocationViaThisInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethodReference(MethodReference reference) {
                /* Basic structure validation */
                final PsiElement operator = OpenapiPsiSearchUtil.findResolutionOperator(reference);
                if (!OpenapiTypesUtil.is(operator, PhpTokenTypes.ARROW)) {
                    return;
                }
                final PsiReference psiReference = reference.getReference();
                final String methodName         = reference.getName();
                if (null == psiReference || null == methodName) {
                    return;
                }

                /* check contexts: $this->, <expression>-> ; placed here due to performance optimization */
                final PsiElement thisCandidate    = reference.getFirstChild();
                final boolean contextOfThis       = thisCandidate.getText().equals("$this");
                final PsiElement objectExpression = contextOfThis ? null : reference.getFirstPsiChild();
                final boolean contextOfExpression = null != objectExpression && !(objectExpression instanceof FunctionReference);
                if (!contextOfThis && !contextOfExpression) {
                    return;
                }

                /* now analyze: contexts are valid  */
                final PsiElement resolved = psiReference.resolve();
                if (resolved instanceof Method) {
                    final Method method  = (Method) resolved;
                    final PhpClass clazz = method.getContainingClass();
                    /* non-static methods and contract interfaces must not be reported */
                    if (null == clazz || clazz.isInterface() || !method.isStatic() || method.isAbstract()) {
                        return;
                    }

                    /* PHP Unit's official docs saying to use $this, follow the guidance */
                    if (RESPECT_PHPUNIT_STANDARDS) {
                        final String classFqn = clazz.getFQN();
                        if (classFqn.startsWith("\\PHPUnit_Framework_") || classFqn.startsWith("\\PHPUnit\\Framework\\")) {
                            return;
                        }
                    }

                    /* Case 1: $this-><static method>() */
                    if (contextOfThis) {
                        final String message = messageThisUsed.replace("%m%", methodName);
                        holder.registerProblem(thisCandidate, message, new TheLocalFix(thisCandidate, operator));

                        return;
                    }

                    /* Case 2: <expression>-><static method>(); no chained calls; no QF - needs looking into cases */
                    final String message = messageExpressionUsed.replace("%m%", reference.getName());
                    holder.registerProblem(reference, message);
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        private final SmartPsiElementPointer<PsiElement> variable;
        private final SmartPsiElementPointer<PsiElement> operator;

        TheLocalFix(@NotNull PsiElement variable, @NotNull PsiElement operator) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(variable.getProject());

            this.variable = factory.createSmartPsiElementPointer(variable);
            this.operator = factory.createSmartPsiElementPointer(operator);
        }

        @NotNull
        @Override
        public String getName() {
            return "Use static::";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement().getParent();
            if (expression instanceof FunctionReference) {
                final PsiElement operator = this.operator.getElement();
                final PsiElement variable = this.variable.getElement();
                if (null == operator || null == variable) {
                    return;
                }

                //noinspection ConstantConditions I' sure NPE will not happen as pattern hardcoded
                operator.replace(PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "::"));
                variable.replace(PhpPsiElementFactory.createClassReference(project, "static"));
            }
        }
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Follow PHPUnit standards", RESPECT_PHPUNIT_STANDARDS, (isSelected) -> RESPECT_PHPUNIT_STANDARDS = isSelected);
        });
    }
}
