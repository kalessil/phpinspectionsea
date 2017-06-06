package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Funivan <alotofall@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
public class EfferentObjectCouplingInspector extends BasePhpInspection {

    public int COUPLING_LIMIT = 20;
    private static final String messagePattern = "Efferent coupling is %d.";

    @NotNull
    public String getShortName() {
        return "EfferentObjectCouplingInspection";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            @Override
            public void visitPhpClass(PhpClass clazz) {
                PsiElement nameIdentifier = clazz.getNameIdentifier();
                if (nameIdentifier != null) {
                    Collection<ClassReference> items = PsiTreeUtil.findChildrenOfType(clazz, ClassReference.class);
                    HashMap<String, Boolean> fqnReferences = new HashMap<>();
                    fqnReferences.put(clazz.getFQN(), true);
                    for (ClassReference reference : items) {
                        fqnReferences.putIfAbsent(reference.getFQN(), true);
                    }
                    int len = fqnReferences.keySet().size() - 1;
                    if (len >= COUPLING_LIMIT) {
                        holder.registerProblem(
                            nameIdentifier,
                            String.format(messagePattern, len),
                            ProblemHighlightType.WEAK_WARNING
                        );
                    }
                }
                super.visitPhpClass(clazz);
            }
        };
    }

}

