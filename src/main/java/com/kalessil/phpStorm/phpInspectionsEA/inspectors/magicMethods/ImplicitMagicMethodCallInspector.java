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
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.UnaryExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.HashSet;

public class ImplicitMagicMethodCallInspector extends BasePhpInspection {
    // configuration flags automatically saved by IDE
    @SuppressWarnings("WeakerAccess")
    public boolean SUGGEST_USING_STRING_CASTING = false;

    private static final String strProblemDescription      = "Implicit magic method calls shall be avoided as these methods are used by PHP internals.";
    private static final String strProblemUseStringCasting = "Please use (string) %o% instead";

    private static final HashSet<String> methods = new HashSet<>();
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
            public void visitPhpMethodReference(MethodReference reference) {
                final String methodName = reference.getName();
                if (!StringUtil.isEmpty(methodName) && methods.contains(methodName)) {
                    /* Pattern 1: direct calls ob objects */
                    final String strReferenceObject = reference.getFirstChild().getText().trim();
                    if (
                        !(reference.getFirstChild() instanceof ClassReferenceImpl) &&
                        !strReferenceObject.equals("$this") && !strReferenceObject.equals("parent")
                    ) {
                        /* __toString is a special case */
                        if (SUGGEST_USING_STRING_CASTING && methodName.equals("__toString")) {
                            final String message = strProblemUseStringCasting.replace("%o%", strReferenceObject);
                            holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new UseStringCastingLocalFix());

                            return;
                        }
                        /* allow calling __toString, as a developer don't want hints on this */
                        if (!SUGGEST_USING_STRING_CASTING && methodName.equals("__toString")) {
                            return;
                        }

                        /* generally reported cases */
                        holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
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

    public JComponent createOptionsPanel() {
        return (new ImplicitMagicMethodCallInspector.OptionsPanel()).getComponent();
    }

    private class OptionsPanel {
        final private JPanel optionsPanel;

        final private JCheckBox suggestUsingStringCasting;

        public OptionsPanel() {
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new MigLayout());

            suggestUsingStringCasting = new JCheckBox("Suggest using (string)...", SUGGEST_USING_STRING_CASTING);
            suggestUsingStringCasting.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    SUGGEST_USING_STRING_CASTING = suggestUsingStringCasting.isSelected();
                }
            });
            optionsPanel.add(suggestUsingStringCasting, "wrap");
        }

        public JPanel getComponent() {
            return optionsPanel;
        }
    }
}
