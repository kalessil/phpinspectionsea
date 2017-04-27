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
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jdom.Element;

import javax.swing.*;
import java.util.*;

import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ForgottenDebugOutputInspector extends BasePhpInspection {
    // Inspection options.
    private final List<String> configuration = new ArrayList<>();

    public boolean defaultsTransferredToUserSpace = false;

    final private Set<String> customFunctions                     = new HashSet<>();
    final private Map<String, Pair<String, String>> customMethods = new HashMap<>();
    final private Set<String> customMethodsNames                  = new HashSet<>();

    // prepared content for smooth runtime
    static private final String message = "Please ensure this is not a forgotten debug statement.";

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

        final List<String> customDebugFQNs = new ArrayList<>();
        if (!this.defaultsTransferredToUserSpace) {
            /* prepare migrated list */
            final List<String> migrated = new ArrayList<>();
            migrated.add("\\Codeception\\Util\\Debug::pause");
            migrated.add("\\Codeception\\Util\\Debug::debug");
            migrated.add("\\Symfony\\Component\\Debug\\Debug::enable");
            migrated.add("\\Symfony\\Component\\Debug\\ErrorHandler::register");
            migrated.add("\\Symfony\\Component\\Debug\\ExceptionHandler::register");
            migrated.add("\\Symfony\\Component\\Debug\\DebugClassLoader::enable");
            migrated.add("\\Zend\\Debug\\Debug::dump");
            migrated.add("\\Zend\\Di\\Display\\Console::export");
            migrated.add("error_log");
            migrated.add("phpinfo");
            migrated.addAll(this.configuration);

            /* migrate the list */
            this.configuration.clear();
            this.configuration.addAll(migrated);
            this.defaultsTransferredToUserSpace = true;

            /* cleanup */
            migrated.clear();
        }
        customDebugFQNs.addAll(this.configuration);

        /* parse what was provided FQNs */
        for (String stringDescriptor : customDebugFQNs) {
            stringDescriptor = stringDescriptor.trim();
            if (!stringDescriptor.contains("::")) {
                customFunctions.add(stringDescriptor);
                continue;
            }

            final String[] disassembledDescriptor = stringDescriptor.split("::", 2);
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
                    final PsiElement resolved = reference.resolve();
                    if (!(resolved instanceof Method)) {
                        continue;
                    }

                    // analyze if class as needed
                    final PhpClass clazz = ((Method) resolved).getContainingClass();
                    if (null != clazz) {
                        final String classFqn = clazz.getFQN();
                        if (!StringUtil.isEmpty(classFqn) && match.getFirst().equals(classFqn)) {
                            holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            return;
                        }
                    }
                }
            }

            public void visitPhpFunctionCall(FunctionReference reference) {
                final String functionName = reference.getName();
                if (
                    null != functionName && functionsRequirements.containsKey(functionName) &&
                    reference.getParameters().length != functionsRequirements.get(functionName) // keep it here when function hit
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
                        if (null != preceding && OpenapiTypesUtil.isFunctionReference(preceding.getFirstChild())) {
                            final FunctionReference precedingCall = (FunctionReference) preceding.getFirstChild();
                            final String precedingFunction            = precedingCall.getName();
                            if (!StringUtil.isEmpty(precedingFunction) && precedingFunction.equals("ob_start")) {
                                return;
                            }
                        }
                    }

                    holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }

                /* user-defined functions */
                if (customFunctions.contains(functionName)) {
                    holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.createList("Custom debug methods:", configuration, this::recompileConfiguration);
        });
    }
}
