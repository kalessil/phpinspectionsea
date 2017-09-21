package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.debug;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

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
    public final List<String> configuration = new ArrayList<>();
    public boolean migratedIntoUserSpace    = false;

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
        this.customFunctions.clear();
        this.customMethods.clear();
        this.customMethodsNames.clear();

        final List<String> customDebugFQNs = new ArrayList<>();
        if (!this.migratedIntoUserSpace) {
            /* prepare migrated list */
            final Set<String> migrated = optionConfigurationDefaults();
            migrated.addAll(this.configuration);

            /* migrate the list */
            this.configuration.clear();
            this.configuration.addAll(migrated);
            this.migratedIntoUserSpace = true;

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
    private static Set<String> optionConfigurationDefaults() {
        final Set<String> migrated = new TreeSet<>();
        migrated.add("\\Codeception\\Util\\Debug::pause");
        migrated.add("\\Codeception\\Util\\Debug::debug");
        migrated.add("\\Doctrine\\Common\\Util\\Debug::dump");
        migrated.add("\\Doctrine\\Common\\Util\\Debug::export");
        migrated.add("\\Symfony\\Component\\Debug\\Debug::enable");
        migrated.add("\\Symfony\\Component\\Debug\\ErrorHandler::register");
        migrated.add("\\Symfony\\Component\\Debug\\ExceptionHandler::register");
        migrated.add("\\Symfony\\Component\\Debug\\DebugClassLoader::enable");
        migrated.add("\\Zend\\Debug\\Debug::dump");
        migrated.add("\\Zend\\Di\\Display\\Console::export");
        migrated.add("\\Illuminate\\Support\\Debug\\Dumper::dump");
        migrated.add("dd");
        migrated.add("debug_print_backtrace");
        migrated.add("debug_zval_dump");
        migrated.add("error_log");
        migrated.add("phpinfo");
        migrated.add("print_r");
        migrated.add("var_export");
        migrated.add("var_dump");

        return migrated;
    }

    @NotNull
    public String getShortName() {
        return "ForgottenDebugOutputInspection";
    }

    private static final Map<String, Integer> functionsRequirements = new HashMap<>();
    static {
        /* function name => amount of arguments considered legal */
        functionsRequirements.put("debug_print_backtrace", -1);
        functionsRequirements.put("debug_zval_dump",       -1);
        functionsRequirements.put("phpinfo",                1);
        functionsRequirements.put("print_r",                2);
        functionsRequirements.put("var_export",             2);
        functionsRequirements.put("var_dump",               -1);
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethodReference(MethodReference reference) {
                final String methodName = reference.getName();
                if (customMethods.isEmpty() || methodName == null || !customMethodsNames.contains(methodName)) {
                    return;
                }

                for (final Pair<String, String> match : customMethods.values()) {
                    final PsiElement resolved
                        = methodName.equals(match.getSecond()) ? OpenapiResolveUtil.resolveReference(reference) : null;
                    if (resolved instanceof Method) {
                        final PhpClass clazz = ((Method) resolved).getContainingClass();
                        if (clazz != null && match.getFirst().equals(clazz.getFQN())) {
                            holder.registerProblem(reference, message);
                            return;
                        }
                    }
                }
            }

            public void visitPhpFunctionCall(FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && customFunctions.contains(functionName)) {
                    final Integer paramsNeeded = functionsRequirements.get(functionName);
                    if (paramsNeeded != null && reference.getParameters().length == paramsNeeded) {
                        return;
                    }
                    if (!this.isBuffered(reference)) {
                        holder.registerProblem(reference, message);
                    }
                }
            }

            private boolean isBuffered(@NotNull PsiElement debugStatement) {
                boolean result = false;

                PsiElement parent = debugStatement.getParent();
                /* statement can be prepended by @ (silence) */
                if (parent instanceof UnaryExpression) {
                    final PsiElement operation = ((UnaryExpression) parent).getOperation();
                    if (operation != null && operation.getNode().getElementType() == PhpTokenTypes.opSILENCE) {
                        parent = parent.getParent();
                    }
                }
                /* ensure it's not prepended with 'ob_start();' */
                if (OpenapiTypesUtil.isStatementImpl(parent)) {
                    final PsiElement preceding = ((Statement) parent).getPrevPsiSibling();
                    if (preceding != null && OpenapiTypesUtil.isFunctionReference(preceding.getFirstChild())) {
                        final FunctionReference precedingCall = (FunctionReference) preceding.getFirstChild();
                        final String precedingFunctionName    = precedingCall.getName();
                        if (precedingFunctionName != null && precedingFunctionName.equals("ob_start")) {
                            result = true;
                        }
                    }
                }

                return result;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create(
            (component) -> component.addList("Custom debug methods:",
                                             configuration, ForgottenDebugOutputInspector::optionConfigurationDefaults,
                                             this::recompileConfiguration,
                                             "Adding custom debug function...", "Examples: \"function_name\" or \"\\Namespace\\Class::method\"")
        );
    }
}
