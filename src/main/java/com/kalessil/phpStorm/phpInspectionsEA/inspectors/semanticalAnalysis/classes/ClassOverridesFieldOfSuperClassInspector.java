package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpModifier;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FileSystemUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ClassOverridesFieldOfSuperClassInspector extends BasePhpInspection {
    // Inspection options.
    private boolean REPORT_PRIVATE_REDEFINITION = true;

    private static final String patternShadows            = "Field '%p%' is already defined in %c%, check our online documentation for options.";
    private static final String patternProtectedCandidate = "Likely needs to be protected (already defined in %c%).";

    @NotNull
    public String getShortName() {
        return "ClassOverridesFieldOfSuperClassInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpField(Field ownField) {
                /* skip un-explorable and test classes */
                final PhpClass clazz = ownField.getContainingClass();
                if (null == clazz || FileSystemUtil.isTestClass(clazz)) {
                    return;
                }

                /* skip static, constants and un-processable fields */
                final PsiElement ownFieldNameId = NamedElementUtil.getNameIdentifier(ownField);
                if (null == ownFieldNameId || ownField.isConstant() || ownField.getModifier().isStatic()) {
                    return;
                }
                /* ensure field is not defined via annotation */
                if (!(ExpressionSemanticUtil.getBlockScope(ownFieldNameId) instanceof PhpClass)) {
                    return;
                }


                final PhpModifier.Access ownFieldAccess  = ownField.getModifier().getAccess();
                final String ownFieldName                = ownField.getName();
                final PsiElement ownFieldParent          = ownField.getParent();
                for (PhpClass parentClass : InterfacesExtractUtil.getCrawlCompleteInheritanceTree(clazz, true)) {
                    /* ensure class and super are explorable */
                    final String superClassFQN = parentClass.getFQN();
                    if (clazz == parentClass || parentClass.isInterface() || StringUtil.isEmpty(superClassFQN)) {
                        continue;
                    }

                    for (Field superclassField : parentClass.getOwnFields()) {
                        if (!superclassField.getName().equals(ownFieldName)) {
                            continue;
                        }

                        /* re-defining private fields with the same name is pretty suspicious itself */
                        if (superclassField.getModifier().isPrivate()) {
                            if (REPORT_PRIVATE_REDEFINITION) {
                                final String message = patternProtectedCandidate.replace("%c%", superClassFQN);
                                holder.registerProblem(ownFieldParent, message, ProblemHighlightType.WEAK_WARNING);
                            }
                            continue;
                        }

                        /* report only cases when access level is not changed */
                        if (!ownFieldAccess.isWeakerThan(superclassField.getModifier().getAccess())) {
                            /* fire common warning */
                            final String message = patternShadows.replace("%p%", ownFieldName).replace("%c%", superClassFQN);
                            holder.registerProblem(ownFieldParent, message, ProblemHighlightType.WEAK_WARNING);
                        }
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Report re-defining private fields", REPORT_PRIVATE_REDEFINITION, (isSelected) -> REPORT_PRIVATE_REDEFINITION = isSelected);
        });
    }
}
