package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ClassConstantUsageCorrectnessInspector extends BasePhpInspection {
    private static final String message = "::class result and class qualified name are not identical (case mismatch).";

    @NotNull
    public String getShortName() {
        return "ClassConstantUsageCorrectnessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClassConstantReference(@NotNull ClassConstantReference constantReference) {
                final String constantName = constantReference.getName();
                if (constantName != null && constantName.equals("class")) {
                    final PsiElement reference = constantReference.getClassReference();
                    final String referencedQn  = reference == null ? null : reference.getText();
                    if (reference instanceof ClassReference) {
                        final ClassReference clazz = (ClassReference) reference;
                        final boolean isTarget     = !referencedQn.equals("self") && !referencedQn.equals("static");
                        final PsiElement resolved  = isTarget ? clazz.resolve() : null;
                        if (resolved instanceof PhpClass) {
                            /* the resolved class will accumulate case issue in it's FQN */
                            final String clazzFqn       = clazz.getFQN();
                            final List<String> variants = this.getVariants(clazz, (PhpClass) resolved);
                            if (!variants.isEmpty() && clazzFqn != null) {
                                final String implicitQn = variants.iterator().next();
                                if (!implicitQn.equals(referencedQn) && !clazzFqn.endsWith(implicitQn)) {
                                    holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                                }
                            }
                            variants.clear();
                        }
                    }
                }
            }

            @NotNull
            private List<String> getVariants(@NotNull ClassReference reference, @NotNull PhpClass clazz) {
                final List<String> result  = new ArrayList<>();
                final String referenceText = reference.getText();
                if (referenceText.startsWith("\\")) {
                    /* FQN specified, resolve as we might have case issues there */
                    final Project project = reference.getProject();
                    for (final PhpClass resolved : PhpIndex.getInstance(project).getClassesByFQN(referenceText)) {
                        result.add(resolved.getFQN());
                        break;
                    }
                } else {
                    PhpNamespace namespace = null;
                    PsiElement current     = reference.getParent();
                    while (current != null && !(current instanceof PsiFile)) {
                        if (current instanceof PhpNamespace) {
                            namespace = (PhpNamespace) current;
                            break;
                        }
                        current = current.getParent();
                    }

                    if (referenceText.contains("\\")) {
                        /* RQN specified */
                        if (namespace != null && !namespace.getFQN().equals("\\")) {
                            result.add(namespace.getFQN() + "\\" + referenceText);
                        }
                    } else {
                        /* imports (incl. aliases) */
                        final String classFqn = clazz.getFQN();
                        for (final PhpUse use : PsiTreeUtil.findChildrenOfType(current, PhpUse.class)) {
                            if (use.getFQN().equalsIgnoreCase(classFqn)) {
                                final String alias    = use.getAliasName();
                                final PsiElement what = use.getFirstChild();
                                if (alias != null) {
                                    /* alias as it is */
                                    result.add(alias);
                                } else if (what instanceof ClassReference) {
                                    /* resolve the imported class, as it's the source for correct naming */
                                    final PsiElement resolved = ((ClassReference) what).resolve();
                                    if (resolved instanceof PhpClass) {
                                        result.add(((PhpClass) resolved).getFQN());
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                return result;
            }
        };
    }
}
