package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.util.PhpStringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class VariableFunctionsUsageInspector extends BasePhpInspection {
    private static final String patternInlineArgs = "'%s' should be used instead (enables further analysis).";
    private static final String patternReplace    = "'%s' should be used instead.";

    @NotNull
    public String getShortName() {
        return "VariableFunctionsUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName == null || !functionName.startsWith("call_user_func")) {
                    return;
                }

                /* only `call_user_func_array(..., array(...))` needs to be checked */
                if (functionName.equals("call_user_func_array")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 2 && arguments[1] instanceof ArrayCreationExpression) {
                        final List<PsiElement> dispatched = this.extract((ArrayCreationExpression) arguments[1]);
                        if (!dispatched.isEmpty()) {
                            final boolean hasByReferences = dispatched.stream().anyMatch(argument -> this.argumentAsString(argument).startsWith("&"));
                            if (!hasByReferences) {
                                final String replacement = String.format(
                                        "call_user_func(%s, %s)",
                                        arguments[0].getText(),
                                        dispatched.stream().map(this::argumentAsString).collect(Collectors.joining(", "))
                                );
                                holder.registerProblem(reference, String.format(patternInlineArgs, replacement), new InlineFix(replacement));
                            }
                            dispatched.clear();
                        }
                    }
                } else if (functionName.equals("call_user_func")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > 0) {
                        /* extract callable */
                        final List<PsiElement> callable = new ArrayList<>();
                        if (arguments[0] instanceof ArrayCreationExpression) {
                            final List<PsiElement> extracted = this.extract((ArrayCreationExpression) arguments[0]);
                            if (!extracted.isEmpty()) {
                                /* false-positive: first part must not be a string - '<string>->...' is invalid code */
                                final PsiElement candidate = extracted.get(0);
                                if (candidate instanceof Variable) {
                                    final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) candidate, holder.getProject());
                                    final boolean skip     = resolved == null ||
                                                             resolved.hasUnknown() ||
                                                             resolved.getTypes().stream().anyMatch(type -> Types.getType(type).equals(Types.strString));
                                    if (skip) {
                                        extracted.clear();
                                        return;
                                    }
                                }
                                /* regular behaviour */
                                callable.addAll(extracted);
                                extracted.clear();
                            }
                        } else {
                            /* false-positive: $func(...) is not working for arrays in PHP below 5.4 */
                            if (arguments[0] instanceof Variable) {
                                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                                if (php == PhpLanguageLevel.PHP530) {
                                    return;
                                }
                            }
                            /* regular behaviour */
                            callable.add(arguments[0]);
                        }

                        PsiElement first  = !callable.isEmpty() ? callable.get(0) : null;
                        PsiElement second = callable.size() > 1 ? callable.get(1) : null;

                        /* check second part: in some cases it overrides the first one completely */
                        String secondAsString = null;
                        if (second != null) {
                            secondAsString = second instanceof Variable ? second.getText() : '{' + second.getText() + '}';
                            if (second instanceof StringLiteralExpression) {
                                final StringLiteralExpression secondPartExpression = (StringLiteralExpression) second;
                                if (null == secondPartExpression.getFirstPsiChild()) {
                                    final String content = secondPartExpression.getContents();
                                    secondAsString       = content;

                                    if (content.indexOf(':') != -1) {
                                        /* don't touch relative invocation at all */
                                        if (content.startsWith("parent::")) {
                                            return;
                                        }
                                        first      = second;
                                        second     = null;
                                        secondAsString = null;
                                    }
                                }
                            }
                        }

                        /* The first part should be a variable or string literal without injections */
                        String firstAsString = null;
                        if (first instanceof StringLiteralExpression) {
                            final StringLiteralExpression literal = (StringLiteralExpression) first;
                            if (literal.getFirstPsiChild() == null) {
                                firstAsString = PhpStringUtil.unescapeText(literal.getContents(), literal.isSingleQuote());
                            }
                        } else if (first instanceof Variable) {
                            firstAsString = first.getText();
                        }
                        if (null == firstAsString) {
                            return;
                        }


                        final String suggestedArguments = Stream.of(Arrays.copyOfRange(arguments, 1, arguments.length))
                                .map(this::argumentAsString)
                                .collect(Collectors.joining(", "));
                        final String replacement = "%f%%o%%s%(%p%)"
                            .replace("%o%%s%", secondAsString == null ? "" : "%o%%s%")
                            .replace("%p%", suggestedArguments)
                            .replace("%s%", secondAsString == null ? ""   : secondAsString)
                            .replace("%o%", first instanceof Variable ? "->" : "::")
                            .replace("%f%", firstAsString);

                        holder.registerProblem(
                                reference,
                                String.format(patternReplace, replacement),
                                new ReplaceFix(replacement)
                        );


                    }
                }
            }

//            private String callable(@Nullable PsiElement first) {
//            }

            @NotNull
            private List<PsiElement> extract(@NotNull ArrayCreationExpression container) {
                return Stream.of(container.getChildren())
                        .map(child -> {
                            if (child instanceof ArrayHashElement) {
                                return ((ArrayHashElement) child).getValue();
                            }
                            if (child instanceof PhpPsiElement) {
                                return ((PhpPsiElement) child).getFirstPsiChild();
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }

            @NotNull
            private String argumentAsString(@NotNull PsiElement parameter) {
                PsiElement previous = parameter.getPrevSibling();
                if (previous instanceof PsiWhiteSpace) {
                    previous = previous.getPrevSibling();
                }

                return OpenapiTypesUtil.is(previous, PhpTokenTypes.opBIT_AND)
                            ? '&' + parameter.getText()
                            : parameter.getText();
            }
        };
    }

    private static final class ReplaceFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use variable function instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        ReplaceFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class InlineFix extends UseSuggestedReplacementFixer {
        private static final String title = "Inline argument and inspect again";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        InlineFix(@NotNull String expression) {
            super(expression);
        }
    }
}

