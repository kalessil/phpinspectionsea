package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.debug;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.gui.PrettyListControl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import net.miginfocom.swing.MigLayout;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ForgottenDebugOutputInspector extends BasePhpInspection {
    // custom configuration, automatically saved between restarts so keep out of changing modifiers
    @SuppressWarnings("WeakerAccess")
    final public LinkedList<String> configuration = new LinkedList<>();

    final private HashSet<String> customFunctions = new HashSet<>();
    final private HashMap<String, Pair<String, String>> customMethods = new HashMap<>();
    final private HashSet<String> customMethodsNames = new HashSet<>();

    // prepared content for smooth runtime
    static private final String strProblemDescription = "Please ensure this is not a forgotten debug statement";

    public ForgottenDebugOutputInspector() {
    }

    public void readSettings(@NotNull Element node) throws InvalidDataException {
        XmlSerializer.deserializeInto(this, node);
        recompileConfiguration();
    }

    public void registerCustomDebugMethod(@NotNull String fqn) {
        this.configuration.add(fqn);
        this.recompileConfiguration();
    }

    private void recompileConfiguration() {
        customFunctions.clear();
        customMethods.clear();
        customMethodsNames.clear();

        final List<String> customDebugFQNs = new LinkedList<>();
        customDebugFQNs.addAll(this.configuration);

        /* known debug methods from community */
        /* codeception */
        customDebugFQNs.add("\\Codeception\\Util\\Debug::pause");
        customDebugFQNs.add("\\Codeception\\Util\\Debug::debug");
        /* symfony 2/3; */
        customDebugFQNs.add("\\Symfony\\Component\\Debug\\Debug::enable");
        customDebugFQNs.add("\\Symfony\\Component\\Debug\\ErrorHandler::register");
        customDebugFQNs.add("\\Symfony\\Component\\Debug\\ExceptionHandler::register");
        customDebugFQNs.add("\\Symfony\\Component\\Debug\\DebugClassLoader::enable");
        /* ZF 2 */
        customDebugFQNs.add("\\Zend\\Debug\\Debug::dump");
        customDebugFQNs.add("\\Zend\\Di\\Display\\Console::export");
        /* known debug methods from community */

        /* parse what was provided FQNs */
        for (String stringDescriptor : customDebugFQNs) {
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
            customMethodsNames.add(disassembledDescriptor[1]);
        }
    }

    @NotNull
    public String getShortName() {
        return "ForgottenDebugOutputInspection";
    }

    private static final HashMap<String, Integer> functionsRequirements = new HashMap<>();
    static {
        /* function name => amount of arguments considered legal */
        functionsRequirements.put("print_r",               2);
        functionsRequirements.put("var_export",            2);
        functionsRequirements.put("var_dump",              -1);
        functionsRequirements.put("debug_zval_dump",       -1);
        functionsRequirements.put("debug_print_backtrace", -1);
        functionsRequirements.put("phpinfo",               -1);
        functionsRequirements.put("error_log",             -1);
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethodReference(MethodReference reference) {
                final String name = reference.getName();
                if (0 == customMethods.size() || StringUtil.isEmpty(name) || !customMethodsNames.contains(name)) {
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
                final String function = reference.getName();
                if (
                    !StringUtil.isEmpty(function) && functionsRequirements.containsKey(function) &&
                    reference.getParameters().length != functionsRequirements.get(function) // keep it here when function hit
                ) {
                    PsiElement parent = reference.getParent();

                    /* statement can be prepended by @ (silence) */
                    if (parent instanceof UnaryExpression) {
                        final PsiElement operation = ((UnaryExpression) parent).getOperation();
                        if (null != operation && PhpTokenTypes.opSILENCE == operation.getNode().getElementType()) {
                            parent = parent.getParent();
                        }
                    }

                    /* ensure it's not prepended with 'ob_start();' */
                    if (parent instanceof StatementImpl) {
                        final PsiElement preceding = ((StatementImpl) parent).getPrevPsiSibling();
                        if (null != preceding && preceding.getFirstChild() instanceof FunctionReferenceImpl) {
                            final FunctionReferenceImpl precedingCall = (FunctionReferenceImpl) preceding.getFirstChild();
                            final String precedingFunction            = precedingCall.getName();
                            if (!StringUtil.isEmpty(precedingFunction) && precedingFunction.equals("ob_start")) {
                                return;
                            }
                        }
                    }

                    holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }

                /* user-defined functions */
                if (customFunctions.contains(function)) {
                    holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return (new ForgottenDebugOutputInspector.OptionsPanel()).getComponent();
    }

    private class OptionsPanel {
        final private JPanel optionsPanel;

        private OptionsPanel() {
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new MigLayout());

            // inject controls
            optionsPanel.add(new JLabel("Custom debug methods:"), "wrap");
            optionsPanel.add((new PrettyListControl(configuration) {
                protected void fireContentsChanged() {
                    recompileConfiguration();
                    super.fireContentsChanged();
                }
            }).getComponent(), "pushx, growx");
        }

        private JPanel getComponent() {
            return optionsPanel;
        }
    }
}
