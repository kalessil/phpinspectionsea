package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassConstantCanBeUsedInspector extends BasePhpInspection {
    // configuration flags automatically saved by IDE
    @SuppressWarnings("WeakerAccess")
    public boolean IMPORT_CLASSES_ON_QF = true;
    @SuppressWarnings("WeakerAccess")
    public boolean USE_RELATIVE_QF = true;

    private static final String messagePattern = "Perhaps this can be replaced with %c%::class";

    @SuppressWarnings("CanBeFinal")
    static private Pattern classNameRegex = null;
    static {
        // Original regex: (\\(\\)?)?([a-zA-z0-9_]+\\(\\)?)?([a-zA-z0-9_]+)
        classNameRegex = Pattern.compile("(\\\\(\\\\)?)?([a-zA-z0-9_]+\\\\(\\\\)?)?([a-zA-z0-9_]+)");
    }

    @NotNull
    public String getShortName() {
        return "ClassConstantCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpStringLiteralExpression(StringLiteralExpression expression) {
                /* ensure selected language level supports the ::class feature*/
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (!phpVersion.hasFeature(PhpLanguageFeature.CLASS_NAME_CONST)) {
                    return;
                }

                /* Skip certain contexts processing and strings with inline injections */
                final PsiElement parent = expression.getParent();
                if (
                    parent instanceof BinaryExpression || parent instanceof SelfAssignmentExpression ||
                    null != expression.getFirstPsiChild()
                ) {
                    return;
                }

                /* Process if has no inline statements and at least 3 chars long (foo, bar and etc. are not a case) */
                final String contents = expression.getContents();
                if (contents.length() > 3) {
                    final Matcher regexMatcher = classNameRegex.matcher(contents);
                    if (!regexMatcher.matches() || ExpressionSemanticUtil.getBlockScope(expression) instanceof PhpDocComment) {
                        return;
                    }
                    String normalizedContents = contents.replaceAll("\\\\\\\\", "\\\\");

                    /* TODO: handle __NAMESPACE__.'\Class' */
                    final boolean isFull = normalizedContents.charAt(0) == '\\';
                    Set<String> namesToLookup = new HashSet<>();
                    if (isFull) {
                        namesToLookup.add(normalizedContents);
                    } else {
                        normalizedContents = '\\' + normalizedContents;
                        namesToLookup.add(normalizedContents);
                    }

                    /* if we could find an appropriate candidate and resolved the class => report (case must match) */
                    if (1 == namesToLookup.size()) {
                        String fqnToLookup = namesToLookup.iterator().next();
                        PhpIndex index     = PhpIndex.getInstance(expression.getProject());

                        /* try searching interfaces and classes for the given FQN */
                        Collection<PhpClass> classes = index.getClassesByFQN(fqnToLookup);
                        if (0 == classes.size()) {
                            classes = index.getInterfacesByFQN(fqnToLookup);
                        }

                        /* check resolved items */
                        if (1 == classes.size() && classes.iterator().next().getFQN().equals(fqnToLookup)) {
                            final String message = messagePattern.replace("%c%", normalizedContents);
                            holder.registerProblem(expression, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(normalizedContents, IMPORT_CLASSES_ON_QF, USE_RELATIVE_QF));
                        }
                    }
                    namesToLookup.clear();
                }
            }
        };
    }

    static private class TheLocalFix implements LocalQuickFix {
        final String fqn;
        final boolean importClasses;
        final boolean useRelativeQN;

        @NotNull
        @Override
        public String getName() {
            return "Use ::class instead";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        TheLocalFix(@NotNull String fqn, boolean importClasses, boolean useRelativeQN) {
            super();

            this.fqn           = fqn;
            this.importClasses = importClasses;
            this.useRelativeQN = useRelativeQN;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target instanceof StringLiteralExpression) {
                String classForReplacement = fqn;
                String className = classForReplacement.substring(1 + classForReplacement.lastIndexOf('\\'));

                synchronized (target.getContainingFile()) {
                    final PhpFile file            = (PhpFile) target.getContainingFile();
                    boolean isImportedAlready     = false;
                    boolean isImportNameCollision = false;
                    PsiElement importMarker       = null;

                    /* check all use-statements and use imported name for QF */
                    for (PhpUseList use : PsiTreeUtil.findChildrenOfType(file, PhpUseList.class)) {
                        /* do not process `function() use () {}` constructs */
                        if (use.getParent() instanceof Function) {
                            continue;
                        }

                        importMarker = use;
                        for (PsiElement used : use.getChildren()){
                            if (!(used instanceof PhpUse)) {
                                continue;
                            }

                            final PhpUse useStatement = (PhpUse) used;
                            if (useStatement.getFQN().equals(fqn)) {
                                classForReplacement = useStatement.getName();
                                isImportedAlready   = true;
                                break;
                            }

                            if (className.equals(useStatement.getName())) {
                                isImportNameCollision = true;
                            }
                        }
                    }

                    if (!isImportedAlready && !isImportNameCollision && importClasses) {
                        /* do not import classes from the root namespace */
                        boolean makesSense = StringUtils.countMatches(classForReplacement, "\\") > 1;
                        if (makesSense) {
                            boolean insertBefore  = false;
                            boolean useRelativeQN = false;

                            /* identify marker/use relative QNs */
                            PhpNamespaceReference ns = PsiTreeUtil.findChildOfType(file, PhpNamespaceReference.class);
                            if (null != ns) {
                                /* NS-ed file */
                                if (null == importMarker && ns.getNextPsiSibling() instanceof GroupStatement) {
                                    importMarker = ns.getNextPsiSibling().getFirstPsiChild();
                                    insertBefore = true;
                                }

                                if (this.useRelativeQN) {
                                    final String nsFQN  = ((PhpNamespace) ns.getParent()).getFQN() + '\\';
                                    if (classForReplacement.startsWith(nsFQN)) {
                                        classForReplacement = classForReplacement.replace(nsFQN, "");
                                        useRelativeQN       = true;
                                    }
                                }
                            } else {
                                /* regular files, no NS */
                                if (file.getFirstPsiChild() instanceof GroupStatement) {
                                    importMarker = file.getFirstPsiChild().getFirstPsiChild();
                                    insertBefore = true;
                                }
                            }

                            /* inject new import after the marker, if relative QN are not possible */
                            if (null != importMarker && !useRelativeQN) {
                                PhpUseList use = PhpPsiElementFactory.createUseStatement(project, classForReplacement, null);

                                if (insertBefore) {
                                    importMarker.getParent().addBefore(use, importMarker);
                                    PsiElement space = PhpPsiElementFactory.createFromText(project, PsiWhiteSpace.class, "\n\n");
                                    if (null != space) {
                                        use.getParent().addAfter(space, use);
                                    }
                                } else {
                                    importMarker.getParent().addAfter(use, importMarker);
                                }

                                //noinspection ConstantConditions as we have hardcoded expression creation here
                                classForReplacement = use.getFirstPsiChild().getName();
                            }
                        }
                    }
                }

                PsiElement replacement = PhpPsiElementFactory.createFromText(project, ClassConstantReference.class, classForReplacement + "::class");
                if (null != replacement) {
                    target.replace(replacement);
                }
            }
        }
    }

    public JComponent createOptionsPanel() {
        return (new ClassConstantCanBeUsedInspector.OptionsPanel()).getComponent();
    }

    public class OptionsPanel {
        final private JPanel optionsPanel;

        final private JCheckBox importClassesAutomatically;
        final private JCheckBox useRelativeQNs;

        public OptionsPanel() {
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new MigLayout());

            importClassesAutomatically = new JCheckBox("Import classes automatically", IMPORT_CLASSES_ON_QF);
            importClassesAutomatically.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    IMPORT_CLASSES_ON_QF = importClassesAutomatically.isSelected();
                }
            });
            optionsPanel.add(importClassesAutomatically, "wrap");

            useRelativeQNs = new JCheckBox("Use relative QN where possible", USE_RELATIVE_QF);
            useRelativeQNs.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    USE_RELATIVE_QF = useRelativeQNs.isSelected();
                }
            });
            optionsPanel.add(useRelativeQNs, "wrap");
        }

        public JPanel getComponent() {
            return optionsPanel;
        }
    }
}
