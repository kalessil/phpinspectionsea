package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.HashMap;
import java.util.HashSet;

public class ForgottenDebugOutputInspector extends BasePhpInspection {
    // comma separated list of items
    public String configuration = "";

    // prepared content for smooth runtime
    HashSet<String> customFunctions = new HashSet<String>();
    HashMap<String, Pair<String, String>> customMethods = new HashMap<String, Pair<String, String>>();

    private static final String strProblemDescription = "Please ensure this is not forgotten debug statement";

    public ForgottenDebugOutputInspector() {
    }

    @NotNull
    public String getShortName() {
        return "ForgottenDebugOutputInspection";
    }

    private HashMap<String, Integer> functionsRequirements = null;
    private HashMap<String, Integer> getFunctionsRequirements() {
        if (null == functionsRequirements) {
            functionsRequirements = new HashMap<String, Integer>();

            /* function name => amount of arguments considered legal */
            functionsRequirements.put("print_r",               2);
            functionsRequirements.put("var_export",            2);
            functionsRequirements.put("var_dump",              -1);
            functionsRequirements.put("debug_zval_dump",       -1);
            functionsRequirements.put("debug_print_backtrace", -1);
        }

        return functionsRequirements;
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethodReference(MethodReference reference) {
                final String name = reference.getName();
                if (0 == customMethods.size() || StringUtil.isEmpty(name)) {
                    return;
                }

                for (Pair<String, String> match : customMethods.values()) {
                    if (!name.equals(match.getSecond())) {
                        continue;
                    }

                    // resolve as method
                    PsiElement resolved = reference.resolve();
                    if (!(resolved instanceof Method)) {
                        continue;
                    }

                    // analyze if class as needed
                    PhpClass clazz = ((Method) resolved).getContainingClass();
                    if (null != clazz) {
                        String classFqn = clazz.getFQN();
                        if (!StringUtil.isEmpty(classFqn) && match.getFirst().equals(classFqn)) {
                            holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            return;
                        }
                    }
                }
            }

            public void visitPhpFunctionCall(FunctionReference reference) {
                final String strFunction              = reference.getName();
                HashMap<String, Integer> requirements = getFunctionsRequirements();
                if (
                    !StringUtil.isEmpty(strFunction) && requirements.containsKey(strFunction) &&
                    reference.getParameters().length != requirements.get(strFunction)
                ) {
                    holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }

                // user-defined functions
                if (customFunctions.contains(strFunction)) {
                    holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return (new ForgottenDebugOutputInspector.OptionsPanel()).getComponent();
    }

    public class OptionsPanel {
        private JPanel myMainPanel;
        private JTextField myCustomDebugFunctionsInput;

        public OptionsPanel() {
            myMainPanel = new JPanel();

            myCustomDebugFunctionsInput = new JTextField(configuration, 34);
            myCustomDebugFunctionsInput.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    stateChanged(e);
                }
                public void removeUpdate(DocumentEvent e) {
                    stateChanged(e);
                }
                public void insertUpdate(DocumentEvent e) {
                    stateChanged(e);
                }
                public void stateChanged(DocumentEvent e) {
                    setDebugDescriptors(myCustomDebugFunctionsInput.getText());
                }
            });

            // alignment strategy
            myMainPanel.setLayout(new MigLayout());

            // inject controls
            myMainPanel.add(new JLabel("Custom debug methods:"), "span, grow");
            myMainPanel.add(myCustomDebugFunctionsInput, "span, grow");
        }

        private void setDebugDescriptors(@Nullable String newOnce) {
            configuration = newOnce;
            customFunctions.clear();
            customMethods.clear();

            // empty once
            if (StringUtil.isEmpty(configuration)) {
                return;
            }

            // parse what was provided
            for (String stringDescriptor : configuration.split(",")) {
                stringDescriptor = stringDescriptor.trim();
                if (!stringDescriptor.contains("::")) {
                    customFunctions.add(stringDescriptor);
                    continue;
                }

                String[] disassembledDescriptor = stringDescriptor.split("::", 2);
                customMethods.put(
                    stringDescriptor.toLowerCase(),
                    Pair.create(disassembledDescriptor[0], disassembledDescriptor[1])
                );
            }
        }

        public JPanel getComponent() {
            return myMainPanel;
        }
    }
}
