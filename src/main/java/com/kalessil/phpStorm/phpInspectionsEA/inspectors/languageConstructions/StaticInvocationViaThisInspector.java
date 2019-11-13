package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.stream.Stream;

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

    private static final String messageThisUsed       = "'self::%s(...)' should be used instead.";
    private static final String messageExpressionUsed = "'...::%s(...)' should be used instead.";

    @NotNull
    @Override
    public String getShortName() {
        return "StaticInvocationViaThisInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Static methods invocation via '->'";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                final String methodName = reference.getName();
                if (methodName != null && ! reference.isStatic() && ! methodName.startsWith("static") /* workaround for WI-33569 */) {
                    final PsiElement base = reference.getFirstChild();
                    if (base != null && ! (base instanceof FunctionReference)) {
                        final PsiElement operator = OpenapiPsiSearchUtil.findResolutionOperator(reference);
                        if (OpenapiTypesUtil.is(operator, PhpTokenTypes.ARROW)) {
                            if (base instanceof Variable && ((Variable) base).getName().equals("this")) {
                                /* $this->static() */
                                final Function scope = ExpressionSemanticUtil.getScope(reference);
                                if (scope instanceof Method && ! ((Method) scope).isStatic()) {
                                    final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                                    if (resolved instanceof Method) {
                                        final Method method = (Method) resolved;
                                        if (method.isStatic()) {
                                            this.handleLateStaticBinding(base, operator, method);
                                        }
                                    }
                                }
                            } else {
                                /* <expression>->static() */
                                if (! (base instanceof Variable) || ! this.isParameterOrUseVariable((Variable) base)) {
                                    final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                                    if (resolved instanceof Method && ((Method) resolved).isStatic()) {
                                        holder.registerProblem(
                                                reference,
                                                String.format(ReportingUtil.wrapReportedMessage(messageExpressionUsed), methodName)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private boolean isParameterOrUseVariable(@NotNull Variable variable) {
                boolean result    = false;
                final String name = variable.getName();
                if (! name.isEmpty()) {
                    final Function scope = ExpressionSemanticUtil.getScope(variable);
                    if (scope != null) {
                        result = Stream.of(scope.getParameters()).anyMatch(p -> name.equals(p.getName()));
                        if (! result) {
                            final List<Variable> variables = ExpressionSemanticUtil.getUseListVariables(scope);
                            if (variables != null && ! variables.isEmpty()) {
                                result = variables.stream().anyMatch(v -> name.equals(v.getName()));
                                variables.clear();
                            }
                        }
                    }
                }
                return result;
            }

            private void handleLateStaticBinding(@NotNull PsiElement base, @NotNull PsiElement operator, @NotNull Method method) {
                if (RESPECT_PHPUNIT_STANDARDS) {
                    final String fqn = method.getFQN();
                    if (fqn.startsWith("\\PHPUnit")) {
                        final String normalized = fqn.indexOf('_') == -1 ? fqn : fqn.replaceAll("_", "\\\\");
                        if (normalized.startsWith("\\PHPUnit\\Framework\\")) {
                            return;
                        }
                    }
                }
                holder.registerProblem(
                        base,
                        String.format(ReportingUtil.wrapReportedMessage(messageThisUsed), method.getName()),
                        new TheLocalFix(holder.getProject(), base, operator)
                );
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use self::";

        private final SmartPsiElementPointer<PsiElement> variable;
        private final SmartPsiElementPointer<PsiElement> operator;

        TheLocalFix(@NotNull Project project, @NotNull PsiElement variable, @NotNull PsiElement operator) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(project);

            this.variable = factory.createSmartPsiElementPointer(variable);
            this.operator = factory.createSmartPsiElementPointer(operator);
        }

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
            final PsiElement operator = this.operator.getElement();
            final PsiElement variable = this.variable.getElement();
            if (operator != null && variable != null && !project.isDisposed()) {
                final PsiElement operation = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "::");
                if (operation != null) {
                    operator.replace(operation);
                    variable.replace(PhpPsiElementFactory.createClassReference(project, "self"));
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
