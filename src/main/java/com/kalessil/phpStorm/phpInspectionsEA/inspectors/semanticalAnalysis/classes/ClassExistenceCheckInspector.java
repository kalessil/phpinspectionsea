package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
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
    private static final String messageMismatch = "This call seems to always return false, please inspect the ::class expression.";
    private static final String messageString   = "This call might work not as expected, please specify the third argument.";

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

                final Project project          = holder.getProject();
                final PhpLanguageLevel php     = PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel();
                final boolean hasClassConstant = php.hasFeature(PhpLanguageFeature.CLASS_NAME_CONST);
                final String functionName      = reference.getName();
                if (hasClassConstant && functionName != null && callbacks.containsKey(functionName)) {
                    final PsiElement candidate;
                    final PsiElement[] arguments = reference.getParameters();
                    /* get target expression */
                    final int argumentsCount = arguments.length;
                    if (functionName.equals("is_subclass_of") || functionName.equals("is_a")) {
                        /* case 2: the object is a string, but the third argument is missing */
                        if (argumentsCount == 2 && arguments[0] instanceof PhpTypedElement) {
                            final PhpType types = OpenapiResolveUtil.resolveType((PhpTypedElement) arguments[0], project);
                            if (types != null && types.getTypes().stream().anyMatch(t -> Types.getType(t).equals(Types.strString))) {
                                holder.registerProblem(reference, messageString, ProblemHighlightType.GENERIC_ERROR);
                            }
                        }
                        candidate = argumentsCount >= 2 ? arguments[1] : null;
                    } else {
                        candidate = argumentsCount >= 1 ? arguments[0] : null;
                    }
                    /* case 1: the object mismatches the given class */
                    if (candidate instanceof ClassConstantReference) {
                        final ClassConstantReference argument = (ClassConstantReference) candidate;
                        final PsiElement targetClass          = argument.getClassReference();
                        final String constantName             = argument.getName();
                        if (constantName != null && constantName.equals("class") && targetClass instanceof ClassReference) {
                            final PsiElement resolved = OpenapiResolveUtil.resolveReference((ClassReference) targetClass);
                            if (resolved instanceof PhpClass && !callbacks.get(functionName).apply((PhpClass) resolved)) {
                                holder.registerProblem(reference, messageMismatch, ProblemHighlightType.GENERIC_ERROR);
                            }
                        }
                    }
                }
            }
        };
    }
}
