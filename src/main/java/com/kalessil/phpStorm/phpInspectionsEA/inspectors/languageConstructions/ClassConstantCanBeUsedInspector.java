package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ClassConstantCanBeUsedInspector extends BasePhpInspection {
    // Inspection options.
    public boolean IMPORT_CLASSES_ON_QF = true;
    public boolean USE_RELATIVE_QF      = false;
    public boolean LOOK_ROOT_NS_UP      = false;

    private static final String messagePattern   = "Perhaps this can be replaced with %c%::class.";
    private static final String messageUseStatic = "'static::class' can be used instead.";
    private static final String messageUseParent = "'parent::class' can be used instead.";

    final static private Pattern classNameRegex;
    static {
        // Original regex: (\\(\\)?)?([a-zA-z0-9_]+\\(\\)?)?([a-zA-z0-9_]+)
        classNameRegex = Pattern.compile("(\\\\(\\\\)?)?([a-zA-z0-9_]+\\\\(\\\\)?)?([a-zA-z0-9_]+)");
    }

    final private static Condition<PsiElement> PARENT_CLASS = new Condition<PsiElement>() {
        public boolean value(PsiElement element) { return element instanceof PhpClass; }
        public String toString()                 { return "Condition.PARENT_CLASS"; }
    };

    @NotNull
    @Override
    public String getShortName() {
        return "ClassConstantCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "::class can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP550)) {
                    final String functionName = reference.getName();
                    if (functionName != null) {
                        if (functionName.equals("get_called_class")) {
                            final PsiElement[] arguments = reference.getParameters();
                            if (arguments.length == 0) {
                                holder.registerProblem(
                                        reference,
                                        MessagesPresentationUtil.prefixWithEa(messageUseStatic),
                                        new UseStaticClassConstantFix()
                                );
                            }
                        } else if (functionName.equals("get_parent_class")) {
                            final PsiElement[] arguments = reference.getParameters();
                            if (arguments.length == 0 && PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP550)) {
                                holder.registerProblem(
                                        reference,
                                        MessagesPresentationUtil.prefixWithEa(messageUseParent),
                                        new UseParentClassConstantFix()
                                );
                            }
                        }
                    }
                }
            }

            @Override
            public void visitPhpStringLiteralExpression(@NotNull StringLiteralExpression expression) {
                /* ensure selected language level supports the ::class feature*/
                final Project project = holder.getProject();
                if (PhpLanguageLevel.get(project).below(PhpLanguageLevel.PHP550)) {
                    return;
                }

                /* Skip certain contexts processing and strings with inline injections */
                if (!OpenapiTypesUtil.isString(expression) || expression.getFirstPsiChild() != null) {
                    return;
                }
                final PsiElement parent = expression.getParent();
                if (parent instanceof BinaryExpression) {
                    boolean process               = false;
                    final BinaryExpression binary = (BinaryExpression) parent;
                    final PsiElement left          = binary.getLeftOperand();
                    if (binary.getOperationType() == PhpTokenTypes.opCONCAT && left instanceof ConstantReference) {
                        final String constantName = ((ConstantReference) left).getName();
                        process                   =  constantName != null && constantName.equals("__NAMESPACE__");
                    }
                    if (! process) {
                        return;
                    }
                }
                if (parent instanceof SelfAssignmentExpression || this.isClassAlias(expression)) {
                    return;
                }

                /* Process if has no inline statements and at least 3 chars long (foo, bar and etc. are not a case) */
                final String contents = this.populateLiteralContent(expression, parent instanceof BinaryExpression);
                if (contents.length() > 3 && classNameRegex.matcher(contents).matches()) {
                    /* do not process lowercase-only strings */
                    if (contents.indexOf('\\') == -1 && contents.toLowerCase().equals(contents)) {
                        return;
                    }

                    String normalizedContents = contents.replaceAll("\\\\\\\\", "\\\\");

                    final boolean isFull            = normalizedContents.charAt(0) == '\\';
                    final Set<String> namesToLookup = new HashSet<>();
                    if (isFull) {
                        namesToLookup.add(normalizedContents);
                    } else {
                        if (LOOK_ROOT_NS_UP || normalizedContents.contains("\\")) {
                            normalizedContents = '\\' + normalizedContents;
                            namesToLookup.add(normalizedContents);
                        }
                    }

                    /* if we could find an appropriate candidate and resolved the class => report (case must match) */
                    if (1 == namesToLookup.size()) {
                        final String fqn             = namesToLookup.iterator().next();
                        final PhpIndex index         = PhpIndex.getInstance(project);
                        final List<PhpClass> classes = OpenapiResolveUtil.resolveClassesAndInterfacesByFQN(fqn, index);
                        /* check resolved items */
                        if (!classes.isEmpty()) {
                            if (1 == classes.size() && classes.get(0).getFQN().equals(fqn)) {
                                if (parent instanceof BinaryExpression) {
                                    normalizedContents = expression.getContents().replaceAll("\\\\\\\\", "\\\\").replaceAll("^\\\\", "");
                                    holder.registerProblem(
                                            parent,
                                            MessagesPresentationUtil.prefixWithEa(messagePattern.replace("%c%", normalizedContents)),
                                            new TheLocalFix(normalizedContents, IMPORT_CLASSES_ON_QF, USE_RELATIVE_QF)
                                    );
                                } else {
                                    holder.registerProblem(
                                            expression,
                                            MessagesPresentationUtil.prefixWithEa(messagePattern.replace("%c%", normalizedContents)),
                                            new TheLocalFix(normalizedContents, IMPORT_CLASSES_ON_QF, USE_RELATIVE_QF)
                                    );
                                }
                            }
                            classes.clear();
                        }
                    }
                    namesToLookup.clear();
                }
            }

            @NotNull
            private String populateLiteralContent(@NotNull StringLiteralExpression literal, boolean prependNamespace) {
                String content = literal.getContents();
                if (prependNamespace) {
                    final PsiElement clazz = PsiTreeUtil.findFirstParent(literal, PARENT_CLASS);
                    if (clazz != null) {
                        content = ((PhpClass) clazz).getNamespaceName() + content.replaceAll("^\\\\", "");
                    }
                }
                return content;
            }

            private boolean isClassAlias(@NotNull StringLiteralExpression literal) {
                boolean result          = false;
                final PsiElement parent = literal.getParent();
                if (parent instanceof ParameterList) {
                    final PsiElement grandParent = parent.getParent();
                    if (OpenapiTypesUtil.isFunctionReference(grandParent)) {
                        final FunctionReference reference = (FunctionReference) grandParent;
                        final String functionName         = reference.getName();
                        if (functionName != null && functionName.equals("class_alias")) {
                            final PsiElement[] arguments = reference.getParameters();
                            result = arguments.length == 2 && arguments[1] == literal;
                        }
                    }
                }
                return result;
            }
        };
    }

    private static final class UseStaticClassConstantFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use static::class instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseStaticClassConstantFix() {
            super("static::class");
        }
    }

    private static final class UseParentClassConstantFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use parent::class instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseParentClassConstantFix() {
            super("parent::class");
        }
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use ::class instead";

        final String fqn;
        final boolean importClasses;
        final boolean useRelativeQN;

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
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
            if (target != null && !project.isDisposed()) {
                String classForReplacement = fqn;
                String className           = classForReplacement.substring(1 + classForReplacement.lastIndexOf('\\'));

                synchronized (target.getContainingFile()) {
                    final PhpFile file            = (PhpFile) target.getContainingFile();
                    boolean isImportedAlready     = false;
                    boolean isImportNameCollision = false;
                    PsiElement importMarker       = null;

                    /* check all use-statements and use imported name for QF */
                    for (final PhpUseList use : PsiTreeUtil.findChildrenOfType(file, PhpUseList.class)) {
                        /* do not process `function() use () {}` constructs or class traits */
                        final PsiElement useParent = use.getParent();
                        if (useParent instanceof Function || useParent instanceof PhpClass) {
                            continue;
                        }

                        importMarker = use;
                        for (final PsiElement used : use.getChildren()){
                            if (used instanceof PhpUse) {
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
                    }

                    if (importClasses && !isImportedAlready && !isImportNameCollision) {
                        /* do not import classes from the root namespace */
                        boolean makesSense = StringUtils.countMatches(classForReplacement, "\\") > 1;
                        if (makesSense) {
                            boolean insertBefore  = false;
                            boolean useRelativeQN = false;

                            /* identify marker/use relative QNs */
                            final PhpNamespace ns = PsiTreeUtil.findChildOfType(file, PhpNamespace.class);
                            if (ns != null) {
                                /* ensure that current NS doesn't have  */
                                final Collection<PhpClass> classNameCollisions = OpenapiResolveUtil.resolveClassesByFQN(
                                        ns.getFQN() + '\\' + className,
                                        PhpIndex.getInstance(project)
                                );
                                if (classNameCollisions.isEmpty()) {
                                    /* NS-ed file */
                                    if (importMarker == null && ns.getLastChild() instanceof GroupStatement) {
                                        importMarker = ((GroupStatement) ns.getLastChild()).getFirstPsiChild();
                                        insertBefore = true;
                                    }
                                    if (this.useRelativeQN) {
                                        final String nsFQN = ns.getFQN() + '\\';
                                        if (classForReplacement.startsWith(nsFQN)) {
                                            classForReplacement = classForReplacement.replace(nsFQN, "");
                                            useRelativeQN       = true;
                                        }
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
                            if (importMarker != null && !useRelativeQN) {
                                /* bug-fix: strict types declaration must be the very first statement  */
                                if (importMarker instanceof Declare) {
                                    insertBefore = true;
                                    importMarker = ((Declare) importMarker).getNextPsiSibling();
                                }

                                if (importMarker != null) {
                                    final PhpUseList use = PhpPsiElementFactory.createUseStatement(project, classForReplacement, null);
                                    if (insertBefore) {
                                        importMarker.getParent().addBefore(use, importMarker);
                                        final PsiElement space = PhpPsiElementFactory.createFromText(project, PsiWhiteSpace.class, "\n\n");
                                        if (space != null) {
                                            use.getParent().addAfter(space, use);
                                        }
                                    } else {
                                        importMarker.getParent().addAfter(use, importMarker);
                                    }
                                    classForReplacement = use.getFirstPsiChild().getName();
                                }
                            }
                        }
                    }
                }

                final PsiElement replacement = PhpPsiElementFactory.createFromText(project, ClassConstantReference.class, classForReplacement + "::class");
                if (replacement != null) {
                    target.replace(replacement);
                }
            }
        }
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Lookup root namespace classes", LOOK_ROOT_NS_UP, (isSelected) -> LOOK_ROOT_NS_UP = isSelected);
            component.addCheckbox("Import classes automatically", IMPORT_CLASSES_ON_QF, (isSelected) -> IMPORT_CLASSES_ON_QF = isSelected);
            component.addCheckbox("Use relative QN where possible", USE_RELATIVE_QF, (isSelected) -> USE_RELATIVE_QF = isSelected);
        });
    }
}
