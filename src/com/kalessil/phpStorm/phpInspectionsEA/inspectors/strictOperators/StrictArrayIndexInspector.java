package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrictArrayIndexInspector extends BasePhpInspection {
    private static final String strProblemDescriptionArrayIndex = "Array key should be either integer or string (not %t%).";
    private static final String strProblemDescriptionStringIndex = "String offset should be integer (not %t%).";

    @NotNull
    public String getShortName() {
        return "StrictArrayIndexInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpArrayIndex(final ArrayIndex index) {
                final PhpPsiElement key = index.getValue();
                if (!(key instanceof PhpExpression)) {
                    return;
                }

                final PsiElement parent = index.getParent();
                if (parent instanceof ArrayAccessExpression) {
                    final PhpExpressionTypes type = new PhpExpressionTypes(((ArrayAccessExpression) parent).getValue(), holder);
                    if (type.isArrayAccess()) {
                        return;
                    }
                    if (type.isString() && !type.isArray()) {
                        inspectStringIndex((PhpExpression) key);
                        return;
                    }
                }

                inspectArrayIndex((PhpExpression) key);
            }

            public void visitPhpExpression(final PhpExpression expr) {
                if (expr instanceof ArrayHashElement) {
                    final PhpPsiElement key = ((ArrayHashElement) expr).getKey();
                    if (key instanceof PhpExpression) {
                        inspectArrayIndex((PhpExpression) key);
                    }
                }
            }

            private void inspectArrayIndex(final PhpExpression key) {
                final PhpExpressionTypes type = new PhpExpressionTypes(key, holder);
                if (type.isInt() || type.isString()) {
                    return;
                }

                final String strWarning = strProblemDescriptionArrayIndex
                        .replace("%t%", type.toString());
                holder.registerProblem(key, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private void inspectStringIndex(final PhpExpression key) {
                final PhpExpressionTypes type = new PhpExpressionTypes(key, holder);
                if (type.isInt()) {
                    return;
                }

                final String strWarning = strProblemDescriptionStringIndex
                        .replace("%t%", type.toString());
                holder.registerProblem(key, strWarning, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
