package com.kalessil.phpStorm.phpInspectionsEA.inspectors.exceptionsWorkflow;


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

import java.util.HashMap;
import java.util.HashSet;

public class ExceptionsAnnotatingAndHandlingInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Throws a non-annotated/unhandled exception: '%c%'";

    @NotNull
    public String getShortName() {
        return "ExceptionsAnnotatingAndHandlingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                String strMethodName = method.getName();
                PsiElement objMethodName = method.getNameIdentifier();
                if (StringUtil.isEmpty(strMethodName) || null == objMethodName || strMethodName.equals("__toString")) {
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
                HashMap<PhpClass, HashSet<PsiElement>> throwsExceptions =
                        CollectPossibleThrowsUtil.collectNestedAndWorkflowExceptions(method, processedRegistry, holder);
//holder.registerProblem(objMethodName, "Processed: " + processedRegistry.size(), ProblemHighlightType.WEAK_WARNING);
                processedRegistry.clear();


                /* exclude annotated exceptions */
                for (PhpClass annotated: annotatedExceptions) {
                    if (throwsExceptions.containsKey(annotated)) {
                        throwsExceptions.get(annotated).clear();
                        throwsExceptions.remove(annotated);
                    }
                }

                /* do reporting now */
                if (throwsExceptions.size() > 0) {
//holder.registerProblem(objMethodName, "Throws: " + throwsExceptions.keySet().toString(), ProblemHighlightType.WEAK_WARNING);
                    /* deeper analysis needed */
                    HashMap<PhpClass, HashSet<PsiElement>> unhandledExceptions = new HashMap<PhpClass, HashSet<PsiElement>>();
                    if (annotatedExceptions.size() > 0) {
                        /* filter what to report based on annotated exceptions  */
                        for (PhpClass annotated : annotatedExceptions) {
                            for (PhpClass thrown : throwsExceptions.keySet()) {
                                /* already reported */
                                if (unhandledExceptions.containsKey(thrown)) {
                                    continue;
                                }

                                /* check thrown parents, as annotated not processed here */
                                HashSet<PhpClass> thrownVariants = InterfacesExtractUtil.getCrawlCompleteInheritanceTree(thrown, true);
                                if (!thrownVariants.contains(annotated)) {
                                    unhandledExceptions.put(thrown, throwsExceptions.get(thrown));
                                    throwsExceptions.put(thrown, null);
                                }
                                thrownVariants.clear();
                            }
                        }
                    } else {
                        /* report all, as nothing is annotated */
                        for (PhpClass thrown : throwsExceptions.keySet()) {
                            /* already reported */
                            if (unhandledExceptions.containsKey(thrown)) {
                                continue;
                            }

                            unhandledExceptions.put(thrown, throwsExceptions.get(thrown));
                            throwsExceptions.put(thrown, null);
                        }
                    }

                    if (unhandledExceptions.size() > 0) {
                        for (PhpClass classUnhandled : unhandledExceptions.keySet()) {
                            String thrown = classUnhandled.getFQN();
                            String strError = strProblemDescription.replace("%c%", thrown);

                            for (PsiElement blame : unhandledExceptions.get(classUnhandled)) {
                                holder.registerProblem(blame, strError, ProblemHighlightType.WEAK_WARNING);
                            }

                            unhandledExceptions.get(classUnhandled).clear();
                        }
                        unhandledExceptions.clear();
                    }

                    throwsExceptions.clear();
                }

                annotatedExceptions.clear();
            }
        };
    }
}
