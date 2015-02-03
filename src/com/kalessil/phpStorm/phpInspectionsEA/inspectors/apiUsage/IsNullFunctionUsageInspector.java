package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class IsNullFunctionUsageInspector extends BasePhpInspection {
    private static final String strProblemDescriptionIsNull  = "'null === ...' construction shall be used instead";
    private static final String strProblemDescriptionNotNull = "'null !== ...' construction shall be used instead";
    private static final String strIsNull = "is_null";

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String strFunctionName = reference.getName();
                if (
                    reference.getParameters().length != 1 ||
                    StringUtil.isEmpty(strFunctionName) ||
                    !strFunctionName.equals(strIsNull)
                ) {
                    return;
                }

                if (reference.getParent() instanceof UnaryExpression) {
                    UnaryExpression objParent = (UnaryExpression) reference.getParent();
                    if (null != objParent.getOperation() && objParent.getOperation().getNode().getElementType() == PhpTokenTypes.opNOT) {
                        holder.registerProblem(objParent, strProblemDescriptionNotNull, ProblemHighlightType.WEAK_WARNING);
                        return;
                    }
                }

                holder.registerProblem(reference, strProblemDescriptionIsNull, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}