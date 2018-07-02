package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

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
    private static final String messagePattern = "Efferent coupling is %d.";

    public int optionCouplingLimit = 20;

    @NotNull
    public String getShortName() {
        return "EfferentObjectCouplingInspection";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(
        @NotNull final ProblemsHolder holder,
        final boolean isOnTheFly
    ) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass phpClass) {
                final PsiElement nameIdentifier = phpClass.getNameIdentifier();

                if (nameIdentifier == null) {
                    return;
                }

                final Collection<ClassReference> classReferences = PsiTreeUtil.findChildrenOfType(phpClass, ClassReference.class);
                final Collection<String>         fqnReferences   = new ArrayList<>();

                fqnReferences.add(phpClass.getFQN());

                for (final ClassReference reference : classReferences) {
                    final String classReferenceFQN = reference.getFQN();

                    if (!fqnReferences.contains(classReferenceFQN)) {
                        fqnReferences.add(classReferenceFQN);
                    }
                }

                final int fqnReferencesSize = fqnReferences.size() - 1;

                if (fqnReferencesSize >= optionCouplingLimit) {
                    holder.registerProblem(
                        nameIdentifier,
                        String.format(messagePattern, fqnReferencesSize),
                        ProblemHighlightType.WEAK_WARNING
                    );
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addSpinner("Coupling limit:", optionCouplingLimit, (inputtedValue) -> optionCouplingLimit = inputtedValue)
        );
    }
}

