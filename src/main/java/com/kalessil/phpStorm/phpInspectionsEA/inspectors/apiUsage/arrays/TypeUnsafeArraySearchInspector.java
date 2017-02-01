package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class TypeUnsafeArraySearchInspector extends BasePhpInspection {
    private static final String message = "Third parameter should be provided to clarify if types safety important in this context.";

    @NotNull
    public String getShortName() {
        return "TypeUnsafeArraySearchInspection";
    }

    private static final Set<String> functions = new HashSet<>();
    static {
        functions.add("array_search");
        functions.add("in_array");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (2 != params.length || null == functionName || !functions.contains(functionName)) {
                    return;
                }

                /* invoke types inspection: perhaps they are correct */
                if (params[0] instanceof PhpTypedElement && params[1] instanceof PhpTypedElement) {
                    final Set<String> needleType    = ((PhpTypedElement) params[0]).getType().filterUnknown().getTypes();
                    final Set<String> containerType = ((PhpTypedElement) params[1]).getType().filterUnknown().getTypes();
                    if (1 == needleType.size() && 1 == containerType.size()) {
                        String neededType = needleType.iterator().next();
                        if (!neededType.isEmpty()) {
                            neededType = ('\\' != neededType.charAt(0) ? '\\' + neededType : neededType) + "[]";
                            if (neededType.equals(containerType.iterator().next())) {
                                return;
                            }
                        }
                    }
                }

                holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Add true as the third argument";
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
                final FunctionReference replacement
                    = PhpPsiElementFactory.createFromText(project, FunctionReference.class, "f(null, null, true)");
                if (null != replacement) {
                    final PsiElement[] replacementParams = replacement.getParameters();
                    final PsiElement[] originalParams    = ((FunctionReference) expression).getParameters();

                    replacementParams[0].replace(originalParams[0]);
                    replacementParams[1].replace(originalParams[1]);
                    //noinspection ConstantConditions - we are dealng with finished structures here
                    ((FunctionReference) expression).getParameterList().replace(replacement.getParameterList());
                }
            }
        }
    }
}
