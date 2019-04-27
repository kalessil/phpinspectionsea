package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnusedFunctionResultInspector extends BasePhpInspection {
    // Inspection options.
    public boolean REPORT_ONLY_SCALARS      = false;
    public boolean REPORT_MIXED_TYPE        = false;
    public boolean REPORT_FLUENT_INTERFACES = false;

    private static final String message = "Function result is not used.";

    private static final Map<String, String> validFunctionsCache = new ConcurrentHashMap<>();
    private static final Set<String> ignoredFunctions            = new HashSet<>();
    static {
        ignoredFunctions.add("end");
        ignoredFunctions.add("next");
        ignoredFunctions.add("reset");
        ignoredFunctions.add("array_shift");
        ignoredFunctions.add("array_pop");
        ignoredFunctions.add("array_splice");

        ignoredFunctions.add("print_r");
        ignoredFunctions.add("exec");
        ignoredFunctions.add("system");
        ignoredFunctions.add("session_id");
        ignoredFunctions.add("session_name");
        ignoredFunctions.add("call_user_func_array");
        ignoredFunctions.add("call_user_func");

        ignoredFunctions.add("ini_set");
        ignoredFunctions.add("set_include_path");
        ignoredFunctions.add("set_error_handler");
        ignoredFunctions.add("set_exception_handler");
        ignoredFunctions.add("setlocale");
        ignoredFunctions.add("mb_internal_encoding");
    }

    @NotNull
    public String getShortName() {
        return "UnusedFunctionResultInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String methodName = reference.getName();
                if (methodName != null && !methodName.isEmpty() && !methodName.equals("__construct")) {
                    this.analyze(reference);
                }
            }

            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && !functionName.isEmpty()) {
                    final boolean skip = ignoredFunctions.contains(functionName) || validFunctionsCache.containsKey(functionName);
                    if (!skip) {
                        this.analyze(reference);
                    }
                }
            }

            private void analyze(@NotNull FunctionReference reference) {
                final boolean isTargetContext = OpenapiTypesUtil.isStatementImpl(reference.getParent());
                if (isTargetContext) {
                    final Project project  = reference.getProject();
                    final PhpType resolved = OpenapiResolveUtil.resolveType(reference, project);
                    if (resolved != null) {
                        final Set<String> types = resolved.filterUnknown().getTypes().stream()
                                .map(Types::getType)
                                .collect(Collectors.toSet());
                        types.remove(Types.strBoolean); /* APIs returning false on failures */
                        types.remove(Types.strInteger); /* APIs returning c-alike result codes */
                        types.remove(Types.strVoid);
                        types.remove(Types.strNull);
                        if (!types.isEmpty()) {
                            final PsiElement target = NamedElementUtil.getNameIdentifier(reference);
                            if (target != null) {
                                if (!REPORT_FLUENT_INTERFACES && reference instanceof MethodReference) {
                                    final PsiElement base = reference.getFirstChild();
                                    if (base instanceof PhpTypedElement) {
                                        final PhpType baseType = OpenapiResolveUtil.resolveType((PhpTypedElement) base, project);
                                        if (baseType != null) {
                                            baseType.getTypes().stream()
                                                    .filter(type  -> type.startsWith("\\"))
                                                    .forEach(type -> types.remove(Types.getType(type)));
                                        }
                                        types.remove(Types.strStatic);
                                    }
                                }

                                final boolean skip = (!REPORT_MIXED_TYPE && types.remove(Types.strMixed) && types.isEmpty()) ||
                                                     (!REPORT_FLUENT_INTERFACES && types.isEmpty()) ||
                                                     (REPORT_ONLY_SCALARS && types.removeIf(t -> t.startsWith("\\")) && types.isEmpty());
                                if (!skip) {
                                    holder.registerProblem(target, message);
                                }
                            }
                        } else {
                            /* there are no types we'd like to report, skip those functions processing */
                            if (OpenapiTypesUtil.isFunctionReference(reference)) {
                                final String functionName = reference.getName();
                                if (functionName != null) {
                                    validFunctionsCache.putIfAbsent(functionName, functionName);
                                }
                            }
                        }
                        types.clear();
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create(
            (component) -> {
                component.addCheckbox("Report only unused scalar results", REPORT_ONLY_SCALARS, (isSelected) -> REPORT_ONLY_SCALARS = isSelected);
                component.addCheckbox("Report 'mixed' type", REPORT_MIXED_TYPE, (isSelected) -> REPORT_MIXED_TYPE = isSelected);
                component.addCheckbox("Report fluent interfaces", REPORT_FLUENT_INTERFACES, (isSelected) -> REPORT_FLUENT_INTERFACES = isSelected);
            }
        );
    }
}
