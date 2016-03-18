package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.ImplementsList;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClassReImplementsParentInterfaceInspector extends BasePhpInspection {
    private static final String messagePattern = "%i% is already announced in %c%.";

    @NotNull
    public String getShortName() {
        return "ClassReImplementsParentInterfaceInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                /* skip classes which we cannot report */
                final PsiElement classNameNode = clazz.getNameIdentifier();
                if (null == classNameNode) {
                    return;
                }

                /* check if parent class and own implements are available */
                final List<ClassReference> listImplements = clazz.getImplementsList().getReferenceElements();
                if (listImplements.size() > 0) {
                    for (PhpClass objParentClass : clazz.getSupers()) {
                        /* do not process parent interfaces */
                        if (objParentClass.isInterface()) {
                            continue;
                        }

                        /* ensure implements some interfaces and discoverable properly */
                        final List<ClassReference> listImplementsParents = objParentClass.getImplementsList().getReferenceElements();
                        if (listImplementsParents.size() > 0) {
                            /* check interfaces of a parent class */
                            final String parentClassFQN = objParentClass.getFQN();
                            for (ClassReference parentInterfaceRef : listImplementsParents) {
                                /* ensure we have all identities valid, or skip analyzing */
                                final String parentInterfaceFQN  = parentInterfaceRef.getFQN();
                                final String parentInterfaceName = parentInterfaceRef.getName();
                                if (StringUtil.isEmpty(parentInterfaceFQN) || StringUtil.isEmpty(parentInterfaceName)) {
                                    continue;
                                }

                                /* match parents interfaces against class we checking */
                                for (ClassReference ownInterface : listImplements) {
                                    /* ensure FQNs matches */
                                    final String ownInterfaceFQN = ownInterface.getFQN();
                                    if (!StringUtil.isEmpty(ownInterfaceFQN) && ownInterfaceFQN.equals(parentInterfaceFQN)) {
                                        final String message = messagePattern
                                                .replace("%i%", parentInterfaceName)
                                                .replace("%c%", parentClassFQN);

                                        holder.registerProblem(ownInterface, message, ProblemHighlightType.LIKE_UNUSED_SYMBOL, new TheLocalFix());
                                        break;
                                    }
                                }
                            }

                            listImplementsParents.clear();
                        }
                    }

                    listImplements.clear();
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Remove unnecessary implements entry";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression.getParent() instanceof ImplementsList) {
                final ImplementsList implementsList = (ImplementsList) expression.getParent();
                if (1 == implementsList.getReferenceElements().size()) {
                    /* drop implements section completely; implementsList.delete() breaks further SCA */
                    expression.delete();
                    implementsList.getFirstChild().delete();
                } else {
                    final boolean cleanupLeftHand = implementsList.getReferenceElements().get(0) != expression;
                    PsiElement commaCandidate = cleanupLeftHand ? expression.getPrevSibling() : expression.getPrevSibling();
                    if (commaCandidate instanceof PsiWhiteSpace) {
                        commaCandidate = cleanupLeftHand ? commaCandidate.getPrevSibling() : commaCandidate.getPrevSibling();
                    }

                    /* drop single implements entry from the list */
                    expression.delete();
                    commaCandidate.delete();
                }
            }
        }
    }
}