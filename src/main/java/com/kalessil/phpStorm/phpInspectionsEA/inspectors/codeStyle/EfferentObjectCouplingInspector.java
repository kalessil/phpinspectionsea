package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Set;
import java.util.stream.Collectors;

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
    // Inspection options.
    public int optionCouplingLimit = 20;

    private static final String messagePattern = "High efferent coupling (%d).";

    @NotNull
    @Override
    public String getShortName() {
        return "EfferentObjectCouplingInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Efferent coupling between objects";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                final PsiElement nameIdentifier = NamedElementUtil.getNameIdentifier(clazz);
                if (nameIdentifier != null) {
                    final Set<String> references = PsiTreeUtil.findChildrenOfType(clazz, ClassReference.class).stream()
                            .map(ClassReference::getFQN)
                            .collect(Collectors.toSet());
                    final int count = references.size();
                    if (count >= optionCouplingLimit) {
                        holder.registerProblem(
                                nameIdentifier,
                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), count)
                        );
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addSpinner("Coupling limit:", optionCouplingLimit, (input) -> optionCouplingLimit = input)
        );
    }
}

