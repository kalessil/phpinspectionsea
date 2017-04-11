package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private static final Set<String> ignoredFiles = new HashSet<>();
    static {
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
            public void visitPhpFile(PhpFile file) {
                final String fileName = file.getName();
                if (fileName.endsWith(".php") && !ignoredFiles.contains(fileName)) {
                    /* find out how many named classes has been defined in the file */
                    final List<PhpClass> classes = new ArrayList<>();
                    for (PhpClass clazz : PsiTreeUtil.findChildrenOfAnyType(file, PhpClass.class)) {
                        if (null != NamedElementUtil.getNameIdentifier(clazz)) {
                            classes.add(clazz);
                        }
                    }

                    /* multiple classes defined, do nothing - this is not PSR compatible */
                    if (1 == classes.size()) {
                        final PhpClass clazz = classes.get(0);

                        /* support older PSR classloading (Package_Subpackage_Class) naming */
                        String extractedClassName = clazz.getName();
                        if (0 == clazz.getFQN().lastIndexOf('\\') && -1 != extractedClassName.indexOf('_')) {
                            extractedClassName = extractedClassName.substring(1 + extractedClassName.lastIndexOf('_'));
                        }

                        /* now check if names are identical */
                        final String expectedClassName = fileName.substring(0, fileName.indexOf('.'));
                        if (!expectedClassName.equals(extractedClassName) && !expectedClassName.equals(clazz.getName())) {
                            final PsiElement classNameNode = NamedElementUtil.getNameIdentifier(clazz);
                            if (null != classNameNode) {
                                holder.registerProblem(classNameNode, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            }
                        }
                    }
                    classes.clear();
                }
            }
        };
    }
}
