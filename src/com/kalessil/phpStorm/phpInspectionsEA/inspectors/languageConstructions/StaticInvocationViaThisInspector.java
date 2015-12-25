package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StaticInvocationViaThisInspector extends BasePhpInspection {
    private static final String strProblemThisUsed = "'static::%m%(...)' or 'self::%m%(...)' should be used instead";
    private static final String strProblemExpressionUsed = "'%c%::%m%(...)' should be used instead";

    @NotNull
    public String getShortName() {
        return "StaticInvocationViaThisInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* Static fields will be simply not resolved properly, so we can not do checking for them */

            public void visitPhpMethodReference(MethodReference reference) {
                PsiReference objReference = reference.getReference();
                String methodName = reference.getName();
                if (null != objReference && !StringUtil.isEmpty(methodName)) {
                    PsiElement objResolvedRef = objReference.resolve();
                    /* resolved method is static but called with $ this*/
                    if (objResolvedRef instanceof Method && ((Method) objResolvedRef).isStatic()) {


                        /* check first pattern $this->static */
                        if (reference.getFirstChild().getText().equals("$this")) {
                            String message = strProblemThisUsed
                                    .replace("%m%", methodName)
                                    .replace("%m%", methodName);
                            holder.registerProblem(reference.getFirstChild(), message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                            return;
                        }

                        /* check second pattern <expression>->static */
                        PhpClass clazz = ((Method) objResolvedRef).getContainingClass();
                        PsiElement objectExpression = reference.getFirstPsiChild();
                        if (null != objectExpression && null != clazz) {
                            /* check operator */
                            PsiElement operator = objectExpression.getNextSibling();
                            if (operator instanceof PsiWhiteSpaceImpl) {
                                operator = operator.getNextSibling();
                            }
                            if (null == operator) {
                                return;
                            }

                            if (operator.getText().replaceAll("\\s+","").equals("->")) {
                                String message = strProblemExpressionUsed
                                        .replace("%m%", reference.getName())
                                        .replace("%c%", clazz.getName());
                                holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            }
                        }
                    }
                }
            }
        };
    }
}