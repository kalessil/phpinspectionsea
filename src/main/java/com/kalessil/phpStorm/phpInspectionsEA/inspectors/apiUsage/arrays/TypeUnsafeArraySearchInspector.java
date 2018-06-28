package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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

public class TypeUnsafeArraySearchInspector extends BasePhpInspection {
    private static final String message = "Third parameter should be provided to clarify if type safety is important in this context.";

    @NotNull
    public String getShortName() {
        return "TypeUnsafeArraySearchInspection";
    }

    private static final Set<String> targetFunctions = new HashSet<>();
    static {
        targetFunctions.add("array_search");
        targetFunctions.add("in_array");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && targetFunctions.contains(functionName)) {
                    final PsiElement[] arguments =  reference.getParameters();
                    if (arguments.length == 2) {
                        /* false-positives: array of string literals */
                        if (arguments[1] instanceof ArrayCreationExpression) {
                            final PsiElement[] elements = arguments[1].getChildren();
                            if (elements.length > 0) {
                                final long validElementsCount = Arrays.stream(elements)
                                    .filter(element -> OpenapiTypesUtil.is(element, PhpElementTypes.ARRAY_VALUE))
                                        .map(PsiElement::getFirstChild)
                                    .filter(element -> element instanceof StringLiteralExpression)
                                        .map(literal -> ((StringLiteralExpression) literal).getContents().trim())
                                    .filter(content -> !content.isEmpty() && !content.matches("^\\d+$"))
                                        .count();
                                if (validElementsCount == elements.length) {
                                    return;
                                }
                            }
                        }

                        /* false-positives: array and item types are complimentary */
                        if (arguments[0] instanceof PhpTypedElement && arguments[1] instanceof PhpTypedElement) {
                            final Project project        = reference.getProject();
                            final PhpType arrayType      = OpenapiResolveUtil.resolveType((PhpTypedElement) arguments[1], project);
                            final Set<String> arrayTypes = arrayType == null ? null : arrayType.filterUnknown().getTypes();
                            if (arrayTypes != null && arrayTypes.size() == 1) {
                                final PhpType itemType      = OpenapiResolveUtil.resolveType((PhpTypedElement) arguments[0], project);
                                final Set<String> itemTypes = itemType == null ? null : itemType.filterUnknown().getTypes();
                                if (itemTypes != null && itemTypes.size() == 1) {
                                    final boolean matching = this.areTypesMatching(itemTypes.iterator().next(), arrayTypes.iterator().next());
                                    if (matching) {
                                        return;
                                    }
                                }
                            }
                        }

                        /* general case: we need the third argument */
                        final String replacement = String.format(
                                "%s(%s, %s, true)",
                                functionName,
                                arguments[0].getText(),
                                arguments[1].getText()
                        );
                        holder.registerProblem(reference, message, new MakeSearchTypeSensitiveFix(replacement));
                    }
                }
            }

            private boolean areTypesMatching(@NotNull String itemType, @NotNull String arrayType) {
                boolean result = false;
                if (!itemType.isEmpty()) {
                    result = arrayType.equals((itemType.charAt(0) == '\\' ? itemType : '\\' + itemType) + "[]");
                }
                return result;
            }
        };
    }

    private static final class MakeSearchTypeSensitiveFix extends UseSuggestedReplacementFixer {
        private static final String title = "Add 'true' as the third argument";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        MakeSearchTypeSensitiveFix(@NotNull String expression) {
            super(expression);
        }
    }
}
