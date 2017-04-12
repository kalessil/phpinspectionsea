package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UnnecessaryElseFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ReturnTypeCanBeDeclaredInspector extends BasePhpInspection {
    private static final String messagePattern = "': %t%' can be declared as return type hint";

    private static final Set<String> returnTypes = new HashSet<>();
    static {
        /* +class/interface reference for PHP7.0+; +void for PHP7.1+ */
        returnTypes.add("self");
        returnTypes.add("array");
        returnTypes.add("callable");
        returnTypes.add("bool");
        returnTypes.add("float");
        returnTypes.add("int");
        returnTypes.add("string");
    }

    @NotNull
    public String getShortName() {
        return "ReturnTypeCanBeDeclaredInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                final Project project      = holder.getProject();
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel();
                if (php.hasFeature(PhpLanguageFeature.RETURN_TYPES) && null == method.getReturnType()) {
                    final PsiElement methodNameNode = NamedElementUtil.getNameIdentifier(method);
                    final boolean isMagicFunction   = method.getName().startsWith("__");
                    if (!isMagicFunction && null != methodNameNode) {
                        final boolean supportNullableTypes = php.hasFeature(PhpLanguageFeature.NULLABLES);
                        if (method.isAbstract()) {
                            handleAbstractMethod(method, methodNameNode, supportNullableTypes);
                        } else {
                            handleMethod(method, methodNameNode, supportNullableTypes);
                        }
                    }
                }
            }

            private void handleAbstractMethod(
                @NotNull Method method,
                @NotNull PsiElement target,
                boolean supportNullableTypes
            ) {
                if (null != method.getDocComment()) {
                    handleMethod(method, target, supportNullableTypes);
                }
            }

            private void handleMethod(
                @NotNull Method method,
                @NotNull PsiElement target,
                boolean supportNullableTypes
            ) {
                /* ignore DocBlock, resolve and normalize types instead (DocBlock is involved, but nevertheless) */
                final Set<String> normalizedTypes = new HashSet<>();
                for (String type : method.getType().global(holder.getProject()).filterUnknown().getTypes()) {
                    normalizedTypes.add(Types.getType(type));
                }

                final int typesCount = normalizedTypes.size();
                /* case 1: offer using void */
                if (0 == typesCount && supportNullableTypes) {
                    final String message = messagePattern.replace("%t%", "void");
                    holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
                /* case 2: offer using type */
                if (1 == typesCount) {
                    final String singleType = normalizedTypes.iterator().next();
                    final boolean isLegit   = singleType.startsWith("\\") || returnTypes.contains(singleType);
                    if (isLegit) {
                        final String message = messagePattern.replace("%t%", singleType);
                        holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
                /* case 3: offer using nullable type */
                if (2 == typesCount && supportNullableTypes) {
                    if (normalizedTypes.contains(Types.strNull)) {
                        normalizedTypes.remove(Types.strNull);

                        final String nullableType = normalizedTypes.iterator().next();
                        final boolean isLegit     = nullableType.startsWith("\\") || returnTypes.contains(nullableType);
                        if (isLegit) {
                            final String message = messagePattern.replace("%t%", "?" + nullableType);
                            holder.registerProblem(target, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                }
            }
        };
    }
}