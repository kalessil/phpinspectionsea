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
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
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

        /* parse what was provided FQNs */
        final List<String> customDebugFQNs = new ArrayList<>(this.configuration);
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
        customDebugFQNs.clear();
    }

    @NotNull
    private static Set<String> optionConfigurationDefaults() {
        final Set<String> list = new TreeSet<>();
        /* Codeception-related debug functions */
        list.add("\\Codeception\\Util\\Debug::pause");
        list.add("\\Codeception\\Util\\Debug::debug");
        /* Doctrine-related debug functions */
        list.add("\\Doctrine\\Common\\Util\\Debug::dump");
        list.add("\\Doctrine\\Common\\Util\\Debug::export");
        /* Symfony-related debug functions */
        list.add("\\Symfony\\Component\\Debug\\Debug::enable");
        list.add("\\Symfony\\Component\\Debug\\ErrorHandler::register");
        list.add("\\Symfony\\Component\\Debug\\ExceptionHandler::register");
        list.add("\\Symfony\\Component\\Debug\\DebugClassLoader::enable");
        /* Zend-related debug functions */
        list.add("\\Zend\\Debug\\Debug::dump");
        list.add("\\Zend\\Di\\Display\\Console::export");
        /* Typo3-related debug functions */
        list.add("\\TYPO3\\CMS\\Core\\Utility\\DebugUtility::debug");
        /* Laravel-related debug functions */
        list.add("\\Illuminate\\Support\\Debug\\Dumper::dump");
        list.add("dd");
        /* Core-php debug functions */
        list.add("debug_print_backtrace");
        list.add("debug_zval_dump");
        list.add("error_log");
        list.add("phpinfo");
        list.add("print_r");
        list.add("var_export");
        list.add("var_dump");
        /* Drupal-related debug functions */
        list.add("dpm");
        list.add("dsm");
        list.add("dvm");
        list.add("kpr");
        list.add("dpq");
        /* PHP XDebug extension */
        list.add("xdebug_break");
        list.add("xdebug_call_class");
        list.add("xdebug_call_file");
        list.add("xdebug_call_function");
        list.add("xdebug_call_line");
        list.add("xdebug_code_coverage_started");
        list.add("xdebug_debug_zval");
        list.add("xdebug_debug_zval_stdout");
        list.add("xdebug_dump_superglobals");
        list.add("xdebug_enable");
        list.add("xdebug_get_code_coverage");
        list.add("xdebug_get_collected_errors");
        list.add("xdebug_get_declared_vars");
        list.add("xdebug_get_function_stack");
        list.add("xdebug_get_headers");
        list.add("xdebug_get_monitored_functions");
        list.add("xdebug_get_profiler_filename");
        list.add("xdebug_get_stack_depth");
        list.add("xdebug_get_tracefile_name");
        list.add("xdebug_is_enabled");
        list.add("xdebug_memory_usage");
        list.add("xdebug_peak_memory_usage");
        list.add("xdebug_print_function_stack");
        list.add("xdebug_start_code_coverage");
        list.add("xdebug_start_error_collection");
        list.add("xdebug_start_function_monitor");
        list.add("xdebug_start_trace");
        list.add("xdebug_stop_code_coverage");
        list.add("xdebug_stop_error_collection");
        list.add("xdebug_stop_function_monitor");
        list.add("xdebug_stop_trace");
        list.add("xdebug_time_index");
        list.add("xdebug_var_dump");

        return list;
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
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                final String methodName = reference.getName();
                if (customMethods.isEmpty() || methodName == null || !customMethodsNames.contains(methodName)) {
                    return;
                }

                for (final Pair<String, String> match : customMethods.values()) {
                    final PsiElement resolved
                        = methodName.equals(match.getSecond()) ? OpenapiResolveUtil.resolveReference(reference) : null;
                    if (resolved instanceof Method) {
                        final PhpClass clazz = ((Method) resolved).getContainingClass();
                        if (clazz != null && match.getFirst().equals(clazz.getFQN()) && !this.isInDebugFunction(reference)) {
                            holder.registerProblem(reference, message);
                            return;
                        }
                    }
                }
            }

            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && customFunctions.contains(functionName)) {
                    final Integer paramsNeeded = functionsRequirements.get(functionName);
                    if (paramsNeeded == null || reference.getParameters().length != paramsNeeded) {
                        final boolean isValidContext = this.isBuffered(reference) || this.isInDebugFunction(reference);
                        if (!isValidContext) {
                            holder.registerProblem(reference, message);
                        }
                    }
                }
            }

            private boolean isInDebugFunction(@NotNull PsiElement debugStatement) {
                final Function scope = ExpressionSemanticUtil.getScope(debugStatement);
                return scope != null && configuration.contains(scope instanceof Method ? scope.getFQN() : scope.getName());
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
        return OptionsComponent.create((component)
            -> component.addList(
                "Custom debug methods:",
                configuration,
                ForgottenDebugOutputInspector::optionConfigurationDefaults,
                this::recompileConfiguration,
                "Adding custom debug function...",
                "Examples: 'function_name' or '\\Namespace\\Class::method'"
            )
        );
    }
}
