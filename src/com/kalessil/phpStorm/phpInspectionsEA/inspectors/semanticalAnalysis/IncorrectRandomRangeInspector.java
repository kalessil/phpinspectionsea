package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.impl.PhpExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.UnaryExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class IncorrectRandomRangeInspector extends BasePhpInspection {
    private static final String strProblemDescription = "The range is not defined properly";

    private static HashSet<String> functions = null;
    private static HashSet<String> getFunctions() {
        if (null == functions) {
            functions = new HashSet<String>();

            functions.add("mt_rand");
            functions.add("random_int");
            functions.add("rand");
        }

        return functions;
    }

    @NotNull
    public String getShortName() {
        return "IncorrectRandomRangeInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check call structure */
                final PsiElement[] params = reference.getParameters();
                final String name         = reference.getName();
                if (2 != params.length || StringUtil.isEmpty(name) || !getFunctions().contains(name)) {
                    return;
                }

                if (
                    params[1] instanceof PhpExpressionImpl && !(params[1] instanceof UnaryExpressionImpl) &&
                    params[0] instanceof PhpExpressionImpl && !(params[0] instanceof UnaryExpressionImpl)
                ) {
                    try {
                        if (Integer.parseInt(params[1].getText()) < Integer.parseInt(params[0].getText())) {
                            holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.GENERIC_ERROR);
                        }
                    } catch (NumberFormatException wrongFormat) {
                        //noinspection UnnecessaryReturnStatement
                        return;
                    }
                }
            }
        };
    }
}