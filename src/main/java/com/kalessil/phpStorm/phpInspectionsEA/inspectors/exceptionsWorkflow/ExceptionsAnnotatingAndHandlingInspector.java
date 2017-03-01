package com.kalessil.phpStorm.phpInspectionsEA.inspectors.exceptionsWorkflow;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Finally;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.Try;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FileSystemUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc.ThrowsResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpExceptions.CollectPossibleThrowsUtil;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

public class ExceptionsAnnotatingAndHandlingInspector extends BasePhpInspection {
    /* TODO: add settings for FQNs which doesn't need to be reported */
    // configuration flags automatically saved by IDE
    @SuppressWarnings("WeakerAccess")
    public boolean REPORT_NON_THROWN_EXCEPTIONS = false;

    private static final String messagePattern           = "Throws a non-annotated/unhandled exception: '%c%'.";
    private static final String messagePatternUnthrown   = "Following exceptions annotated, but not thrown: '%c%'.";
    private static final String messageFinallyExceptions = "Exceptions management inside finally has a variety of side-effects in certain PHP versions.";

    @NotNull
    public String getShortName() {
        return "ExceptionsAnnotatingAndHandlingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFinally(Finally element) {
                PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (!phpVersion.hasFeature(PhpLanguageFeature.FINALLY)) {
                    return;
                }

                final HashSet<PsiElement> processedRegistry             = new HashSet<>();
                final HashMap<PhpClass, HashSet<PsiElement>> exceptions =
                        CollectPossibleThrowsUtil.collectNestedAndWorkflowExceptions(element, processedRegistry, holder);

                /* report individual statements */
                if (exceptions.size() > 0) {
                    final HashSet<PsiElement> reportedExpressions = new HashSet<>();
                    for (HashSet<PsiElement> pool : exceptions.values()) {
                        for (PsiElement expression : pool) {
                            if (!reportedExpressions.contains(expression)) {
                                holder.registerProblem(expression, messageFinallyExceptions, ProblemHighlightType.GENERIC_ERROR);
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
                            holder.registerProblem(statement.getFirstChild(), messageFinallyExceptions, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                    processedRegistry.clear();
                }
            }

            public void visitPhpMethod(Method method) {
                final PhpClass clazz = method.getContainingClass();
                if (null == clazz || method.isAbstract() || FileSystemUtil.isTestClass(clazz)) {
                    return;
                }

                // __toString has magic methods validation, must not raise exceptions
                final PsiElement objMethodName = NamedElementUtil.getNameIdentifier(method);
                if (null == objMethodName || method.getName().equals("__toString")) {
                    return;
                }

                /* collect announced cases */
                final HashSet<PhpClass> annotatedExceptions = new HashSet<>();
                if (ThrowsResolveUtil.ResolveType.NOT_RESOLVED == ThrowsResolveUtil.resolveThrownExceptions(method, annotatedExceptions)) {
                    return;
                }

                HashSet<PsiElement> processedRegistry = new HashSet<>();
                HashMap<PhpClass, HashSet<PsiElement>> throwsExceptions =
                        CollectPossibleThrowsUtil.collectNestedAndWorkflowExceptions(method, processedRegistry, holder);
                processedRegistry.clear();


                /* exclude annotated exceptions, identify which has not been thrown */
                final Set<PhpClass> annotatedButNotThrownExceptions = new HashSet<>();
                annotatedButNotThrownExceptions.addAll(annotatedExceptions);
                for (PhpClass annotated: annotatedExceptions) {
                    if (throwsExceptions.containsKey(annotated)) {
                        /* release bundled expressions */
                        throwsExceptions.get(annotated).clear();
                        throwsExceptions.remove(annotated);
                        /* actualize un-thrown exceptions registry */
                        annotatedButNotThrownExceptions.remove(annotated);
                    }
                }


                /* do reporting now: exceptions annotated, but not thrown */
                if (REPORT_NON_THROWN_EXCEPTIONS && annotatedButNotThrownExceptions.size() > 0) {
                    List<String> toReport = new ArrayList<>();
                    for (PhpClass notThrown : annotatedButNotThrownExceptions) {
                        toReport.add(notThrown.getFQN());
                    }

                    final String message = messagePatternUnthrown.replace("%c%", String.join(", ", toReport));
                    holder.registerProblem(objMethodName, message, ProblemHighlightType.WEAK_WARNING);

                    toReport.clear();
                }
                annotatedButNotThrownExceptions.clear();


                /* do reporting now: exceptions thrown but not annotated */
                if (throwsExceptions.size() > 0) {
                    /* deeper analysis needed */
                    HashMap<PhpClass, HashSet<PsiElement>> unhandledExceptions = new HashMap<>();
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
                            final String message = messagePattern.replace("%c%", thrown);

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
                final LinkedList<String> newCommentLines = new LinkedList<>();
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

                final String newCommentText = String.join("\n", newCommentLines);
                newCommentLines.clear();

                //noinspection ConstantConditions I' sure NPE will not happen as we get valid structure for input
                phpDoc.replace(PhpPsiElementFactory.createFromText(project, PhpDocComment.class, newCommentText));
            }

            /* release a tree node reference */
            this.method = null;
        }
    }

    public JComponent createOptionsPanel() {
        return (new ExceptionsAnnotatingAndHandlingInspector.OptionsPanel()).getComponent();
    }

    public class OptionsPanel {
        final private JPanel optionsPanel;

        final private JCheckBox importClassesAutomatically;

        public OptionsPanel() {
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new MigLayout());

            importClassesAutomatically = new JCheckBox("Report non-thrown exceptions", REPORT_NON_THROWN_EXCEPTIONS);
            importClassesAutomatically.addChangeListener(e -> REPORT_NON_THROWN_EXCEPTIONS = importClassesAutomatically.isSelected());
            optionsPanel.add(importClassesAutomatically, "wrap");
        }

        public JPanel getComponent() {
            return optionsPanel;
        }
    }

}
