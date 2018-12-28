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
                        final List<PsiElement> dispatched = this.extractArguments((ArrayCreationExpression) arguments[1]);
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
                    return;
                }


                /* TODO: `callReturningCallable()(...)` syntax not yet supported, re-evaluate */
                if (functionName.equals("call_user_func")) {
                    final PsiElement[] arguments = reference.getParameters();
                    /* collect callable parts */
                    final PsiElement firstParam          = arguments[0];
                    final List<PsiElement> callableParts = new ArrayList<>();
                    if (firstParam instanceof ArrayCreationExpression) {
                        /* extract parts */
                        PhpPsiElement firstPart  = ((ArrayCreationExpression) firstParam).getFirstPsiChild();
                        PhpPsiElement secondPart = null == firstPart ? null : firstPart.getNextPsiSibling();

                        /* now expression themselves */
                        firstPart  = null == firstPart  ? null : firstPart.getFirstPsiChild();
                        secondPart = null == secondPart ? null : secondPart.getFirstPsiChild();
                        if (firstPart == null || secondPart == null) {
                            return;
                        }
                        /* false-positive: first part must not be a string - '<string>->...' is invalid code */
                        if (firstPart instanceof Variable) {
                            final PhpType type = OpenapiResolveUtil.resolveType((PhpTypedElement) firstPart, holder.getProject());
                            if (type != null) {
                                /* incompletely resolved types, we shouldn't continue */
                                if (type.hasUnknown()) {
                                    return;
                                }
                                /* '<string>->method(...)' breaks at runtime */
                                else if (type.getTypes().stream().anyMatch(t -> Types.getType(t).equals(Types.strString))) {
                                    return;
                                }
                            }
                        }

                        callableParts.add(firstPart);
                        callableParts.add(secondPart);
                    } else {
                        callableParts.add(firstParam);
                    }


                    /* extract parts into local variables for further processing */
                    PsiElement firstPart  = !callableParts.isEmpty() ? callableParts.get(0) : null;
                    PsiElement secondPart = callableParts.size() > 1 ? callableParts.get(1) : null;
                    callableParts.clear();


                    /* check second part: in some cases it overrides the first one completely */
                    String secondAsString = null;
                    if (secondPart != null) {
                        secondAsString = secondPart instanceof Variable ? secondPart.getText() : '{' + secondPart.getText() + '}';
                        if (secondPart instanceof StringLiteralExpression) {
                            final StringLiteralExpression secondPartExpression = (StringLiteralExpression) secondPart;
                            if (null == secondPartExpression.getFirstPsiChild()) {
                                final String content = secondPartExpression.getContents();
                                secondAsString       = content;

                                if (content.indexOf(':') != -1) {
                                    /* don't touch relative invocation at all */
                                    if (content.startsWith("parent::")) {
                                        return;
                                    }
                                    firstPart      = secondPart;
                                    secondPart     = null;
                                    secondAsString = null;
                                }
                            }
                        }
                    }

                    /* The first part should be a variable or string literal without injections */
                    String firstAsString = null;
                    if (null != firstParam) {
                        if (firstPart instanceof StringLiteralExpression) {
                            final StringLiteralExpression firstPartExpression = (StringLiteralExpression) firstPart;
                            if (firstPartExpression.getFirstPsiChild() == null) {
                                firstAsString = PhpStringUtil.unescapeText(firstPartExpression.getContents(), firstPartExpression.isSingleQuote());
                            }
                        }
                        if (firstPart instanceof Variable) {
                            firstAsString = firstPart.getText();
                        }
                    }
                    if (null == firstAsString) {
                        return;
                    }


                    /* $func(...) is not working for arrays in PHP below 5.4 */
                    if (secondPart == null && firstPart instanceof Variable) {
                        PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                        if (PhpLanguageLevel.PHP530 == php) {
                            return;
                        }
                    }


                    final String suggestedArguments = Stream.of(Arrays.copyOfRange(arguments, 1, arguments.length))
                            .map(this::argumentAsString)
                            .collect(Collectors.joining(", "));
                    final String replacement = "%f%%o%%s%(%p%)"
                        .replace("%o%%s%", null == secondPart ? "" : "%o%%s%")
                        .replace("%p%", suggestedArguments)
                        .replace("%s%", null == secondPart            ? ""   : secondAsString)
                        .replace("%o%", firstPart instanceof Variable ? "->" : "::")
                        .replace("%f%", firstAsString);

                    holder.registerProblem(
                            reference,
                            String.format(patternReplace, replacement),
                            new ReplaceFix(replacement)
                    );
                }
            }

            @NotNull
            private List<PsiElement> extractArguments(@NotNull ArrayCreationExpression container) {
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

