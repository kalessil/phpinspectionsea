package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class FopenBinaryUnsafeUsageInspector extends BasePhpInspection {
    private static final String messageUseBinaryMode         = "The mode is not binary-safe ('b' is missing)";
    private static final String messageReplaceWithBinaryMode = "The mode is not binary-safe (replace 't' with 'b')";

    @NotNull
    public String getShortName() {
        return "FopenBinaryUnsafeUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* verify expected structure */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (params.length < 2 || StringUtil.isEmpty(functionName) || !functionName.equals("fopen")) {
                    return;
                }

                /* verify if mode provided and has no 'b' already */
                final StringLiteralExpression mode = ExpressionSemanticUtil.resolveAsStringLiteral(params[1]);
                if (null == mode) {
                    return;
                }
                final String modeText = mode.getContents();
                if (StringUtil.isEmpty(modeText) || modeText.indexOf('b') != -1) {
                    return;
                }

                if (modeText.indexOf('t') != -1) {
                    holder.registerProblem(params[1], messageReplaceWithBinaryMode, ProblemHighlightType.GENERIC_ERROR);
                    return;
                }
                holder.registerProblem(params[1], messageUseBinaryMode, ProblemHighlightType.GENERIC_ERROR);
            }
        };
    }
}
