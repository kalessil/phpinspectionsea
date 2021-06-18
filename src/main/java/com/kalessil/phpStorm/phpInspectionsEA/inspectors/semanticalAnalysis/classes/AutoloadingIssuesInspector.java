package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
    private static final String message = "Class autoloading might be broken: file and class names are not identical.";

    final static private Pattern laravelMigration        = Pattern.compile("\\d{4}_\\d{2}_\\d{2}_\\d{6}_.+\\.php");
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
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFile(@NotNull PhpFile file) {
                final String fileName = file.getName();
                if (fileName.endsWith(".php") && ! ignoredFiles.contains(fileName) && ! laravelMigration.matcher(fileName).matches()) {
                    final List<PhpClass> classes = new ArrayList<>();
                    file.getTopLevelDefs().values().stream()
                            .filter(definition  -> definition instanceof PhpClass)
                            .forEach(definition -> classes.add((PhpClass) definition));
                    if (classes.size() == 1) {
                        final PhpClass clazz   = classes.get(0);
                        final String className = clazz.getName();
                        /* PSR-0 classloading (Package_Subpackage_Class) naming */
                        String extractedClassName = className;
                        if (clazz.getFQN().lastIndexOf('\\') == 0 && extractedClassName.indexOf('_') != -1) {
                            extractedClassName = extractedClassName.substring(1 + extractedClassName.lastIndexOf('_'));
                        }
                        /* check the file name as per extraction compliant with PSR-0/PSR-4 standards */
                        final String expectedClassName = fileName.substring(0, fileName.indexOf('.'));
                        if (this.isBreakingPsrStandard(className, expectedClassName, extractedClassName) && ! this.isWordpressStandard(className, fileName)) {
                            final PsiElement classNameNode = NamedElementUtil.getNameIdentifier(clazz);
                            if (classNameNode != null) {
                                holder.registerProblem(
                                        classNameNode,
                                        MessagesPresentationUtil.prefixWithEa(message)
                                );
                            }
                        }
                    }
                    classes.clear();
                }
            }

            private boolean isBreakingPsrStandard(@NotNull String className, @NotNull String expectedClassName, @NotNull String extractedClassName) {
                return ! expectedClassName.equals(extractedClassName) && ! expectedClassName.equals(className);
            }

            private boolean isWordpressStandard(@NotNull String className, @NotNull String fileName) {
                final String wordpressFileName = String.format("class-%s.php", className.toLowerCase().replaceAll("_", "-"));
                return fileName.endsWith(wordpressFileName);
            }
        };
    }
}
