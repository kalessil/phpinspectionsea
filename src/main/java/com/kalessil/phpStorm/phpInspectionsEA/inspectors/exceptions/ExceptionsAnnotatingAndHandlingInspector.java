package com.kalessil.phpStorm.phpInspectionsEA.inspectors.exceptions;

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
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc.ThrowsResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpExceptions.CollectPossibleThrowsUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ExceptionsAnnotatingAndHandlingInspector extends BasePhpInspection {
    // Inspection options.
    public boolean REPORT_NON_THROWN_EXCEPTIONS = false;
    public final List<String> configuration     = new ArrayList<>();

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
            @Override
            public void visitPhpFinally(@NotNull Finally element) {
                PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (!phpVersion.hasFeature(PhpLanguageFeature.FINALLY)) {
                    return;
                }

                final HashSet<PsiElement> processedRegistry             = new HashSet<>();
                final HashMap<PhpClass, HashSet<PsiElement>> exceptions =
                        CollectPossibleThrowsUtil.collectNestedAndWorkflowExceptions(element, processedRegistry, holder);

                /* report individual statements */
                if (exceptions.size() > 0) {
                    final Set<PsiElement> reportedExpressions = new HashSet<>();
                    for (final Set<PsiElement> pool : exceptions.values()) {
                        pool.stream()
                                .filter(expression  -> !reportedExpressions.contains(expression))
                                .forEach(expression -> {
                                    holder.registerProblem(
                                            this.getReportingTarget(expression),
                                            messageFinallyExceptions,
                                            ProblemHighlightType.GENERIC_ERROR
                                    );
                                    reportedExpressions.add(expression);
                                });
                        pool.clear();
                    }
                    reportedExpressions.clear();
                    exceptions.clear();
                }

                /* report try-blocks */
                if (processedRegistry.size() > 0) {
                    processedRegistry.stream()
                        .filter(statement  -> statement instanceof Try).forEach(statement ->
                                holder.registerProblem(
                                statement.getFirstChild(),
                                messageFinallyExceptions,
                                ProblemHighlightType.GENERIC_ERROR
                        ));
                    processedRegistry.clear();
                }
            }

            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (method.isAbstract() || this.isTestContext(method)) {
                    return;
                }

                // __toString has magic methods validation, must not raise exceptions
                final PsiElement methodName = NamedElementUtil.getNameIdentifier(method);
                if (null == methodName || method.getName().equals("__toString")) {
                    return;
                }

                /* collect announced cases */
                final HashSet<PhpClass> annotatedExceptions = new HashSet<>();
                final boolean hasPhpDoc                     = method.getDocComment() != null;
                if (!ThrowsResolveUtil.resolveThrownExceptions(method, annotatedExceptions)) {
                    return;
                }

                HashSet<PsiElement> processedRegistry = new HashSet<>();
                HashMap<PhpClass, HashSet<PsiElement>> throwsExceptions =
                        CollectPossibleThrowsUtil.collectNestedAndWorkflowExceptions(method, processedRegistry, holder);
                processedRegistry.clear();

                /* exclude annotated exceptions, identify which has not been thrown */
                final Set<PhpClass> annotatedButNotThrownExceptions = new HashSet<>(annotatedExceptions);
                /* release bundled expressions *//* actualize un-thrown exceptions registry */
                annotatedExceptions.stream()
                        .filter(key        -> hasPhpDoc && throwsExceptions.containsKey(key))
                        .forEach(annotated -> {
                            /* release bundled expressions */
                            throwsExceptions.get(annotated).clear();
                            throwsExceptions.remove(annotated);
                            /* actualize un-thrown exceptions registry */
                            annotatedButNotThrownExceptions.remove(annotated);
                        });

                /* do reporting now: exceptions annotated, but not thrown */
                if (REPORT_NON_THROWN_EXCEPTIONS && annotatedButNotThrownExceptions.size() > 0) {
                    final List<String> toReport = annotatedButNotThrownExceptions.stream()
                            .map(PhpNamedElement::getFQN)
                            .collect(Collectors.toList());
                    holder.registerProblem(methodName, messagePatternUnthrown.replace("%c%", String.join(", ", toReport)));
                    toReport.clear();
                }
                annotatedButNotThrownExceptions.clear();

                /* do reporting now: exceptions thrown but not annotated */
                if (throwsExceptions.size() > 0) {
                    /* deeper analysis needed */
                    HashMap<PhpClass, HashSet<PsiElement>> unhandledExceptions = new HashMap<>();
                    if (!annotatedExceptions.isEmpty() && hasPhpDoc) {
                        /* filter what to report based on annotated exceptions  */
                        for (final PhpClass annotated : annotatedExceptions) {
                            for (final Map.Entry<PhpClass, HashSet<PsiElement>> throwsExceptionsPair: throwsExceptions.entrySet()) {
                                final PhpClass thrown = throwsExceptionsPair.getKey();
                                /* already reported */
                                if (unhandledExceptions.containsKey(thrown)) {
                                    continue;
                                }

                                /* check thrown parents, as annotated not processed here */
                                final HashSet<PhpClass> thrownVariants = InterfacesExtractUtil.getCrawlInheritanceTree(thrown, true);
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
                        for (final Map.Entry<PhpClass, HashSet<PsiElement>> unhandledExceptionsPair : unhandledExceptions.entrySet()) {
                            final String thrown                     = unhandledExceptionsPair.getKey().getFQN();
                            final Set<PsiElement> blamedExpressions = unhandledExceptionsPair.getValue();
                            if (!configuration.contains(thrown)) {
                                final String message = messagePattern.replace("%c%", thrown);
                                for (final PsiElement blame : blamedExpressions) {
                                    final LocalQuickFix fix = hasPhpDoc ? new MissingThrowAnnotationLocalFix(method, thrown) : null;
                                    holder.registerProblem(this.getReportingTarget(blame), message, fix);
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

            @NotNull
            private PsiElement getReportingTarget(@NotNull PsiElement expression) {
                PsiElement result = expression;
                if (expression instanceof FunctionReference) {
                    final PsiElement nameNode = (PsiElement) ((FunctionReference) expression).getNameNode();
                    if (nameNode != null) {
                        result = nameNode;
                    }
                } else if (expression instanceof PhpThrow) {
                    final PsiElement subject = ((PhpThrow) expression).getArgument();
                    if (subject instanceof NewExpression) {
                        final PsiElement reference = ((NewExpression) subject).getClassReference();
                        if (reference != null) {
                            result = reference;
                        }
                    }
                }
                return result;
            }
        };
    }

    private static class MissingThrowAnnotationLocalFix implements LocalQuickFix {
        private static final String title = "Declare exception via @throws";

        final private String exception;
        final private SmartPsiElementPointer<Method> method;

        MissingThrowAnnotationLocalFix(@NotNull Method method, @NotNull String exception){
            super();

            this.exception = exception;
            this.method    = SmartPointerManager.getInstance(method.getProject()).createSmartPsiElementPointer(method);
        }

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final Method method = (null == this.method ? null : this.method.getElement());
            if (null == method || project.isDisposed()) {
                return;
            }

            final PhpDocComment phpDoc = method.getDocComment();
            final String  pattern      =  this.exception;
            final String  patternPlace = "@throws " + pattern.replaceAll("\\\\", "\\\\\\\\");

            /* fix if PhpDoc exists and not fixed yet */
            if (phpDoc != null && !phpDoc.getText().contains(pattern)) {
                boolean injected = false;
                final List<String> newCommentLines = new ArrayList<>();
                for (final String line : phpDoc.getText().split("\\n")) {
                    /* injecting after return tag: probe 1 */
                    if (!injected && line.contains("@throws")) {
                        newCommentLines.add(line.replaceAll("@throws[^\\r\\n]*", patternPlace));
                        newCommentLines.add(line);

                        injected = true;
                        continue;
                    }

                    /* injecting at the end of PhpDoc: probe 3 */
                    if (!injected && line.contains("*/")) {
                        // no throw/return is declared
                        newCommentLines.add(line.replaceAll("/", patternPlace));
                        newCommentLines.add(line);

                        injected = true;
                        continue;
                    }

                    newCommentLines.add(line);
                }

                final String newCommentText = String.join("\n", newCommentLines);
                newCommentLines.clear();

                phpDoc.replace(PhpPsiElementFactory.createFromText(project, PhpDocComment.class, newCommentText));
            }
        }
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Report non-thrown exceptions", REPORT_NON_THROWN_EXCEPTIONS, (isSelected) -> REPORT_NON_THROWN_EXCEPTIONS = isSelected);
            component.addList("Ignored exceptions:", configuration, null, null, "Adding exception class...", "Examples: '\\RuntimeException' or '\\Namespace\\Exception'");
        });
    }
}
