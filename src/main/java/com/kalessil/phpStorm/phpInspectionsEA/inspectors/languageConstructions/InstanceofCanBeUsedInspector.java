package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                if (functionName != null) {
                    if (functionName.equals("get_class") || functionName.equals("get_parent_class")) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 1 && this.isTargetBinaryContext(reference) && this.isNotString(arguments[0])) {
                            final BinaryExpression binary = (BinaryExpression) reference.getParent();
                            final String fqn              = this.extractClassFqn(reference, binary);
                            if (fqn != null) {
                                this.analyze(reference, binary, fqn, !functionName.equals("get_class"));
                            }
                        }
                    }
                }
            }

            private void analyze(
                    @NotNull FunctionReference reference,
                    @NotNull BinaryExpression binary,
                    @NotNull String fqn,
                    boolean allowChildClasses
            ) {
                final PhpIndex index               = PhpIndex.getInstance(holder.getProject());
                final Collection<PhpClass> classes = OpenapiResolveUtil.resolveClassesByFQN(fqn, index);
                if (!classes.isEmpty() && (allowChildClasses || index.getDirectSubclasses(fqn).isEmpty())) {
                    final IElementType operator = binary.getOperationType();
                    final boolean isInverted    = operator == PhpTokenTypes.opNOT_IDENTICAL || operator == PhpTokenTypes.opNOT_EQUAL;
                    final String replacement = String.format(
                            isInverted ? "! %s instanceof %s" : "%s instanceof %s",
                            reference.getParameters()[0].getText(),
                            fqn
                    );
                    holder.registerProblem(binary, String.format(messagePattern, replacement), new UseInstanceofFix(replacement));
                }
            }

            @Nullable
            private String extractClassFqn(@NotNull FunctionReference reference, @NotNull BinaryExpression binary) {
                final PsiElement second = OpenapiElementsUtil.getSecondOperand(binary, reference);
                if (second instanceof StringLiteralExpression) {
                    final StringLiteralExpression string = (StringLiteralExpression) second;
                    final String clazz                   = string.getContents();
                    if (clazz.length() > 3 && !clazz.equals("__PHP_Incomplete_Class") && string.getFirstPsiChild() == null) {
                        return '\\' + clazz.replaceAll("\\\\\\\\", "\\\\");
                    }
                }
                return null;
            }

            private boolean isTargetBinaryContext(@NotNull FunctionReference reference) {
                final PsiElement parent = reference.getParent();
                if (parent instanceof BinaryExpression) {
                    return OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(((BinaryExpression) parent).getOperationType());

                }
                return false;
            }

            private boolean isNotString(@NotNull PsiElement subject) {
                if (subject instanceof PhpTypedElement && !(subject instanceof StringLiteralExpression)) {
                    final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) subject, subject.getProject());
                    if (resolved != null && !resolved.hasUnknown()) {
                        return resolved.getTypes().stream().noneMatch(type -> Types.getType(type).equals(Types.strString));
                    }
                }
                return false;
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
