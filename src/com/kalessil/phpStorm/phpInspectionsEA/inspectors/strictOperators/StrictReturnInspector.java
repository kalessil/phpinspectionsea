package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrictReturnInspector extends BasePhpInspection {
    private static final String strProblemDescriptionReturn = "Type of return value (%t1%) doesn't match declared type (%t2%).";

    @NotNull
    public String getShortName() {
        return "StrictReturnInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {

            public void visitPhpReturn(PhpReturn returnStatement) {
                final PsiElement value = returnStatement.getArgument();
                if (value == null) {
                    return;
                }

                final PsiElement func = getFunctionScope(returnStatement);
                if (!(func instanceof PhpTypedElement)) {
                    return;
                }

                final PhpExpressionTypes funcType = new PhpExpressionTypes(((PhpTypedElement) func).getType().toString(), holder, true);
                final PhpExpressionTypes returnType = new PhpExpressionTypes(value, holder);
                if (funcType.isMixed() || funcType.equals(returnType)) {
                    return;
                }

                final String strWarning = strProblemDescriptionReturn
                        .replace("%t1%", returnType.toString())
                        .replace("%t2%", funcType.toString());
                holder.registerProblem(value, strWarning, ProblemHighlightType.WEAK_WARNING);
            }

            private PsiElement getFunctionScope(final PhpReturn returnStatement) {
                PsiElement current = returnStatement;
                while (current != null) {
                    if (current instanceof PhpFile) {
                        break;
                    }
                    if ((current instanceof Function) || (current instanceof Method)) {
                        return current;
                    }
                    current = current.getParent();
                }
                return null;
            }
        };
    }
}
