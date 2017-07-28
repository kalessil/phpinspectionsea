package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.debug;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpClassMember;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jdom.Element;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

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
    private static final String message = "Please ensure this is not a forgotten debug statement.";

    private static final Map<String, Integer> functionsRequirements = new HashMap<>();

    private static final Pattern R_FQN_SPLITTER = Pattern.compile("::");

    static {
        /* function name => amount of arguments considered legal */
        functionsRequirements.put("debug_print_backtrace", -1);
        functionsRequirements.put("debug_zval_dump", -1);
        functionsRequirements.put("phpinfo", 1);
        functionsRequirements.put("print_r", 2);
        functionsRequirements.put("var_export", 2);
        functionsRequirements.put("var_dump", -1);
    }

    // Inspection options.
    public final List<String> configuration = new ArrayList<>();
    public boolean migratedIntoUserSpace;

    // Custom functions and methods.
    private final Collection<String>                customFunctions    = new HashSet<>();
    private final Map<String, Pair<String, String>> customMethods      = new HashMap<>();
    private final Collection<String>                customMethodsNames = new HashSet<>();

    public void registerCustomDebugMethod(@NotNull final String fqn) {
        configuration.add(fqn);
        recompileConfiguration();
    }

    @NotNull
    public String getShortName() {
        return "ForgottenDebugOutputInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethodReference(final MethodReference reference) {
                final String methodName = reference.getName();

                if (customMethods.isEmpty() || (methodName == null) || !customMethodsNames.contains(methodName)) {
                    return;
                }

                for (final Pair<String, String> match : customMethods.values()) {
                    final PsiElement resolved = methodName.equals(match.getSecond()) ? reference.resolve() : null;

                    if (resolved instanceof Method) {
                        final PhpClass clazz = ((PhpClassMember) resolved).getContainingClass();

                        if ((clazz != null) && match.getFirst().equals(clazz.getFQN())) {
                            holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            return;
                        }
                    }
                }
            }

            public void visitPhpFunctionCall(final FunctionReference reference) {
                final String functionName = reference.getName();

                if ((functionName != null) && customFunctions.contains(functionName)) {
                    final Integer paramsNeeded = functionsRequirements.get(functionName);

                    if ((paramsNeeded != null) && (reference.getParameters().length == paramsNeeded)) {
                        return;
                    }

                    if (!isBuffered(reference)) {
                        holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }

            private boolean isBuffered(@NotNull final PsiElement debugStatement) {
                PsiElement parent = debugStatement.getParent();

                /* statement can be prepended by @ (silence) */
                if (parent instanceof UnaryExpression) {
                    final PsiElement operation = ((UnaryExpression) parent).getOperation();

                    if ((operation != null) && (operation.getNode().getElementType() == PhpTokenTypes.opSILENCE)) {
                        parent = parent.getParent();
                    }
                }

                boolean result = false;

                /* ensure it's not prepended with 'ob_start();' */
                if (parent instanceof StatementImpl) {
                    final PsiElement preceding = ((PhpPsiElement) parent).getPrevPsiSibling();

                    if ((preceding != null) && OpenapiTypesUtil.isFunctionReference(preceding.getFirstChild())) {
                        final NavigationItem precedingCall         = (NavigationItem) preceding.getFirstChild();
                        final String         precedingFunctionName = precedingCall.getName();

                        if ("ob_start".equals(precedingFunctionName)) {
                            result = true;
                        }
                    }
                }

                return result;
            }
        };
    }

    public void readSettings(@NotNull final Element node) {
        XmlSerializer.deserializeInto(this, node);
        recompileConfiguration();
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> component.addList("Custom debug methods:", configuration, this::recompileConfiguration));
    }

    private void recompileConfiguration() {
        customFunctions.clear();
        customMethods.clear();
        customMethodsNames.clear();

        if (!migratedIntoUserSpace) {
            /* prepare migrated list */
            final Collection<String> migrated = new TreeSet<>();
            migrated.add("\\Codeception\\Util\\Debug::pause");
            migrated.add("\\Codeception\\Util\\Debug::debug");
            migrated.add("\\Symfony\\Component\\Debug\\Debug::enable");
            migrated.add("\\Symfony\\Component\\Debug\\ErrorHandler::register");
            migrated.add("\\Symfony\\Component\\Debug\\ExceptionHandler::register");
            migrated.add("\\Symfony\\Component\\Debug\\DebugClassLoader::enable");
            migrated.add("\\Zend\\Debug\\Debug::dump");
            migrated.add("\\Zend\\Di\\Display\\Console::export");
            migrated.add("debug_print_backtrace");
            migrated.add("debug_zval_dump");
            migrated.add("error_log");
            migrated.add("phpinfo");
            migrated.add("print_r");
            migrated.add("var_export");
            migrated.add("var_dump");
            // Laravel related.
            migrated.add("dd");
            migrated.add("dump");
            migrated.addAll(configuration);

            /* migrate the list */
            configuration.clear();
            configuration.addAll(migrated);
            migratedIntoUserSpace = true;

            /* cleanup */
            migrated.clear();
        }

        final Iterable<String> customDebugFQNs = new ArrayList<>(configuration);

        /* parse what was provided FQNs */
        for (String stringDescriptor : customDebugFQNs) {
            stringDescriptor = stringDescriptor.trim();

            if (!stringDescriptor.contains("::")) {
                customFunctions.add(stringDescriptor);
                continue;
            }

            final String[] disassembledDescriptor = R_FQN_SPLITTER.split(stringDescriptor, 2);

            customMethods.put(stringDescriptor.toLowerCase(), Pair.create(disassembledDescriptor[0], disassembledDescriptor[1]));
            customMethodsNames.add(disassembledDescriptor[1]);
        }
    }
}
