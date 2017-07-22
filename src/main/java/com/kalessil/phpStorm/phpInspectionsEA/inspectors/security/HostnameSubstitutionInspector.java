package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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
    private static final String messageGeneral = "...";
    private static final String messageNaming  = "...";

    private static final Pattern regexTargetNames;
    static {
        regexTargetNames = Pattern.compile(".*(domain|email|host).*", Pattern.CASE_INSENSITIVE);
    }

    @NotNull
    public String getShortName() {
        return "HostnameSubstitutionInspection";
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
                            this.identifyContextAndDelegateInspection(expression);
                        }
                    }
                }
            }

            private void identifyContextAndDelegateInspection(@NotNull ArrayAccessExpression expression) {
                PsiElement parent = expression.getParent();
                while (parent != null && !(parent instanceof PsiFile)) {
                    if (parent instanceof Function || parent instanceof PhpClass) {
                        /* at function/method/class level we can stop */
                        break;
                    } else if (parent instanceof ConcatenationExpression){
                        this.inspectConcatenationContext(expression, (ConcatenationExpression) parent);
                        break;
                    } else if (OpenapiTypesUtil.isAssignment(parent)) {
                        this.inspectAssignmentContext(expression, (AssignmentExpression) parent);
                        break;
                    }
                    parent = parent.getParent();
                }
            }

            /* direct/decorated concatenation with "...@" */
            private void inspectConcatenationContext(
                @NotNull ArrayAccessExpression expression,
                @NotNull ConcatenationExpression context
            ) {
                PsiElement left = context.getLeftOperand();
                if (left instanceof ConcatenationExpression) {
                    left = ((ConcatenationExpression) left).getRightOperand();
                }
                if (left instanceof StringLiteralExpression) {
                    final boolean isEmailLike = ((StringLiteralExpression) left).getContents().endsWith("@");
                    if (isEmailLike) {
                        holder.registerProblem(expression, messageGeneral);
                    }
                }
            }

            private void inspectAssignmentContext(
                @NotNull ArrayAccessExpression expression,
                @NotNull AssignmentExpression context
            ) {
                final PsiElement storage = context.getVariable();
                if (storage instanceof Variable || storage instanceof FieldReference) {
                    final String storageName = ((PhpNamedElement) storage).getName();
                    if (!storageName.isEmpty()) {
                        final Matcher matcher = regexTargetNames.matcher(storageName);
                        if (matcher.matches()) {
                            holder.registerProblem(expression, messageNaming);
                        }
                    }
                    /* TODO: in case of variable - find usages, incl. invoking inspectConcatenationContext */
                }
            }
        };
    }

}
