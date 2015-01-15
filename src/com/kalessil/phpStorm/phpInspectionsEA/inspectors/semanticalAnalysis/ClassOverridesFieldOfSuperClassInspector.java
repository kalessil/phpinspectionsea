package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class ClassOverridesFieldOfSuperClassInspector  extends BasePhpInspection {
    private static final String strProblemDescription = "Field %s% is already defined in a parent class. See inspection description for details.";

    @NotNull
    public String getDisplayName() {
        return "Architecture: class overrides a field of superclass";
    }

    @NotNull
    public String getShortName() {
        return "ClassOverridesFieldOfSuperClassInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                /** check if parent class is available */
                PhpClass objParentClass = clazz.getSuperClass();
                if (null == objParentClass || null == clazz.getNameIdentifier()) {
                    return;
                }

                for (Field ownField : clazz.getOwnFields()) {
                    if (ownField.isConstant() || null == ownField.getNameIdentifier()) {
                        continue;
                    }

                    String strOwnField = ownField.getName();
                    for (Field superclassField : objParentClass.getFields()) {
                        /** not possible to check access level */
                        if (
                            superclassField.getName().equals(strOwnField) &&
                            ExpressionSemanticUtil.getBlockScope(ownField.getNameIdentifier()) instanceof PhpClass
                            /** php doc can re-define property type */
                        ) {
                            String strWarning = strProblemDescription.replace("%s%", strOwnField);
                            holder.registerProblem(ownField.getNameIdentifier(), strWarning, ProblemHighlightType.WEAK_WARNING);
                            break;
                        }
                    }
                }
            }
        };
    }
}