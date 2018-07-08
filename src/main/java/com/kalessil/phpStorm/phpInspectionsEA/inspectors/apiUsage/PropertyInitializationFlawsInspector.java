package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

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
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

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

    private static final String messageDefaultNull     = "Null assignment can be safely removed. Define null in annotations if it's important.";
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
            @Override
            public void visitPhpField(@NotNull Field field) {
                if (REPORT_DEFAULTS_FLAWS && !field.isConstant()) {
                    final PhpClass clazz       = field.getContainingClass();
                    final PhpClass parentClazz = clazz == null ? null : OpenapiResolveUtil.resolveSuperClass(clazz);
                    final Field originField    = parentClazz == null ? null : OpenapiResolveUtil.resolveField(parentClazz, field.getName());

                    final PsiElement fieldDefault  = field.getDefaultValue();
                    final PsiElement originDefault = originField == null ? null : originField.getDefaultValue();

                    if (PhpLanguageUtil.isNull(fieldDefault)) {
                        holder.registerProblem(fieldDefault, messageDefaultNull, ProblemHighlightType.LIKE_UNUSED_SYMBOL, new DropFieldDefaultValueFix());
                    } else if (fieldDefault instanceof PhpPsiElement && originDefault instanceof PhpPsiElement) {
                        final boolean isDefaultDuplicate =
                            !originField.getModifier().getAccess().isPrivate() &&
                            OpenapiEquivalenceUtil.areEqual(fieldDefault, originDefault);
                        if (isDefaultDuplicate) {
                            holder.registerProblem(fieldDefault, messageSenselessWrite, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                        }
                    }
                }
            }

            @Override
            public void visitPhpMethod(@NotNull Method method) {
                /* configuration-based toggle */
                if (!REPORT_INIT_FLAWS) {
                    return;
                }

                /* process only constructors with non-empty body */
                final PhpClass clazz = method.getContainingClass();
                if (null == clazz || !method.getName().equals("__construct") || clazz.isInterface() || clazz.isTrait()) {
                    return;
                }
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                if (null == body || 0 == ExpressionSemanticUtil.countExpressionsInGroup(body)) {
                    return;
                }

                /* collect private properties with default values; stop inspection if none found */
                /* protected/public properties init in __construct can be bypassed, so defaults might have sense */
                final Map<String, PsiElement> propertiesToCheck = new HashMap<>();
                for (final Field field : clazz.getOwnFields()) {
                    if (!field.isConstant()) {
                        final PhpModifier modifiers = field.getModifier();
                        if (modifiers.isPrivate() && !modifiers.isStatic()) {
                            final PsiElement defaultValue = field.getDefaultValue();
                            if (defaultValue instanceof PhpPsiElement && !PhpLanguageUtil.isNull(defaultValue)) {
                                propertiesToCheck.put(field.getName(), defaultValue);
                            } else {
                                propertiesToCheck.put(field.getName(), null);
                            }
                        }
                    }
                }
                if (propertiesToCheck.isEmpty()) {
                    return;
                }

                /* iterate 1st level instructions and analyze overriding properties */
                for (final PsiElement expression : body.getChildren()) {
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
                            (null != fieldDefault && OpenapiEquivalenceUtil.areEqual(value, fieldDefault))
                        ) {
                            holder.registerProblem(expression, messageSenselessWrite, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                            continue;
                        }
                        if (null == fieldDefault) {
                            continue;
                        }

                        /* false-positive: property is involved into generating new value */
                        boolean isPropertyReused = false;
                        for (final FieldReference candidate : PsiTreeUtil.findChildrenOfType(value, FieldReference.class)) {
                            if (OpenapiEquivalenceUtil.areEqual(container, candidate)) {
                                isPropertyReused = true;
                                break;
                            }
                        }

                        if (!isPropertyReused && REPORT_DEFAULTS_FLAWS) {
                            holder.registerProblem(fieldDefault, messageDefaultOverride, new DropFieldDefaultValueFix());
                        }
                    }
                }
                propertiesToCheck.clear();
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Check default values", REPORT_DEFAULTS_FLAWS, (isSelected) -> REPORT_DEFAULTS_FLAWS = isSelected);
            component.addCheckbox("Check constructor", REPORT_INIT_FLAWS, (isSelected) -> REPORT_INIT_FLAWS = isSelected);
        });
    }

    private static final class DropFieldDefaultValueFix implements LocalQuickFix {
        private static final String title = "Remove default assignment";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement defaultValue = descriptor.getPsiElement();
            final Field field             = (Field) defaultValue.getParent();
            final PsiElement nameNode     = NamedElementUtil.getNameIdentifier(field);
            if (null != nameNode) {
                field.deleteChildRange(nameNode.getNextSibling(), defaultValue);
            }
        }
    }
}

