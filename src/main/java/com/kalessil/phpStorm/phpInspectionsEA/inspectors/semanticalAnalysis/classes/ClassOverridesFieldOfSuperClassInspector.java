package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;

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
    public boolean REPORT_PRIVATE_REDEFINITION = true;

    private static final String patternShadows            = "Field '%p%' is already defined in %c%, check our online documentation for options.";
    private static final String patternProtectedCandidate = "Likely needs to be renamed in sake of maintainability (private property with the same name already defined in %c%).";

    @NotNull
    @Override
    public String getShortName() {
        return "ClassOverridesFieldOfSuperClassInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Class overrides a field of a parent class";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpField(@NotNull Field ownField) {
                /* skip un-explorable and test classes */
                final PhpClass clazz = ownField.getContainingClass();
                if (clazz == null || this.isTestContext(clazz)) {
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
                /* ensure field doesn't have any user-land annotations */
                final PhpDocTag[] tags  = PsiTreeUtil.getChildrenOfType(ownField.getDocComment(), PhpDocTag.class);
                final boolean annotated = tags != null && Arrays.stream(tags).anyMatch(t -> !t.getName().equals(t.getName().toLowerCase()));
                if (annotated) {
                    return;
                }

                final PhpClass parent     = OpenapiResolveUtil.resolveSuperClass(clazz);
                final String ownFieldName = ownField.getName();
                final Field parentField   = parent == null ? null : OpenapiResolveUtil.resolveField(parent, ownFieldName);
                if (parentField != null) {
                    final PhpClass parentFieldHolder = parentField.getContainingClass();
                    final PsiElement fieldNameNode   = NamedElementUtil.getNameIdentifier(ownField);
                    if (fieldNameNode != null && parentFieldHolder != null) {
                        /* re-defining private fields with the same name is pretty suspicious itself */
                        if (parentField.getModifier().isPrivate()) {
                            if (REPORT_PRIVATE_REDEFINITION) {
                                holder.registerProblem(
                                        fieldNameNode,
                                        MessagesPresentationUtil.prefixWithEa(patternProtectedCandidate.replace("%c%", parentFieldHolder.getFQN())),
                                        ProblemHighlightType.WEAK_WARNING
                                );
                            }
                            return;
                        }

                        /* report only cases when access level is not changed */
                        if (!ownField.getModifier().getAccess().isWeakerThan(parentField.getModifier().getAccess())) {
                           /* fire common warning */
                            holder.registerProblem(
                                    fieldNameNode,
                                    MessagesPresentationUtil.prefixWithEa(patternShadows.replace("%p%", ownFieldName).replace("%c%", parentFieldHolder.getFQN())),
                                    ProblemHighlightType.WEAK_WARNING
                            );
                        }
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component)
            -> component.addCheckbox("Report re-defining private fields", REPORT_PRIVATE_REDEFINITION, (isSelected) -> REPORT_PRIVATE_REDEFINITION = isSelected)
        );
    }
}
