package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class AutoloadingIssuesInspector extends PhpInspection {
    private static final String messageName = "Class autoloading might be broken: file and class names are not matching.";
    private static final String messagePath = "Class autoloading might be broken: directory path and namespace are not matching.";

    final static private Pattern laravelMigration        = Pattern.compile("\\d{4}_\\d{2}_\\d{2}_\\d{6}_(\\w+)\\.php");
    private static final Collection<String> ignoredFiles = new HashSet<>();
    static {
        ignoredFiles.add("index.php");
        ignoredFiles.add("actions.class.php"); // Symfony 1.*
    }

    @NotNull
    @Override
    public String getShortName() {
        return "AutoloadingIssuesInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Class autoloading correctness";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFile(@NotNull PhpFile file) {
                if (this.shouldSkipAnalysis(file, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String fileName = file.getName();
                if (fileName.endsWith(".php") && !ignoredFiles.contains(fileName)) {
                    final PhpClass clazz = this.getClass(file);
                    if (clazz != null) {
                        final Matcher matcher = laravelMigration.matcher(fileName);
                        if (matcher.matches()) {
                            this.checkLaravelMigration(clazz, matcher.group(1));
                        } else {
                            final String expectedClassName = fileName.substring(0, fileName.indexOf('.'));
                            /* PSR-0 classloading (Package_Subpackage_Class) naming */
                            String extractedClassName      = clazz.getName();
                            if (clazz.getFQN().lastIndexOf('\\') == 0 && extractedClassName.indexOf('_') != -1) {
                                extractedClassName = extractedClassName.substring(extractedClassName.lastIndexOf('_') + 1);
                            }
                            /* check the file name as per extraction compliant with PSR-0/PSR-4 standards */
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

            private void checkLaravelMigration(@NotNull PhpClass clazz, @NotNull String classNamingInput) {
                final String expectedClassName = StringUtil.capitalizeWords(classNamingInput.replaceAll("_", " "), true).replaceAll(" ", "");
                if (!expectedClassName.equals(clazz.getName())) {
                    final PsiElement classNameNode = NamedElementUtil.getNameIdentifier(clazz);
                    if (classNameNode != null) {
                        holder.registerProblem(classNameNode, messageName);
                    }
                }
            }

            @Nullable
            private PhpClass getClass(@NotNull PhpFile file) {
                PhpClass clazz                 = null;
                final List<PsiElement> classes = file.getTopLevelDefs().values().stream().filter(d -> d instanceof PhpClass).collect(Collectors.toList());
                if (classes.size() == 1) {
                    /* according to PSRs file can contain only one class */
                    clazz = (PhpClass) classes.get(0);
                }
                classes.clear();

                return clazz;
            }
        };
    }
}
