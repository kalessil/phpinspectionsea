package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ClassExistenceCheckInspector extends BasePhpInspection {
    private static final String message = "This call seems to always return false, please inspect the ::class expression.";

    final private static Map<String, Function<PhpClass, Boolean>> callbacks = new HashMap<>();
    static {
        callbacks.put("class_exists",     (clazz) -> !clazz.isInterface() && !clazz.isTrait());
        callbacks.put("interface_exists", PhpClass::isInterface);
        callbacks.put("trait_exists",     PhpClass::isTrait);
        callbacks.put("is_subclass_of",   (clazz) -> !clazz.isTrait());
        callbacks.put("is_a",             (clazz) -> !clazz.isTrait());
    }

    @NotNull
    public String getShortName() {
        return "ClassExistenceCheckInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final PhpLanguageLevel php     = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                final boolean hasClassConstant = php.hasFeature(PhpLanguageFeature.CLASS_NAME_CONST);
                final String functionName      = reference.getName();
                if (hasClassConstant && functionName != null && callbacks.containsKey(functionName)) {
                    final PsiElement candidate;
                    final PsiElement[] arguments = reference.getParameters();
                    /* get target expression */
                    if (functionName.equals("is_subclass_of") || functionName.equals("is_a")) {
                        candidate = arguments.length >= 2 ? arguments[1] : null;
                    } else {
                        candidate = arguments.length >= 1 ? arguments[0] : null;
                    }
                    /* check target expression */
                    if (candidate instanceof ClassConstantReference) {
                        final ClassConstantReference argument = (ClassConstantReference) candidate;
                        final PsiElement targetClass          = argument.getClassReference();
                        final String constantName             = argument.getName();
                        if (constantName != null && constantName.equals("class") && targetClass instanceof ClassReference) {
                            final PsiElement resolved = OpenapiResolveUtil.resolveReference((ClassReference) targetClass);
                            if (resolved instanceof PhpClass && !callbacks.get(functionName).apply((PhpClass) resolved)) {
                                holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);
                            }
                        }
                    }
                }
            }
        };
    }
}
