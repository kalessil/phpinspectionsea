package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
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

public class EmptyClassInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Class does not contain any properties or methods";

    @NotNull
    public String getShortName() {
        return "EmptyClassInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                final String strClassFQN = clazz.getFQN();
                /* skip un-explorable and exception classes */
                if (StringUtil.isEmpty(strClassFQN) || strClassFQN.endsWith("Exception")) {
                    return;
                }

                /* require class with name which can be targeted by warning */
                if (clazz.isInterface() || clazz.isTrait() || null == clazz.getNameIdentifier()) {
                    return;
                }

                final boolean isEmpty = (0 == clazz.getOwnFields().length + clazz.getOwnMethods().length);
                if (isEmpty && 0 == clazz.getTraitUseRules().size() && !isDeprecated(clazz)) {
                    holder.registerProblem( clazz.getNameIdentifier(), strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                }
            }

            /**
             * Personally I deprecating empty classes before they can be dropped.
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
