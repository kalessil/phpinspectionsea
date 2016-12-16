package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VariableFunctionsUsageInspector extends BasePhpInspection {
    private static final String patternInlineArgs = "'call_user_func(%c%, %p%)' should be used instead (enables further analysis)";
    private static final String patternReplace    = "'%e' should be used instead";

    @NotNull
    public String getShortName() {
        return "VariableFunctionsUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* general requirements for calls */
                final String function         = reference.getName();
                final PsiElement[] parameters = reference.getParameters();
                if (0 == parameters.length || StringUtil.isEmpty(function) || !function.startsWith("call_user_func")) {
                    return;
                }

                /* only `call_user_func_array(..., array(...))` needs to be checked */
                if (2 == parameters.length && function.equals("call_user_func_array")) {
                    if (parameters[1] instanceof ArrayCreationExpression) {
                        final List<String> parametersUsed = new ArrayList<>();
                        for (PsiElement item : parameters[1].getChildren()) {
                            if (item instanceof PhpPsiElement) {
                                final PhpPsiElement itemValue = ((PhpPsiElement) item).getFirstPsiChild();
                                if (null != itemValue) {
                                    parametersUsed.add(itemValue.getText());
                                }
                            }
                        }

                        final String message = patternInlineArgs
                                .replace("%c%", parameters[0].getText())
                                .replace("%p%", String.join(", ", parametersUsed));
                        parametersUsed.clear();

                        holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING);
                    }
                    return;
                }


                /* TODO: `callReturningCallable()(...)` syntax not yet supported, re-evaluate */
                /* TODO: search is_callable in the scope or report probable bugs */
                if (function.equals("call_user_func")) {
                    /* collect callable parts */
                    final PsiElement firstParam          = parameters[0];
                    final List<PsiElement> callableParts = new ArrayList<>();
                    if (firstParam instanceof ArrayCreationExpression) {
                        /* extract parts */
                        PhpPsiElement firstPart  = ((ArrayCreationExpression) firstParam).getFirstPsiChild();
                        PhpPsiElement secondPart = null == firstPart ? null : firstPart.getNextPsiSibling();

                        /* now expression themselves */
                        firstPart  = null == firstPart  ? null : firstPart.getFirstPsiChild();
                        secondPart = null == secondPart ? null : secondPart.getFirstPsiChild();
                        if (null == firstPart || null == secondPart) {
                            return;
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
                        secondAsString = secondPart instanceof Variable ? secondPart.getText() : "{" + secondPart.getText() + "}";
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
                            if (null == firstPartExpression.getFirstPsiChild()) {
                                firstAsString = firstPartExpression.getContents();
                            }
                        }
                        if (firstPart instanceof Variable) {
                            firstAsString = firstPart.getText();
                        }
                    }
                    if (null == firstAsString) {
                        return;
                    }


                    final List<String> parametersToSuggest = new ArrayList<>();
                    for (PsiElement parameter : Arrays.copyOfRange(parameters, 1, parameters.length)) {
                        parametersToSuggest.add(parameter.getText());
                    }
                    final String expression = "%f%%o%%s%(%p%)"
                            .replace("%o%%s%", null == secondPart ? "" : "%o%%s%")
                            .replace("%o%", firstPart instanceof Variable ? "->" : "::")
                            .replace("%s%", null == secondPart            ? ""   : secondAsString)
                            .replace("%f%", firstAsString)
                            .replace("%p%", String.join(", ", parametersToSuggest));
                    parametersToSuggest.clear();

                    final String message = patternReplace.replace("%e%", expression);
                    holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(expression));
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private String replacement;

        TheLocalFix(@NotNull String replacement) {
            super();
            this.replacement = replacement;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use suggested replacement";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference) {
                PsiElement replacement = PhpPsiElementFactory.createFromText(expression.getProject(), FunctionReference.class, this.replacement);
                if (null != replacement) {
                    expression.replace(replacement);
                }
            }
        }
    }

}

