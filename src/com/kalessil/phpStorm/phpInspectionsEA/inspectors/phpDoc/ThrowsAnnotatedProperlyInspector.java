package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpDoc;


import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocCommentImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocReturnTag;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public class ThrowsAnnotatedProperlyInspector extends BasePhpInspection {
    private static final String strProblemDescription = "@throw annotation is missing for %c%";

    @NotNull
    public String getShortName() {
        return "ThrowsAnnotatedProperlyInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                String strMethodName = method.getName();
                PsiElement objMethodName = method.getNameIdentifier();
                if (StringUtil.isEmpty(strMethodName) || null == objMethodName) {
                    return;
                }
                PhpPsiElement previous = method.getPrevPsiSibling();
                if (!(previous instanceof PhpDocCommentImpl)) {
                    return;
                }

                // inherit doc not yet supported
                Collection<PhpDocTagImpl> tags = PsiTreeUtil.findChildrenOfType(previous, PhpDocTagImpl.class);
                for (PhpDocTagImpl tag : tags) {
                    if (tag.getName().equals("@inheritdoc")) {
                        return;
                    }
                }
                tags.clear();

                // find all throw statements
                Collection<PhpThrow> throwStatements = PsiTreeUtil.findChildrenOfType(method, PhpThrow.class);
                if (throwStatements.size() > 0) {
                    HashSet<String> declared = new HashSet<String>();

                    // find all @throws and remember FQNs
                    Collection<PhpDocReturnTag> returns = PsiTreeUtil.findChildrenOfType(previous, PhpDocReturnTag.class);
                    for (PhpDocReturnTag returnOrThrow : returns) {
                        if (returnOrThrow.getName().equals("@throws") && returnOrThrow.getFirstPsiChild() instanceof PhpDocType) {
                            PhpDocType type = (PhpDocType) returnOrThrow.getFirstPsiChild();
                            declared.add(type.getFQN());
                            declared.add(type.getName());
                        }
                    }
                    returns.clear();

                    // process all throw new ... expressions
                    for (PhpThrow throwStatement : throwStatements) {
                        PhpPsiElement what = throwStatement.getFirstPsiChild();
                        if (what instanceof NewExpression) {
                            ClassReference whatsClass = ((NewExpression) what).getClassReference();
                            if (null != whatsClass && whatsClass.resolve() instanceof PhpClass) {
                                // match FQNs
                                PhpClass resolved = (PhpClass) whatsClass.resolve();
                                if (!declared.contains(resolved.getFQN()) && !declared.contains(resolved.getName())) {
                                    String strError = strProblemDescription.replace("%c%", resolved.getName());
                                    holder.registerProblem(objMethodName, strError, ProblemHighlightType.WEAK_WARNING);
                                }
                            }
                        }
                    }
                    throwStatements.clear();

                    declared.clear();
                }
            }
        };
    }
}