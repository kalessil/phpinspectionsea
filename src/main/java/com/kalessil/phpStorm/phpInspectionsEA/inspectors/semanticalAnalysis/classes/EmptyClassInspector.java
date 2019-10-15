package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
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

public class EmptyClassInspector extends PhpInspection {
    private static final String message = "Class does not contain any properties or methods.";

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
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (this.shouldSkipAnalysis(clazz, StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE)) { return; }

                if (!clazz.isInterface() && !clazz.isDeprecated() && !clazz.isAnonymous()) {
                    final boolean isEmpty = clazz.getOwnFields().length == 0 && clazz.getOwnMethods().length == 0 && clazz.getTraits().length == 0;
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
                            holder.registerProblem(nameNode, ReportingUtil.wrapReportedMessage(message));
                        }
                    }
                }
            }
        };
    }
}
