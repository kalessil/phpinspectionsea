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
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                /* Basic structure validation */
                final PsiElement operator = OpenapiPsiSearchUtil.findResolutionOperator(reference);
                if (!OpenapiTypesUtil.is(operator, PhpTokenTypes.ARROW)) {
                    return;
                }
                final PsiReference psiReference = reference.getReference();
                final String methodName         = reference.getName();
                if (psiReference == null || methodName == null || methodName.startsWith("static")) {
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
                final PsiElement resolved = OpenapiResolveUtil.resolveReference(psiReference);
                if (resolved instanceof Method) {
                    final Method method = (Method) resolved;
                    /* non-static methods and contract interfaces must not be reported */
                    if (!method.isStatic() || method.isAbstract()) {
                        return;
                    }
                    final PhpClass clazz = method.getContainingClass();
                    if (clazz == null || clazz.isInterface()) {
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
                        holder.registerProblem(
                                thisCandidate,
                                messageThisUsed.replace("%m%", methodName),
                                new TheLocalFix(thisCandidate, operator)
                        );
                    }
                    /* Case 2: <expression>-><static method>(); no chained calls; no QF - needs looking into cases */
                    else {
                        holder.registerProblem(reference, messageExpressionUsed.replace("%m%", methodName));
                    }
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
            if (!project.isDisposed()) {
                final PsiElement operator = this.operator.getElement();
                final PsiElement variable = this.variable.getElement();
                if (operator != null && variable != null) {
                    final PsiElement operation = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "::");
                    if (operation != null) {
                        operator.replace(operation);
                        variable.replace(PhpPsiElementFactory.createClassReference(project, "static"));
                    }
                }
            }
        }
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addCheckbox("Follow PHPUnit standards", RESPECT_PHPUNIT_STANDARDS, (isSelected) -> RESPECT_PHPUNIT_STANDARDS = isSelected)
        );
    }
}
