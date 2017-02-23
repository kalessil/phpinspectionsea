package com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class PropertyImmediateOverrideStrategy {
    private static final String message = "The property is overridden immediately (default value is lost).";

    static public void apply(@NotNull Method method, @NotNull ProblemsHolder holder) {
        /* process only constructors with non-empty body */
        final PhpClass clazz = method.getContainingClass();
        if (null == clazz || clazz.isInterface() || clazz.isTrait() || !method.getName().equals("__construct")) {
            return;
        }
        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
        if (null == body || 0 == ExpressionSemanticUtil.countExpressionsInGroup(body)) {
            return;
        }

        /* collect properties with default values; stop inspection if none found */
        final Set<String> propertiesToCheck = new HashSet<>();
        for (Field field : clazz.getOwnFields()) {
            if (field.isConstant() || field.getModifier().isStatic()) {
                continue;
            }

            final PsiElement defaultValue = field.getDefaultValue();
            if (defaultValue instanceof PhpPsiElement && !PhpLanguageUtil.isNull(defaultValue)) {
                propertiesToCheck.add(field.getName());
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

            final PsiElement container = ((AssignmentExpression) assignmentCandidate).getVariable();
            if (container instanceof FieldReference && container.getFirstChild().getText().equals("$this")) {
                final String overriddenProperty = ((FieldReference) container).getName();
                if (null == overriddenProperty || !propertiesToCheck.contains(overriddenProperty)) {
                    continue;
                }

                holder.registerProblem(expression, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        }
        propertiesToCheck.clear();
    }
}
