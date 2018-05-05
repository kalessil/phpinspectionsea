package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnqualifiedReferenceInspector extends BasePhpInspection {
    private static final String messagePattern = "Using '\\%s' would enable some of opcode optimizations.";

    // Inspection options.
    public boolean REPORT_ALL_FUNCTIONS = false;
    public boolean REPORT_CONSTANTS     = false;

    final private static Set<String> falsePositives              = new HashSet<>();
    final private static Set<String> advancedOpcode              = new HashSet<>();
    final private static Map<String, Integer> callbacksPositions = new HashMap<>();
    static {
        falsePositives.add("true");
        falsePositives.add("TRUE");
        falsePositives.add("false");
        falsePositives.add("FALSE");
        falsePositives.add("null");
        falsePositives.add("NULL");

        falsePositives.add("__LINE__");
        falsePositives.add("__FILE__");
        falsePositives.add("__DIR__");
        falsePositives.add("__FUNCTION__");
        falsePositives.add("__CLASS__");
        falsePositives.add("__TRAIT__");
        falsePositives.add("__METHOD__");
        falsePositives.add("__NAMESPACE__");

        callbacksPositions.put("call_user_func", 0);
        callbacksPositions.put("call_user_func_array", 0);
        callbacksPositions.put("array_filter", 1);
        callbacksPositions.put("array_map", 0);
        callbacksPositions.put("array_walk", 1);
        callbacksPositions.put("array_reduce", 1);

        /* https://github.com/php/php-src/blob/f2db305fa4e9bd7d04d567822687ec714aedcdb5/Zend/zend_compile.c#L3872 */
        advancedOpcode.add("array_slice");
        advancedOpcode.add("assert");
        advancedOpcode.add("boolval");
        advancedOpcode.add("call_user_func");
        advancedOpcode.add("call_user_func_array");
        advancedOpcode.add("chr");
        advancedOpcode.add("count");
        advancedOpcode.add("defined");
        advancedOpcode.add("doubleval");
        advancedOpcode.add("floatval");
        advancedOpcode.add("func_get_args");
        advancedOpcode.add("func_num_args");
        advancedOpcode.add("get_called_class");
        advancedOpcode.add("get_class");
        advancedOpcode.add("gettype");
        advancedOpcode.add("in_array");
        advancedOpcode.add("intval");
        advancedOpcode.add("is_array");
        advancedOpcode.add("is_bool");
        advancedOpcode.add("is_double");
        advancedOpcode.add("is_float");
        advancedOpcode.add("is_int");
        advancedOpcode.add("is_integer");
        advancedOpcode.add("is_long");
        advancedOpcode.add("is_null");
        advancedOpcode.add("is_object");
        advancedOpcode.add("is_real");
        advancedOpcode.add("is_resource");
        advancedOpcode.add("is_string");
        advancedOpcode.add("ord");
        advancedOpcode.add("strlen");
        advancedOpcode.add("strval");
        advancedOpcode.add("function_exists");
        advancedOpcode.add("is_callable");
        advancedOpcode.add("extension_loaded");
        advancedOpcode.add("dirname");
        advancedOpcode.add("constant");
        advancedOpcode.add("define");
    }

    final private static Condition<PsiElement> PARENT_NAMESPACE = new Condition<PsiElement>() {
        public boolean value(PsiElement element) { return element instanceof PhpNamespace; }
        public String toString()                 { return "Condition.PARENT_NAMESPACE";    }
    };

    @NotNull
    public String getShortName() {
        return "UnqualifiedReferenceInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                /* ensure php version is at least PHP 7.0; makes sense only with PHP7+ opcode */
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP700) >= 0) {
                    final String functionName = reference.getName();
                    if (functionName != null) {
                        if (REPORT_ALL_FUNCTIONS || advancedOpcode.contains(functionName)) {
                            this.analyzeReference(reference);
                        }
                        if (callbacksPositions.containsKey(functionName)) {
                            this.analyzeCallback(reference, functionName);
                        }
                    }
                }
            }

            @Override
            public void visitPhpConstantReference(@NotNull ConstantReference reference) {
                if (REPORT_CONSTANTS) {
                    /* ensure php version is at least PHP 7.0; makes sense only with PHP7+ opcode */
                    final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(reference.getProject()).getLanguageLevel();
                    if (php.compareTo(PhpLanguageLevel.PHP700) >= 0) {
                        analyzeReference(reference);
                    }
                }
            }

            private void analyzeCallback(@NotNull FunctionReference reference, @NotNull String functionName) {
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length >= 2) {
                    final Integer callbackPosition = callbacksPositions.get(functionName);
                    if (arguments[callbackPosition] instanceof StringLiteralExpression) {
                        final StringLiteralExpression callback = (StringLiteralExpression) arguments[callbackPosition];
                        if (callback.getFirstPsiChild() == null) {
                            final String function     = callback.getContents();
                            final boolean isCandidate = !function.startsWith("\\") && !function.contains("::");
                            if (isCandidate && (REPORT_ALL_FUNCTIONS || advancedOpcode.contains(function))) {
                                final PhpIndex index = PhpIndex.getInstance(holder.getProject());
                                if (!index.getFunctionsByFQN('\\' + functionName).isEmpty()) {
                                    holder.registerProblem(
                                            callback,
                                            String.format(messagePattern, function),
                                            new TheLocalFix()
                                    );
                                }
                            }
                        }
                    }
                }
            }

            private void analyzeReference(@NotNull PhpReference reference) {
                /* constructs structure expectations */
                final String referenceName = reference.getName();
                if (referenceName == null) {
                    return;
                }
                /* some constants prefixing is making no sense IMO */
                if (reference instanceof ConstantReference && falsePositives.contains(referenceName)) {
                    return;
                }
                /* NS specification is identified differently for { define } and { call, constant } */
                final PsiElement nsCandidate = reference.getFirstChild();
                if (nsCandidate instanceof PhpNamespaceReference || OpenapiTypesUtil.is(nsCandidate, PhpTokenTypes.NAMESPACE_RESOLUTION)) {
                    return;
                }
                final PhpNamespace ns = (PhpNamespace) PsiTreeUtil.findFirstParent(reference, PARENT_NAMESPACE);
                if (ns == null) {
                    return;
                }

                /* resolve the constant/function, report if it's from the root NS */
                final PsiElement function = OpenapiResolveUtil.resolveReference(reference);
                final boolean isFunction  = function instanceof Function;
                if (isFunction || function instanceof Constant) {
                    /* false-positives: non-root NS function/constant referenced */
                    final String fqn = ((PhpNamedElement) function).getFQN();
                    if (fqn.length() != 1 + referenceName.length() || !fqn.equals('\\' + referenceName)) {
                        return;
                    }
                    /* false-positive: opcode-ed functions are imported already */
                    if (isFunction && advancedOpcode.contains(referenceName)) {
                        for (final PhpUse use : PsiTreeUtil.findChildrenOfType(ns, PhpUse.class)) {
                            final PsiElement candidate = use.getFirstPsiChild();
                            if (candidate instanceof FunctionReference) {
                                final String importedFunction = ((FunctionReference) candidate).getName();
                                if (importedFunction != null && referenceName.equals(importedFunction)) {
                                    return;
                                }
                            }
                        }
                    }

                    final String message = String.format(messagePattern, referenceName + (isFunction ? "(...)" : ""));
                    holder.registerProblem(reference, message, new TheLocalFix());
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Use the qualified reference";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target != null && !project.isDisposed()) {
                if (target instanceof FunctionReference || target instanceof ConstantReference) {
                    final PsiElement rootNs   = PhpPsiElementFactory.createNamespaceReference(project, "\\ ", false);
                    final PsiElement nameNode = target.getFirstChild();
                    nameNode.getParent().addBefore(rootNs, nameNode);
                } else if (target instanceof StringLiteralExpression) {
                    final StringLiteralExpression expression = (StringLiteralExpression) target;
                    final String quote                       = expression.isSingleQuote() ? "'" : "\"";
                    final String rootNs                      = expression.isSingleQuote() ? "\\" : "\\\\";
                    final String pattern                     = quote + rootNs + expression.getContents() + quote;
                    final StringLiteralExpression replacement
                            = PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, pattern);
                    target.replace(replacement);
                }
            }
        }
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create(component -> {
            component.addCheckbox("Report calls without opcode tweaks", REPORT_ALL_FUNCTIONS, (isSelected) -> REPORT_ALL_FUNCTIONS = isSelected);
            component.addCheckbox("Report constants references", REPORT_CONSTANTS, (isSelected) -> REPORT_CONSTANTS = isSelected);
        });
    }
}
