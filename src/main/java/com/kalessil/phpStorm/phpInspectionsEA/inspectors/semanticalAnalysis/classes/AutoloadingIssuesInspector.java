package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class AutoloadingIssuesInspector extends BasePhpInspection {
    private static final String messageName = "Class autoloading might be broken: file and class names are not matching.";
    private static final String messagePath = "Class autoloading might be broken: directory path and namespace are not matching.";

    final static private Pattern laravelMigration        = Pattern.compile("\\d{4}_\\d{2}_\\d{2}_\\d{6}_(\\w+)\\.php");
    private static final Collection<String> ignoredFiles = new HashSet<>();
    static {
        ignoredFiles.add("index.php");
        ignoredFiles.add("actions.class.php"); // Symfony 1.*
    }

    @NotNull
    public String getShortName() {
        return "AutoloadingIssuesInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFile(@NotNull PhpFile file) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(file))                   { return; }

                final String fileName = file.getName();
                if (fileName.endsWith(".php") && !ignoredFiles.contains(fileName)) {
                    final PhpClass clazz = this.getClass(file);
                    if (clazz != null) {
                        final Matcher matcher = laravelMigration.matcher(fileName);
                        if (matcher.matches()) {
                            String expectedClassName = StringUtil.capitalizeWords(matcher.group(1).replaceAll("_", " "), true);
                            expectedClassName        = expectedClassName.replaceAll(" ", "");
                            /* match names and report issues */
                            if (!expectedClassName.equals(clazz.getName())) {
                                final PsiElement classNameNode = NamedElementUtil.getNameIdentifier(clazz);
                                if (classNameNode != null) {
                                    holder.registerProblem(classNameNode, messageName);
                                }
                            }
                        } else {
                            final String expectedClassName = fileName.substring(0, fileName.indexOf('.'));
                            /* case: older PSR classloading naming (Package_Subpackage_Class) */
                            String extractedClassName      = clazz.getName();
                            if (clazz.getFQN().lastIndexOf('\\') == 0 && extractedClassName.indexOf('_') != -1) {
                                extractedClassName = extractedClassName.substring(extractedClassName.lastIndexOf('_') + 1);
                            }
                            /* match names and report issues */
                            if (!expectedClassName.equals(extractedClassName) && !expectedClassName.equals(clazz.getName())) {
                                final PsiElement classNameNode = NamedElementUtil.getNameIdentifier(clazz);
                                if (classNameNode != null) {
                                    holder.registerProblem(classNameNode, messageName);
                                }
                            } else {
                                final String path = file.getVirtualFile().getPath();
                                if (path.contains("/src/")) {
                                    final String[] fragments = path.split("/src/");
                                    if (fragments.length == 2) {
                                        final String normalizedFragment = fragments[1].replaceAll("/", "\\\\").replaceAll(fileName, "");
                                        final String expectedFqnEnding  = "\\" + normalizedFragment + extractedClassName;
                                        if (!clazz.getFQN().endsWith(expectedFqnEnding)) {
                                            final PsiElement classNameNode = NamedElementUtil.getNameIdentifier(clazz);
                                            if (classNameNode != null) {
                                                holder.registerProblem(classNameNode, messagePath);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Nullable
            private PhpClass getClass(@NotNull PhpFile file) {
                /* according to PSRs file can contain only one class */
                final List<PhpClass> classes = new ArrayList<>();
                for (final PsiElement definition : file.getTopLevelDefs().values()) {
                    if (definition instanceof PhpClass) {
                        classes.add((PhpClass) definition);
                        if (classes.size() > 1) {
                            break;
                        }
                    }
                }
                final PhpClass clazz = classes.size() == 1 ? classes.get(0) : null;
                classes.clear();

                return clazz;
            }
        };
    }
}
