package com.kalessil.phpStorm.phpInspectionsEA.inspectors.exceptionsWorkflow;


import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocCommentImpl;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Finally;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.Try;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc.ThrowsResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpExceptions.CollectPossibleThrowsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ExceptionsAnnotatingAndHandlingInspector extends BasePhpInspection {
    private static final String strProblemDescription       = "Throws a non-annotated/unhandled exception: '%c%'";
    private static final String strProblemFinallyExceptions = "Exceptions management inside finally has variety of side-effects in certain PHP versions";

    @NotNull
    public String getShortName() {
        return "ExceptionsAnnotatingAndHandlingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFinally(Finally element) {
                PhpLanguageLevel preferableLanguageLevel = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (!preferableLanguageLevel.hasFeature(PhpLanguageFeature.FINALLY)) {
                    return;
                }

                final HashSet<PsiElement> processedRegistry             = new HashSet<PsiElement>();
                final HashMap<PhpClass, HashSet<PsiElement>> exceptions =
                        CollectPossibleThrowsUtil.collectNestedAndWorkflowExceptions(element, processedRegistry, holder);

                /* report individual statements */
                if (exceptions.size() > 0) {
                    final HashSet<PsiElement> reportedExpressions = new HashSet<PsiElement>();
                    for (HashSet<PsiElement> pool : exceptions.values()) {
                        for (PsiElement expression : pool) {
                            if (!reportedExpressions.contains(expression)) {
                                holder.registerProblem(expression, strProblemFinallyExceptions, ProblemHighlightType.GENERIC_ERROR);
                                reportedExpressions.add(expression);
                            }
                        }
                        pool.clear();
                    }
                    reportedExpressions.clear();
                    exceptions.clear();
                }

                /* report try-blocks */
                if (processedRegistry.size() > 0) {
                    for (PsiElement statement : processedRegistry) {
                        if (statement instanceof Try) {
                            holder.registerProblem(statement.getFirstChild(), strProblemFinallyExceptions, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                    processedRegistry.clear();
                }
            }

            public void visitPhpMethod(Method method) {
                String strMethodName     = method.getName();
                PsiElement objMethodName = method.getNameIdentifier();
                if (
                    StringUtil.isEmpty(strMethodName) || null == objMethodName ||
                    strMethodName.equals("__toString") // __toString has magic methods validation, must not raise exceptions
                ) {
                    return;
                }

                PhpClass clazz = method.getContainingClass();
                if (null == clazz) {
                    return;
                }
                String strClassFQN = clazz.getFQN();
                /* skip un-explorable classes */
                if (StringUtil.isEmpty(strClassFQN)) {
                    return;
                }


                /* collect announced cases */
                final HashSet<PhpClass> annotatedExceptions = new HashSet<PhpClass>();
                if (ThrowsResolveUtil.ResolveType.NOT_RESOLVED == ThrowsResolveUtil.resolveThrownExceptions(method, annotatedExceptions)) {
                    return;
                }
                /* count PhpUnit assertion exceptions as always annotated, so we don't report them see #225 */
                final Collection<PhpClass> phpUnitAssertionErrorClasses =  PhpIndex.
                        getInstance(holder.getProject()).getClassesByFQN("\\PHPUnit_Framework_AssertionFailedError");
                if (phpUnitAssertionErrorClasses.size() > 0) {
                    annotatedExceptions.addAll(phpUnitAssertionErrorClasses);
                }
                /* collect announced cases with injected PhpUnit exceptions */


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
                            for (Map.Entry<PhpClass, HashSet<PsiElement>> throwsExceptionsPair: throwsExceptions.entrySet()) {
                                final PhpClass thrown = throwsExceptionsPair.getKey();
                                /* already reported */
                                if (unhandledExceptions.containsKey(thrown)) {
                                    continue;
                                }

                                /* check thrown parents, as annotated not processed here */
                                final HashSet<PhpClass> thrownVariants = InterfacesExtractUtil.getCrawlCompleteInheritanceTree(thrown, true);
                                if (!thrownVariants.contains(annotated)) {
                                    unhandledExceptions.put(thrown, throwsExceptionsPair.getValue());
                                    throwsExceptions.put(thrown, null);
                                }
                                thrownVariants.clear();
                            }
                        }
                    } else {
                        /* report all, as nothing is annotated */
                        for (Map.Entry<PhpClass, HashSet<PsiElement>> throwsExceptionsPair: throwsExceptions.entrySet()) {
                            final PhpClass thrown = throwsExceptionsPair.getKey();
                            /* already reported */
                            if (unhandledExceptions.containsKey(thrown)) {
                                continue;
                            }

                            unhandledExceptions.put(thrown, throwsExceptionsPair.getValue());
                            throwsExceptions.put(thrown, null);
                        }
                    }

                    if (unhandledExceptions.size() > 0) {
                        final boolean suggestQuickFix = null != method.getDocComment();

                        for (Map.Entry<PhpClass, HashSet<PsiElement>> unhandledExceptionsPair : unhandledExceptions.entrySet()) {
                            final String thrown  = unhandledExceptionsPair.getKey().getFQN();
                            final String message = strProblemDescription.replace("%c%", thrown);

                            final HashSet<PsiElement> blamedExpressions = unhandledExceptionsPair.getValue();
                            for (PsiElement blame : blamedExpressions) {
                                if (suggestQuickFix) {
                                    final MissingThrowAnnotationLocalFix fix = new MissingThrowAnnotationLocalFix(method, thrown);
                                    holder.registerProblem(blame, message, ProblemHighlightType.WEAK_WARNING, fix);
                                } else {
                                    holder.registerProblem(blame, message, ProblemHighlightType.WEAK_WARNING);
                                }
                            }
                            blamedExpressions.clear();
                        }
                        unhandledExceptions.clear();
                    }

                    throwsExceptions.clear();
                }

                annotatedExceptions.clear();
            }
        };
    }

    private static class MissingThrowAnnotationLocalFix implements LocalQuickFix {
        final private String exception;
        private SmartPsiElementPointer<Method> method;

        MissingThrowAnnotationLocalFix(@NotNull Method method, @NotNull String exception){
            super();

            this.exception = exception;
            this.method    = SmartPointerManager.getInstance(method.getProject()).createSmartPsiElementPointer(method);
        }

        @NotNull
        @Override
        public String getName() {
            return "Declare exception via @throws";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final Method method = (null == this.method ? null : this.method.getElement());
            if (null == method) {
                return;
            }

            final PhpDocComment phpDoc = method.getDocComment();
            final String  pattern      =  this.exception;
            final String  patternPlace = "@throws " + pattern.replaceAll("\\\\", "\\\\\\\\");

            /* fix if phpDoc exists and not fixed yet */
            if (null != phpDoc && !phpDoc.getText().contains(pattern)) {
                final String[] comment = phpDoc.getText().split("\\n");

                boolean isInjected = false;
                final LinkedList<String> newCommentLines = new LinkedList<String>();
                for (String line : comment) {
                    /* injecting after return tag: probe 1 */
                    if (!isInjected && line.contains("@return")) {
                        newCommentLines.add(line);
                        newCommentLines.add(line.replaceAll("@return[^\\r\\n]*", patternPlace));

                        isInjected = true;
                        continue;
                    }

                    /* injecting at the end of PhpDoc: probe 3 */
                    if (!isInjected && line.contains("*/")) {
                        // no throw/return is declared
                        newCommentLines.add(line.replaceAll("/", patternPlace));
                        newCommentLines.add(line);

                        isInjected = true;
                        continue;
                    }

                    newCommentLines.add(line);
                }

                final String newCommentText = StringUtils.join(newCommentLines, "\n");
                newCommentLines.clear();

                //noinspection ConstantConditions I' sure NPE will not happen as we get valid structure for input
                phpDoc.replace(PhpPsiElementFactory.createFromText(project, PhpDocCommentImpl.class, newCommentText));
            }

            /* release a tree node reference */
            this.method = null;
        }
    }
}
