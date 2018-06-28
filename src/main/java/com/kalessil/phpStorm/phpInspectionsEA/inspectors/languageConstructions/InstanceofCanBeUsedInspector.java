package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class InstanceofCanBeUsedInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' can be used instead.";

    @NotNull
    public String getShortName() {
        return "InstanceofCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final String functionName = reference.getName();
                if (functionName != null) {
                    final PsiElement parent = reference.getParent();
                    if (functionName.equals("get_class")) {
                        if (parent instanceof BinaryExpression) {
                            this.checkGetClass(reference, (BinaryExpression) parent);
                        }
                    } else if (functionName.equals("class_parents") || functionName.equals("class_implements")) {
                        if (parent instanceof ParameterList) {
                            final PsiElement grandParent = parent.getParent();
                            if (OpenapiTypesUtil.isFunctionReference(grandParent)) {
                                this.checkHierarhyLookup(reference, (FunctionReference) grandParent);
                            }
                        }
                    }
                }
            }

            private void checkHierarhyLookup(@NotNull FunctionReference reference, @NotNull FunctionReference context) {
                final String functionName = context.getName();
                if (functionName != null && functionName.equals("in_array")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1) {
                        final PsiElement[] contextArguments = context.getParameters();
                        if (contextArguments.length > 0 && contextArguments[0] instanceof StringLiteralExpression) {
                            final StringLiteralExpression literal = (StringLiteralExpression) contextArguments[0];
                            final String clazz                    = literal.getContents();
                            if (clazz.length() > 3 && !clazz.equals("__PHP_Incomplete_Class") && literal.getFirstPsiChild() == null) {
                                final Project project              = holder.getProject();
                                final String fqn                   = '\\' + clazz.replaceAll("\\\\\\\\", "\\\\");
                                final Collection<PhpClass> classes
                                        = OpenapiResolveUtil.resolveClassesByFQN(fqn, PhpIndex.getInstance(project));
                                if (!classes.isEmpty()) {
                                    final PsiElement object = arguments[0];
                                    if (object instanceof PhpTypedElement) {
                                        final PhpType resolved
                                                = OpenapiResolveUtil.resolveType((PhpTypedElement) object, project);
                                        if (resolved != null) {
                                            final boolean isTarget = resolved.filterUnknown().getTypes().stream()
                                                    .noneMatch(t -> {
                                                        final String normalized = Types.getType(t);
                                                        return normalized.equals(Types.strMixed) || normalized.equals(Types.strString);
                                                    });
                                            if (isTarget) {
                                                final String replacement = String.format("%s instanceof %s", object.getText(), fqn);
                                                holder.registerProblem(
                                                        context,
                                                        String.format(messagePattern, replacement),
                                                        new UseInstanceofFix(replacement)
                                                );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private void checkGetClass(@NotNull FunctionReference reference, @NotNull BinaryExpression context) {
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length == 1) {
                    final IElementType operator = context.getOperationType();
                    if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operator)) {
                        final PsiElement second = OpenapiElementsUtil.getSecondOperand(context, reference);
                        if (second instanceof StringLiteralExpression) {
                            final StringLiteralExpression literal = (StringLiteralExpression) second;
                            final String clazz                    = literal.getContents();
                            if (clazz.length() > 3 && !clazz.equals("__PHP_Incomplete_Class") && literal.getFirstPsiChild() == null) {
                                final PhpIndex index               = PhpIndex.getInstance(holder.getProject());
                                final String fqn                   = '\\' + clazz.replaceAll("\\\\\\\\", "\\\\");
                                final Collection<PhpClass> classes = OpenapiResolveUtil.resolveClassesByFQN(fqn, index);
                                if (!classes.isEmpty() && index.getDirectSubclasses(fqn).isEmpty()) {
                                    final boolean isInverted =
                                            operator == PhpTokenTypes.opNOT_IDENTICAL || operator == PhpTokenTypes.opNOT_EQUAL;
                                    final String replacement = String.format(
                                            isInverted ? "! %s instanceof %s" : "%s instanceof %s",
                                            arguments[0].getText(),
                                            fqn
                                    );
                                    holder.registerProblem(
                                            context,
                                            String.format(messagePattern, replacement),
                                            new UseInstanceofFix(replacement)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseInstanceofFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use instanceof operator instead";

        UseInstanceofFix(@NotNull String suggestedReplacement) {
            super(suggestedReplacement);
        }

        @NotNull
        @Override
        public String getName() {
            return title;
        }
    }
}
