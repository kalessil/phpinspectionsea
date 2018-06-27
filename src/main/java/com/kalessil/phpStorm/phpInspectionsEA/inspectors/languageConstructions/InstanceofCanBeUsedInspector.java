package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
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
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("get_class")) {
                    final PsiElement[] arguments = reference.getParameters();
                    final PsiElement parent      = reference.getParent();
                    if (arguments.length == 1 && parent instanceof BinaryExpression) {
                        final BinaryExpression binary = (BinaryExpression) parent;
                        final IElementType operator   = binary.getOperationType();
                        if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operator)) {
                            final PsiElement second = OpenapiElementsUtil.getSecondOperand(binary, reference);
                            if (second instanceof StringLiteralExpression) {
                                final StringLiteralExpression string = (StringLiteralExpression) second;
                                final String clazz                   = string.getContents();
                                if (clazz.length() > 3 && !clazz.equals("__PHP_Incomplete_Class") && string.getFirstPsiChild() == null) {
                                    final PhpIndex index               = PhpIndex.getInstance(holder.getProject());
                                    final String fqn                   = '\\' + clazz.replaceAll("\\\\\\\\", "\\\\");
                                    final Collection<PhpClass> classes = OpenapiResolveUtil.resolveClassesByFQN(fqn, index);
                                    if (!classes.isEmpty() && index.getDirectSubclasses(fqn).isEmpty()) {
                                        final boolean isInverted =
                                                operator == PhpTokenTypes.opNOT_IDENTICAL ||
                                                operator == PhpTokenTypes.opNOT_EQUAL;
                                        final String replacement = String.format(
                                                isInverted ? "! %s instanceof %s" : "%s instanceof %s",
                                                arguments[0].getText(),
                                                fqn
                                        );
                                        holder.registerProblem(
                                                binary,
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
        };
    }

    private static class UseInstanceofFix extends UseSuggestedReplacementFixer {
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
