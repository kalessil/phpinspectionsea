package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Couple;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.util.PhpStringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
    private static final String patternInlineArgs = "'%s' would make possible to perform better code analysis here.";
    private static final String patternReplace    = "'%s' would make more sense here (it also faster).";

    final private static Map<String, String> arrayFunctionsMapping = new HashMap<>();
    final private static Set<String> callerFunctions               = new HashSet<>();
    static {
        arrayFunctionsMapping.put("call_user_func_array", "call_user_func");
        arrayFunctionsMapping.put("forward_static_call_array", "forward_static_call");

        callerFunctions.add("call_user_func");
        callerFunctions.add("forward_static_call");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "VariableFunctionsUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Variable functions usage";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null) {
                    /* case: `call_user_func_array(..., array(...))` */
                    if (arrayFunctionsMapping.containsKey(functionName)) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 2 && arguments[1] instanceof ArrayCreationExpression) {
                            final List<PsiElement> dispatched = this.extract((ArrayCreationExpression) arguments[1]);
                            if (!dispatched.isEmpty()) {
                                final boolean hasByReferences = dispatched.stream().anyMatch(argument -> this.argumentAsString(argument).startsWith("&"));
                                if (!hasByReferences) {
                                    final String replacement = String.format(
                                            "%s(%s, %s)",
                                            arrayFunctionsMapping.get(functionName),
                                            arguments[0].getText(),
                                            dispatched.stream().map(this::argumentAsString).collect(Collectors.joining(", "))
                                    );
                                    holder.registerProblem(
                                            reference,
                                            String.format(MessagesPresentationUtil.prefixWithEa(patternInlineArgs), replacement),
                                            new InlineFix(replacement)
                                    );
                                }
                                dispatched.clear();
                            }
                        }
                    } else if (callerFunctions.contains(functionName)) {
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
                                if (arguments[0] instanceof Variable && PhpLanguageLevel.get(holder.getProject()).below(PhpLanguageLevel.PHP540)) {
                                    return;
                                }
                                /* regular behaviour */
                                callable.add(arguments[0]);
                            }

                            final PsiElement first  = !callable.isEmpty() ? callable.get(0) : null;
                            final PsiElement second = callable.size() > 1 ? callable.get(1) : null;
                            final Couple<String> suggestedCallable = this.callable(first, second);
                            if (suggestedCallable.getFirst() != null) {
                                final String replacement;
                                if (suggestedCallable.getSecond() != null) {
                                    final boolean useScopeResolution = (!(first instanceof Variable) || functionName.equals("forward_static_call"));
                                    replacement = String.format(
                                            "%s%s%s(%s)",
                                            suggestedCallable.getFirst(),
                                            useScopeResolution ? "::" : "->",
                                            suggestedCallable.getSecond(),
                                            Stream.of(Arrays.copyOfRange(arguments, 1, arguments.length)).map(this::argumentAsString).collect(Collectors.joining(", "))
                                    );
                                } else {
                                    replacement = String.format(
                                            "%s(%s)",
                                            suggestedCallable.getFirst(),
                                            Stream.of(Arrays.copyOfRange(arguments, 1, arguments.length)).map(this::argumentAsString).collect(Collectors.joining(", "))
                                    );
                                }
                                holder.registerProblem(
                                        reference,
                                        String.format(MessagesPresentationUtil.prefixWithEa(patternReplace), replacement),
                                        new ReplaceFix(replacement)
                                );
                            }
                            callable.clear();
                        }
                    }
                }
            }

            @NotNull
            private Couple<String> callable(@Nullable PsiElement first, @Nullable PsiElement second) {
                final String[] callable = {null, null};

                /* check second part: in some cases it overrides the first one completely */
                if (second != null) {
                    callable[1] =  '{' + second.getText() + '}';
                    if (second instanceof StringLiteralExpression) {
                        final StringLiteralExpression literal = (StringLiteralExpression) second;
                        if (literal.getFirstPsiChild() == null) {
                            final String content = PhpStringUtil.unescapeText(literal.getContents(), literal.isSingleQuote());
                            /* false-positives: relative invocation */
                            if (content.startsWith("parent::")) {
                                return Couple.of(null, null);
                            }
                            /* special case: the second overrides first one */
                            if (content.indexOf(':') != -1) {
                                return Couple.of(content, null);
                            }
                            /* regular behaviour cases */
                            callable[1] = content;
                        }
                    } else if (second instanceof Variable) {
                        callable[1] = second.getText();
                    }
                }

                /* the first part should be a variable or string literal without injections */
                if (first instanceof StringLiteralExpression) {
                    final StringLiteralExpression literal = (StringLiteralExpression) first;
                    if (literal.getFirstPsiChild() == null) {
                        callable[0] = PhpStringUtil.unescapeText(literal.getContents(), literal.isSingleQuote());
                    }
                } else if (first instanceof Variable) {
                    callable[0] = first.getText();
                }

                return Couple.of(callable[0], callable[1]);
            }

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
            private String argumentAsString(@NotNull PsiElement argument) {
                PsiElement previous = argument.getPrevSibling();
                if (previous instanceof PsiWhiteSpace) {
                    previous = previous.getPrevSibling();
                }

                if (OpenapiTypesUtil.is(previous, PhpTokenTypes.opBIT_AND)) {
                    return '&' + argument.getText();
                } else if (OpenapiTypesUtil.is(argument.getPrevSibling(), PhpTokenTypes.opVARIADIC)) {
                    return "..." + argument.getText();
                }

                return argument.getText();
            }
        };
    }

    private static final class ReplaceFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use variable function instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
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
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        InlineFix(@NotNull String expression) {
            super(expression);
        }
    }
}

