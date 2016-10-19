package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocCommentImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class LongInheritanceChainInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Class has 3 or more parent classes, consider using appropriate design patterns.";

    @NotNull
    public String getShortName() {
        return "LongInheritanceChainInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                final String strClassFQN = clazz.getFQN();
                /* skip un-explorable classes */
                if (StringUtil.isEmpty(strClassFQN) || strClassFQN.endsWith("Exception")) {
                    return;
                }

                final PsiElement psiClassName = clazz.getNameIdentifier();
                if (null != psiClassName) {
                    int intParentsCount = 0;

                    /* count parents */
                    PhpClass classToCheck = clazz;
                    while (null != classToCheck.getSuperClass() && clazz != classToCheck.getSuperClass()) {
                        classToCheck = classToCheck.getSuperClass();
                        ++intParentsCount;
                    }

                    if (intParentsCount >= 3 && !isDeprecated(clazz)) {
                        holder.registerProblem(psiClassName, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }

            /**
             * TODO: code duplicate of com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor#isDeprecated
             * Personally I deprecating such classes before they can be dropped.
             */
            private boolean isDeprecated(PhpClass clazz) {
                PhpPsiElement classDoc = clazz.getPrevPsiSibling();
                if (!(classDoc instanceof PhpDocCommentImpl)) {
                    return false;
                }

                /* try to find @deprecated */
                Collection<PhpDocTagImpl> tags = PsiTreeUtil.findChildrenOfType(classDoc, PhpDocTagImpl.class);
                if (tags.size() > 0) {
                    for (PhpDocTagImpl subject : tags) {
                        if (subject.getName().equals("@deprecated")) {
                            tags.clear();
                            return true;
                        }
                    }
                    tags.clear();
                }

                return false;
            }
        };
    }
}