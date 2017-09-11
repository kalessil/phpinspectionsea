package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class InconsistentQueryBuildInspector extends BasePhpInspection {
    private static final String strProblemDescription = "'ksort(%a%, SORT_STRING)' should be used instead, " +
            "so http_build_query() produces result independent from key types.";

    @NotNull
    public String getShortName() {
        return "InconsistentQueryBuildInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final PsiElement[] parameters = reference.getParameters();
                final String function         = reference.getName();
                if (1 == parameters.length && !StringUtils.isEmpty(function) && function.equals("ksort")) {
                    // pre-condition satisfied, now check if http_build_query used in the scope

                    final Function scope = ExpressionSemanticUtil.getScope(reference);
                    if (null != scope) {
                        Collection<FunctionReference> calls = PsiTreeUtil.findChildrenOfType(scope, FunctionReference.class);
                        if (calls.size() > 0) {
                            for (final FunctionReference oneCall : calls) {
                                /* skip inspected call and calls without arguments */
                                if (
                                    oneCall == reference || !OpenapiTypesUtil.isFunctionReference(oneCall) ||
                                    oneCall.getParameters().length == 0
                                ) {
                                    continue;
                                }

                                /* skip non-target function */
                                final String currentFunction = oneCall.getName();
                                if (currentFunction == null || !currentFunction.equals("http_build_query")) {
                                    continue;
                                }

                                /* pattern match: ksort and http_build_query operating on the same expression */
                                if (PsiEquivalenceUtil.areElementsEquivalent(oneCall.getParameters()[0], parameters[0])) {
                                    final String message = strProblemDescription.replace("%a%", parameters[0].getText());
                                    holder.registerProblem(reference, message, new TheLocalFix());

                                    break;
                                }
                            }

                            calls.clear();
                        }
                    }
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Add SORT_STRING as a parameter";
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
                final FunctionReference call = (FunctionReference) expression;
                final PsiElement[] params    = call.getParameters();

                /* override existing parameters */
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, "ksort(null, SORT_STRING)");
                replacement.getParameters()[0].replace(params[0]);

                /* replace parameters list */
                //noinspection ConstantConditions I'm really sure NPE will not happen
                call.getParameterList().replace(replacement.getParameterList());
            }
        }
    }

}

