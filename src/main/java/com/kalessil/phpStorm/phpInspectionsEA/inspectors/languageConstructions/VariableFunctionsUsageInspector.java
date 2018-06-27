package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
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

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class VariableFunctionsUsageInspector extends BasePhpInspection {
    private static final String patternInlineArgs = "'%e%' should be used instead (enables further analysis).";
    private static final String patternReplace    = "'%e%' should be used instead.";

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
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length == 0) {
                    return;
                }

                /* only `call_user_func_array(..., array(...))` needs to be checked */
                if (arguments.length == 2 && functionName.equals("call_user_func_array")) {
                    if (arguments[1] instanceof ArrayCreationExpression) {
                        final List<String> parametersUsed = new ArrayList<>();
                        for (final PsiElement argument : arguments[1].getChildren()) {
                            if (argument instanceof PhpPsiElement) {
                                final PhpPsiElement itemValue = ((PhpPsiElement) argument).getFirstPsiChild();
                                final String itemValueString  = itemValue == null ? null : parameterAsString(itemValue);
                                if (itemValueString != null) {
                                    /* false-positives: call_user_func does not support arguments by reference */
                                    if (itemValueString.startsWith("&")) {
                                        parametersUsed.clear();
                                        return;
                                    }
                                    parametersUsed.add(itemValueString);
                                }
                            }
                        }

                        final String replacement = String.format("call_user_func(%s, %s)", arguments[0].getText(), String.join(", ", parametersUsed));
                        final String message     = patternInlineArgs.replace("%e%", replacement);
                        holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, new InlineFix(replacement));
                        parametersUsed.clear();
                    }
                    return;
                }


                /* TODO: `callReturningCallable()(...)` syntax not yet supported, re-evaluate */
                if (functionName.equals("call_user_func")) {
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
                    PsiElement firstPart  = callableParts.size() > 0 ? callableParts.get(0) : null;
                    PsiElement secondPart = callableParts.size() > 1 ? callableParts.get(1) : null;
                    callableParts.clear();


                    /* check second part: in some cases it overrides the first one completely */
                    String secondAsString = null;
                    if (null != secondPart) {
                        secondAsString = secondPart instanceof Variable ? secondPart.getText() : '{' + secondPart.getText() + '}';
                        if (secondPart instanceof StringLiteralExpression) {
                            final StringLiteralExpression secondPartExpression = (StringLiteralExpression) secondPart;
                            if (null == secondPartExpression.getFirstPsiChild()) {
                                final String content = secondPartExpression.getContents();
                                secondAsString       = content;

                                if (-1 != content.indexOf(':')) {
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
                    if (null == secondPart && firstPart instanceof Variable) {
                        PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                        if (PhpLanguageLevel.PHP530 == php) {
                            return;
                        }
                    }


                    final List<String> parametersToSuggest = new ArrayList<>();
                    for (PsiElement parameter : Arrays.copyOfRange(arguments, 1, arguments.length)) {
                        parametersToSuggest.add(parameterAsString(parameter));
                    }
                    final String replacement = "%f%%o%%s%(%p%)"
                        .replace("%o%%s%", null == secondPart ? "" : "%o%%s%")
                        .replace("%p%", String.join(", ", parametersToSuggest))
                        .replace("%s%", null == secondPart            ? ""   : secondAsString)
                        .replace("%o%", firstPart instanceof Variable ? "->" : "::")
                        .replace("%f%", firstAsString);
                    parametersToSuggest.clear();

                    final String message = patternReplace.replace("%e%", replacement);
                    holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, new ReplaceFix(replacement));
                }
            }
        };
    }

    @NotNull
    private String parameterAsString(@NotNull PsiElement parameter) {
        String asString     = parameter.getText();
        PsiElement previous = parameter.getPrevSibling();
        if (previous instanceof PsiWhiteSpace) {
            previous = previous.getPrevSibling();
        }
        if (OpenapiTypesUtil.is(previous, PhpTokenTypes.opBIT_AND)) {
            asString = '&' + asString;
        }

        return asString;
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

