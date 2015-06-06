package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpDoc;


import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc.ThrowsResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpExceptions.CollectPossibleThrowsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class ThrowsAnnotatedProperlyInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Some of exceptions are not handled/annotated: '%c%'";

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

                PhpClass clazz = method.getContainingClass();
                if (null == clazz) {
                    return;
                }
                String strClassFQN = clazz.getFQN();
                /* skip un-explorable and test classes */
                if (
                    StringUtil.isEmpty(strClassFQN) ||
                    strClassFQN.contains("\\Tests\\") || strClassFQN.contains("\\Test\\") ||
                    strClassFQN.endsWith("Test")
                ) {
                    return;
                }

                HashSet<PhpClass> annotatedExceptions = new HashSet<PhpClass>();
                if (ThrowsResolveUtil.ResolveType.NOT_RESOLVED == ThrowsResolveUtil.resolveThrownExceptions(method, annotatedExceptions)) {
                    return;
                }

                HashSet<PsiElement> processedRegistry = new HashSet<PsiElement>();
                HashSet<PhpClass> throwsExceptions = CollectPossibleThrowsUtil.collectNestedAndWorkflowExceptions(method, processedRegistry, holder);
//holder.registerProblem(objMethodName, "Processed: " + processedRegistry.size(), ProblemHighlightType.WEAK_WARNING);
                processedRegistry.clear();

                /* exclude annotated exceptions */
                throwsExceptions.removeAll(annotatedExceptions);
                if (throwsExceptions.size() > 0) {
                    /* deeper analysis needed */
                    HashSet<PhpClass> unhandledExceptions = new HashSet<PhpClass>();
                    for (PhpClass annotated : annotatedExceptions) {
                        for (PhpClass thrown : throwsExceptions) {
                            /* already reported */
                            if (unhandledExceptions.contains(thrown)) {
                                continue;
                            }

                            /* check thrown parents, as annotated not processed here */
                            HashSet<PhpClass> thrownVariants = InterfacesExtractUtil.getCrawlCompleteInheritanceTree(thrown, true);
                            if (!thrownVariants.contains(annotated)) {
                                unhandledExceptions.add(thrown);
                            }
                        }
                    }

                    if (unhandledExceptions.size() > 0) {
                        String strUnhandled = "";

                        String strDelimiter = "";
                        for (PhpClass classUnhandled : unhandledExceptions) {
                            strUnhandled += strDelimiter + classUnhandled.getFQN();
                            strDelimiter = ", ";
                        }
                        unhandledExceptions.clear();

                        String strError = strProblemDescription.replace("%c%", strUnhandled);
                        holder.registerProblem(objMethodName, strError, ProblemHighlightType.WEAK_WARNING);
                    }

                    throwsExceptions.clear();
                }

                annotatedExceptions.clear();
            }
        };
    }
}