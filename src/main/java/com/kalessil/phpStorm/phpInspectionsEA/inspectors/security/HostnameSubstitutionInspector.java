package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class HostnameSubstitutionInspector extends BasePhpInspection {
    private static final String patternGeneral = "The email generation can be compromised via '$_SERVER['%s']', consider introducing whitelists.";
    private static final String messageNaming  = "The domain here can be compromised, consider introducing whitelists.";

    private static final Pattern regexTargetNames;
    static {
        regexTargetNames = Pattern.compile(".*(domain|email|host).*", Pattern.CASE_INSENSITIVE);
    }

    @NotNull
    @Override
    public String getShortName() {
        return "HostnameSubstitutionInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Hostname substitution";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpArrayAccessExpression(@NotNull ArrayAccessExpression expression) {
                final PsiElement variable = expression.getValue();
                if (variable instanceof Variable && ((Variable) variable).getName().equals("_SERVER")) {
                    final ArrayIndex index = expression.getIndex();
                    final PsiElement key   = index == null ? null : index.getValue();
                    if (key instanceof StringLiteralExpression) {
                        final String attribute = ((StringLiteralExpression) key).getContents();
                        if (attribute.equals("SERVER_NAME") || attribute.equals("HTTP_HOST")) {
                            this.identifyContextAndDelegateInspection(expression, attribute);
                        }
                    }
                }
            }

            private void identifyContextAndDelegateInspection(@NotNull ArrayAccessExpression expression, @NotNull String attribute) {
                PsiElement parent = expression.getParent();
                while (parent != null && !(parent instanceof PsiFile)) {
                    if (parent instanceof Function || parent instanceof PhpClass) {
                        /* at function/method/class level we can stop */
                        break;
                    } else if (parent instanceof ConcatenationExpression){
                        this.inspectConcatenationContext(expression, (ConcatenationExpression) parent, attribute);
                        break;
                    } else if (OpenapiTypesUtil.isAssignment(parent)) {
                        this.inspectAssignmentContext(expression, (AssignmentExpression) parent, attribute);
                        break;
                    }
                    parent = parent.getParent();
                }
            }

            /* direct/decorated concatenation with "...@" */
            private void inspectConcatenationContext(
                    @NotNull ArrayAccessExpression substitutedExpression,
                    @NotNull ConcatenationExpression context,
                    @NotNull String attribute
            ) {
                PsiElement left = context.getLeftOperand();
                if (left instanceof ConcatenationExpression) {
                    left = ((ConcatenationExpression) left).getRightOperand();
                }
                final PsiElement right = context.getRightOperand();
                if (right != null && left instanceof StringLiteralExpression) {
                    final boolean containsAt = ((StringLiteralExpression) left).getContents().endsWith("@");
                    if (containsAt && !this.isChecked(substitutedExpression)) {
                        holder.registerProblem(
                                right,
                                String.format(MessagesPresentationUtil.prefixWithEa(patternGeneral), attribute)
                        );
                    }
                }
            }

            private void inspectAssignmentContext(
                @NotNull ArrayAccessExpression substitutedExpression,
                @NotNull AssignmentExpression context,
                @NotNull String attribute
            ) {
                final PsiElement storage = context.getVariable();
                if (storage instanceof FieldReference) {
                    /* fields processing is too complex, just report it when naming matches */
                    final String storageName = ((FieldReference) storage).getName();
                    if (storageName != null && !storageName.isEmpty()) {
                        final Matcher matcher = regexTargetNames.matcher(storageName);
                        if (matcher.matches() && !this.isChecked(substitutedExpression)) {
                            holder.registerProblem(
                                    substitutedExpression,
                                    MessagesPresentationUtil.prefixWithEa(messageNaming)
                            );
                        }
                    }
                } else if (storage instanceof Variable) {
                    /* variables can be processed in scope only */
                    final Function scope = ExpressionSemanticUtil.getScope(storage);
                    if (scope != null) {
                        final String variableName = ((Variable) storage).getName();
                        boolean reachedExpression = false;
                        for (final Variable candidate : PsiTreeUtil.findChildrenOfType(scope, Variable.class)) {
                            if (!reachedExpression) {
                                reachedExpression = candidate == storage;
                            } else if (candidate.getName().equals(variableName)) {
                                final PsiElement parent = candidate.getParent();
                                if (parent instanceof ConcatenationExpression) {
                                    this.inspectConcatenationContext(substitutedExpression, (ConcatenationExpression) parent, attribute);
                                }
                            }
                        }
                    } else {
                        /* variables in global context processing is too complex, just report it when naming matches */
                        final String storageName = ((PhpNamedElement) storage).getName();
                        if (!storageName.isEmpty()) {
                            final Matcher matcher = regexTargetNames.matcher(storageName);
                            if (matcher.matches() && !this.isChecked(substitutedExpression)) {
                                holder.registerProblem(
                                        substitutedExpression,
                                        MessagesPresentationUtil.prefixWithEa(messageNaming)
                                );
                            }
                        }
                    }
                }
            }

            private boolean isChecked(@NotNull ArrayAccessExpression substitutedExpression) {
                final Function scope = ExpressionSemanticUtil.getScope(substitutedExpression);
                if (scope != null) {
                    final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(scope);
                    if (body != null) {
                        return PsiTreeUtil.findChildrenOfType(body, FunctionReference.class).stream().anyMatch(c -> {
                            final String functionName = c.getName();
                            if (functionName != null && functionName.equals("in_array")) {
                                final PsiElement[] arguments = c.getParameters();
                                if (arguments.length > 0 && arguments[0] instanceof ArrayAccessExpression) {
                                    return OpenapiEquivalenceUtil.areEqual(arguments[0], substitutedExpression);
                                }
                            }
                            return false;
                        });
                    }
                }
                return false;
            }
        };
    }
}
