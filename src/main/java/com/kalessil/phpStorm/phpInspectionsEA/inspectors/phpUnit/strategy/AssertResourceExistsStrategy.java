package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AssertResourceExistsStrategy {
    private final static String messagePattern = "%s should be used instead.";

    private final static Map<String, String> assertionMapping = new HashMap<>();
    static {
        assertionMapping.put("file_exists", "assertFileExists");
        assertionMapping.put("is_dir",      "assertDirectoryExists");
    }

    static public boolean apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] assertionArguments = reference.getParameters();
        if (assertionArguments.length > 0 && (function.equals("assertTrue") || function.equals("assertNotFalse"))) {
            final PsiElement candidate = ExpressionSemanticUtil.getExpressionTroughParenthesis(assertionArguments[0]);
            if (OpenapiTypesUtil.isFunctionReference(candidate)) {
                final FunctionReference functionCall = (FunctionReference) candidate;
                final PsiElement[] functionArguments = functionCall.getParameters();
                final String functionName            = functionCall.getName();
                if (functionArguments.length == 1 && functionName != null && assertionMapping.containsKey(functionName)) {
                    final String suggestedAssertion = assertionMapping.get(functionName);
                    final String message            = String.format(messagePattern, suggestedAssertion);
                    holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(suggestedAssertion, functionArguments[0]));
                    return true;
                }
            }
        }
        return false;
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private SmartPsiElementPointer<PsiElement> resource;
        final private String suggestedAssertion;

        TheLocalFix(@NotNull String suggestedAssertion, @NotNull PsiElement resource) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(resource.getProject());

            this.resource           = factory.createSmartPsiElementPointer(resource);
            this.suggestedAssertion = suggestedAssertion;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use suggested assertion";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            final PsiElement resource   = this.resource.getElement();
            if (resource != null && expression instanceof FunctionReference && !project.isDisposed()) {
                final FunctionReference currentAssertion = (FunctionReference) expression;
                final PsiElement[] arguments             = currentAssertion.getParameters();
                final boolean hasCustomMessage           = arguments.length == 2;

                final String pattern                    = hasCustomMessage ? "pattern(null, null)" : "pattern(null)";
                final FunctionReference replacement     = PhpPsiElementFactory.createFunctionReference(project, pattern);
                final PsiElement[] replacementArguments = replacement.getParameters();
                replacementArguments[0].replace(resource);
                if (hasCustomMessage) {
                    replacementArguments[1].replace(arguments[1]);
                }

                final PsiElement socket  = currentAssertion.getParameterList();
                final PsiElement implant = replacement.getParameterList();
                if (socket != null && implant != null) {
                    socket.replace(implant);
                    currentAssertion.handleElementRename(this.suggestedAssertion);
                }
            }
        }
    }
}
