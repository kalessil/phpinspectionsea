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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassConstantCanBeUsedInspector extends BasePhpInspection {
    private static final String messagePattern = "Perhaps this can be replaced with %c%::class";

    @SuppressWarnings("CanBeFinal")
    static private Pattern classNameRegex = null;
    static {
        // Original regex: (\\(\\)?)?([a-zA-z0-9]+\\(\\)?)?([a-zA-z0-9]+)
        classNameRegex = Pattern.compile("(\\\\(\\\\)?)?([a-zA-z0-9]+\\\\(\\\\)?)?([a-zA-z0-9]+)");
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
                // if supported CLASS_NAME_CONST

                /* Skip Certain contexts processing and strings with inline injections */
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

                    /* TODO: handle __NAMESPACE__.'\Class' */
                    final boolean isRootNamespace = contents.charAt(0) == '\\';
                    final boolean lookupImports   = !isRootNamespace && contents.indexOf('\\') == -1;

                    Set<String> namesToLookup = new HashSet<>();
                    if (isRootNamespace) {
                        namesToLookup.add(contents);
                    } else {
                        /* we have to look up imports and the same namespace */
                        final PsiFile file = expression.getContainingFile();

                        if (lookupImports) {
                            for (PhpUseList use : PsiTreeUtil.findChildrenOfType(file, PhpUseList.class)) {
                                final PsiElement used = use.getFirstPsiChild();
                                if (used instanceof PhpUse) {
                                    final String usedFqn = ((PhpUse) used).getFQN();
                                    if (usedFqn.endsWith('\\' + contents)) {
                                        namesToLookup.add(usedFqn);
                                    }
                                }
                            }
                        }

                        /* lookup the same namespace, also fallbacks after imports lookup */
                        if (0 == namesToLookup.size()) {
                            final PhpNamespace namespace = PsiTreeUtil.findChildOfType(file, PhpNamespace.class);
                            if (null != namespace) {
                                final String namespaceFqn = namespace.getFQN();
                                if (!StringUtil.isEmpty(namespaceFqn)) {
                                    namesToLookup.add(namespaceFqn + '\\' + contents);
                                }
                            }
                        }
                    }


                    /* if we could find an appropriate candidate and resolved the class => report (case must match) */
                    if (1 == namesToLookup.size()) {
                        String fqnToLookup           = namesToLookup.iterator().next();
                        PhpIndex index               = PhpIndex.getInstance(expression.getProject());
                        Collection<PhpClass> classes = index.getClassesByFQN(fqnToLookup);
                        if (1 == classes.size() && classes.iterator().next().getFQN().equals(fqnToLookup)) {
                            final String message = messagePattern.replace("%c%", contents);
                            holder.registerProblem(expression, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(contents));
                        }
                    }
                    namesToLookup.clear();
                }
            }
        };
    }

    static private class TheLocalFix implements LocalQuickFix {
        final String fqn;

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

        TheLocalFix(@NotNull String fqn) {
            super();
            this.fqn = fqn;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target instanceof StringLiteralExpression) {
                PsiElement replacement = PhpPsiElementFactory.createFromText(project, ClassConstantReference.class, fqn + "::class");
                if (null != replacement) {
                    target.replace(replacement);
                }
            }
        }
    }
}
