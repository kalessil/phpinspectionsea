package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class EmptyClassInspector extends BasePhpInspection {
    private static final String messageClass = "Class does not contain any properties or methods.";
    private static final String messageEnum  = "Enum does not contain any values or methods.";

    @NotNull
    @Override
    public String getShortName() {
        return "EmptyClassInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Empty class";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (! clazz.isInterface() && ! clazz.isDeprecated() && ! clazz.isAnonymous()) {
                    final boolean isEmpty = clazz.getOwnFields().length == 0 &&
                                            clazz.getOwnMethods().length == 0 &&
                                            clazz.getTraits().length == 0 &&
                                            clazz.getEnumCases().isEmpty();
                    if (isEmpty) {
                        final PhpClass parent = OpenapiResolveUtil.resolveSuperClass(clazz);
                        if (parent != null) {
                            /* we can be forced to introduce an empty class: abstract parent, exception classes */
                            final boolean skip = parent.isAbstract() || InterfacesExtractUtil.getCrawlInheritanceTree(clazz, true).stream().anyMatch(c -> c.getFQN().equals("\\Exception"));
                            if (skip) {
                                return;
                            }
                        }
                        final PsiElement nameNode = NamedElementUtil.getNameIdentifier(clazz);
                        if (nameNode != null) {
                            holder.registerProblem(
                                    nameNode,
                                    MessagesPresentationUtil.prefixWithEa(clazz.isEnum() ? messageEnum : messageClass )
                            );
                        }
                    }
                }
            }
        };
    }
}
