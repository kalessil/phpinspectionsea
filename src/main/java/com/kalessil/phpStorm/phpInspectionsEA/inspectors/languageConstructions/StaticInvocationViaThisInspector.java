package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
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
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiPsiSearchUtil;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class StaticInvocationViaThisInspector extends BasePhpInspection {
    // configuration flags automatically saved by IDE
    @SuppressWarnings("WeakerAccess")
    public boolean RESPECT_PHPUNIT_STANDARDS = true;

    private static final String messageThisUsed       = "'static::%m%(...)' should be used instead";
    private static final String messageExpressionUsed = "'...::%m%(...)' should be used instead";

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
                if (null == operator || PhpTokenTypes.ARROW != operator.getNode().getElementType()) {
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
                    if (RESPECT_PHPUNIT_STANDARDS && clazz.getFQN().startsWith("\\PHPUnit_Framework_")) {
                        return;
                    }

                    /* Case 1: $this-><static method>() */
                    if (contextOfThis) {
                        final String message = messageThisUsed.replace("%m%", methodName);
                        holder.registerProblem(thisCandidate, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix(thisCandidate, operator));

                        return;
                    }

                    /* Case 2: <expression>-><static method>(); no chained calls; no QF - needs looking into cases */
                    final String message = messageExpressionUsed.replace("%m%", reference.getName());
                    holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        private SmartPsiElementPointer<PsiElement> variable;
        private SmartPsiElementPointer<PsiElement> operator;

        TheLocalFix(@NotNull PsiElement variable, @NotNull PsiElement operator) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(variable.getProject());

            this.variable = factory.createSmartPsiElementPointer(variable, variable.getContainingFile());
            this.operator = factory.createSmartPsiElementPointer(operator, operator.getContainingFile());
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
        return (new StaticInvocationViaThisInspector.OptionsPanel()).getComponent();
    }

    private class OptionsPanel {
        final private JPanel optionsPanel;

        final private JCheckBox respectPhpunitStandards;

        public OptionsPanel() {
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new MigLayout());

            respectPhpunitStandards = new JCheckBox("Follow PHPUnit standards", RESPECT_PHPUNIT_STANDARDS);
            respectPhpunitStandards.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    RESPECT_PHPUNIT_STANDARDS = respectPhpunitStandards.isSelected();
                }
            });
            optionsPanel.add(respectPhpunitStandards, "wrap");
        }

        public JPanel getComponent() {
            return optionsPanel;
        }
    }
}