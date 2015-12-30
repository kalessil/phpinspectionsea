package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;

public class VariableFunctionsUsageInspector extends BasePhpInspection {
    private static final String strProblemDescription = "'%r%(...);' should be used instead";

    @NotNull
    public String getShortName() {
        return "VariableFunctionsUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* process `call();` expressions only */
                if (!(reference.getParent() instanceof StatementImpl)) {
                    return;
                }

                /* general requirements for calls */
                String function = reference.getName();
                PsiElement[] parameters = reference.getParameters();
                if (
                    0 == parameters.length || StringUtil.isEmpty(function) ||
                    !(function.equals("call_user_func") || function.equals("call_user_func_array"))
                ) {
                    return;
                }

                /* only `call_user_func_array(..., array(...))` needs to be checked */
                if (function.equals("call_user_func_array")) {
                    if (parameters[1] instanceof ArrayCreationExpression) {
                        holder.registerProblem(reference, "'call_user_func(...)' should be used instead", ProblemHighlightType.WEAK_WARNING);
                    }

                    return;
                }

                if (function.equals("call_user_func")) {
                    /* `callReturningCallable()(...)` is not possible -> syntax error */
                    if (parameters[0] instanceof FunctionReference) {
                        return;
                    }

                    if (parameters[0] instanceof ArrayCreationExpression) {
                        ArrayCreationExpression callable = (ArrayCreationExpression) parameters[0];

                        /* get array values */
                        LinkedList<PhpPsiElement> values = new LinkedList<PhpPsiElement>();
                        PhpPsiElement value = callable.getFirstPsiChild();
                        while (null != value) {
                            values.add(value.getFirstPsiChild());
                            value = value.getNextPsiSibling();
                        }

                        /* ensure we have 2 values array and first is not a callable reference */
                        if (
                            2 == values.size()
                            && null != values.get(0) && null != values.get(1)
                            && !(values.get(0) instanceof FunctionReference)
                        ) {
                            LinkedList<String> parametersToSuggest = new LinkedList<String>();
                            for (PsiElement parameter : Arrays.copyOfRange(parameters, 1, parameters.length)) {
                                parametersToSuggest.add(parameter.getText());
                            }

                            /* as usually personalization of messages is overcomplicated */
                            String message = "'%o%->{%m%}(%p%)' should be used instead"
                                .replace("%p%", StringUtil.join(parametersToSuggest, ", "));

                            final boolean isFirstString = values.get(0) instanceof StringLiteralExpression;
                            final boolean isSecondString = values.get(1) instanceof StringLiteralExpression;
                            message = message
                                .replace(
                                    isFirstString  ? "%o%->" : "%o%",
                                    isFirstString  ? ((StringLiteralExpression) values.get(0)).getContents() + "::" : values.get(0).getText()
                                )
                                .replace(
                                    isSecondString ? "{%m%}" : "%m%",
                                    isSecondString ? ((StringLiteralExpression) values.get(1)).getContents()        : values.get(1).getText()
                                );
                            ;
                            parametersToSuggest.clear();

                            holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING);
                        }
                    } else {
                        PhpLanguageLevel preferableLanguageLevel = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                        if (PhpLanguageLevel.PHP700 == preferableLanguageLevel) {
                            /* in PHP7+ it's absolutely safe to use variable functions */
                            LinkedList<String> parametersToSuggest = new LinkedList<String>();
                            for (PsiElement parameter : Arrays.copyOfRange(parameters, 1, parameters.length)) {
                                parametersToSuggest.add(parameter.getText());
                            }

                            String message = "'%c%(%p%)' should be used instead"
                                    .replace("%c%", parameters[0].getText())
                                    .replace("%p%", StringUtil.join(parametersToSuggest, ", "));
                            parametersToSuggest.clear();

                            holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING);
                        }
                    }
                }
            }
        };
    }
}

