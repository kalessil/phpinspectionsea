package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class PropertyInitializationFlawsInspector extends BasePhpInspection {
    // Inspection options.
    public boolean REPORT_DEFAULTS_FLAWS = true;
    public boolean REPORT_INIT_FLAWS     = true;

    private static final String messageDefaultValue    = "Null assignment can be safely removed. Define null in annotations if it's important.";
    private static final String messageDefaultOverride = "The assignment can be safely removed as the constructor overrides it.";
    private static final String messageSenselessWrite  = "Written value is same as default one, consider removing this assignment.";

    @NotNull
    public String getShortName() {
        return "PropertyInitializationFlawsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpField(Field field) {
                /* configuration-based toggle */
                if (!REPORT_DEFAULTS_FLAWS) {
                    return;
                }

                /* general construct requirements */
                final PsiElement defaultValue = field.getDefaultValue();
                if (field.isConstant() || !PhpLanguageUtil.isNull(defaultValue)) {
                    return;
                }

                /* if parent has non-private field, check its' defaults (terminate if non-null defaults) */
                final PhpClass clazz       = field.getContainingClass();
                final PhpClass parentClazz = null == clazz ? null : clazz.getSuperClass();
                final Field parentField    = null == parentClazz ? null : parentClazz.findFieldByName(field.getName(), false);
                if (null != parentField && !parentField.isConstant() && !parentField.getModifier().isPrivate()) {
                    final PsiElement parentDefaultValue = parentField.getDefaultValue();
                    if (parentDefaultValue instanceof PhpPsiElement && !PhpLanguageUtil.isNull(parentDefaultValue)) {
                        return;
                    }
                }

                /* fire a warning */
                holder.registerProblem(defaultValue, messageDefaultValue, ProblemHighlightType.LIKE_UNUSED_SYMBOL, new DropFieldDefaultValueFix());
            }

            public void visitPhpMethod(Method method) {
                /* configuration-based toggle */
                if (!REPORT_INIT_FLAWS) {
                    return;
                }

                /* process only constructors with non-empty body */
                final PhpClass clazz = method.getContainingClass();
                if (null == clazz || clazz.isInterface() || clazz.isTrait() || !method.getName().equals("__construct")) {
                    return;
                }
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                if (null == body || 0 == ExpressionSemanticUtil.countExpressionsInGroup(body)) {
                    return;
                }

                /* collect private properties with default values; stop inspection if none found */
                /* protected/public properties init in __construct can be bypassed, so defaults might have sense */
                final Map<String, PsiElement> propertiesToCheck = new HashMap<>();
                for (Field field : clazz.getOwnFields()) {
                    if (field.isConstant() || field.getModifier().isStatic() || !field.getModifier().isPrivate()) {
                        continue;
                    }

                    final PsiElement defaultValue = field.getDefaultValue();
                    if (defaultValue instanceof PhpPsiElement && !PhpLanguageUtil.isNull(defaultValue)) {
                        propertiesToCheck.put(field.getName(), defaultValue);
                    } else {
                        propertiesToCheck.put(field.getName(), null);
                    }
                }
                if (propertiesToCheck.isEmpty()) {
                    return;
                }

                /* iterate 1st level instructions and analyze overriding properties */
                for (PsiElement expression : body.getChildren()) {
                    final PsiElement assignmentCandidate = expression.getFirstChild();
                    if (!OpenapiTypesUtil.isAssignment(assignmentCandidate)) {
                        continue;
                    }

                    final AssignmentExpression assignment = (AssignmentExpression) assignmentCandidate;
                    final PsiElement container            = assignment.getVariable();
                    final PsiElement value                = assignment.getValue();
                    if (container instanceof FieldReference && container.getFirstChild().getText().equals("$this")) {
                        final String overriddenProperty = ((FieldReference) container).getName();
                        if (null == value || null == overriddenProperty || !propertiesToCheck.containsKey(overriddenProperty)) {
                            continue;
                        }
                        final PsiElement fieldDefault = propertiesToCheck.get(overriddenProperty);

                        /* Pattern: written and default values are identical */
                        if (
                            (null == fieldDefault && PhpLanguageUtil.isNull(value)) ||
                            (null != fieldDefault && PsiEquivalenceUtil.areElementsEquivalent(value, fieldDefault))
                        ) {
                            holder.registerProblem(expression, messageSenselessWrite, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                            continue;
                        }
                        if (null == fieldDefault) {
                            continue;
                        }

                        /* false-positive: property is involved into generating new value */
                        boolean isPropertyReused = false;
                        for (FieldReference candidate : PsiTreeUtil.findChildrenOfType(value, FieldReference.class)) {
                            if (!PsiEquivalenceUtil.areElementsEquivalent(container, candidate)) {
                                continue;
                            }

                            isPropertyReused = true;
                            break;
                        }

                        if (!isPropertyReused && REPORT_DEFAULTS_FLAWS) {
                            holder.registerProblem(fieldDefault, messageDefaultOverride, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new DropFieldDefaultValueFix());
                        }
                    }
                }
                propertiesToCheck.clear();
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.createCheckbox("Check default values", REPORT_DEFAULTS_FLAWS, (isSelected) -> REPORT_DEFAULTS_FLAWS = isSelected);
            component.createCheckbox("Check constructor", REPORT_INIT_FLAWS, (isSelected) -> REPORT_INIT_FLAWS = isSelected);
        });
    }

    private static class DropFieldDefaultValueFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Remove default assignment";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement defaultValue = descriptor.getPsiElement();
            final Field field             = (Field) defaultValue.getParent();

            final PsiElement nameNode = NamedElementUtil.getNameIdentifier(field);
            if (null != nameNode) {
                field.deleteChildRange(nameNode.getNextSibling(), defaultValue);
            }
        }
    }
}

