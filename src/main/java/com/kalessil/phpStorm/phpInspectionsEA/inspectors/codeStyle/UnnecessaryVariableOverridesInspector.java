package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnnecessaryVariableOverridesInspector extends BasePhpInspection {
    private static final String message = "Unnecessary assignment, a nested call would simplify workflow.";

    @NotNull
    public String getShortName() {
        return "UnnecessaryVariableOverridesInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression assignment) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final PsiElement parent = assignment.getParent();
                if (OpenapiTypesUtil.isStatementImpl(parent)) {
                    final PsiElement variable = assignment.getVariable();
                    if (variable instanceof Variable) {
                        /* find the override source, incl. via nested calls */
                        PsiElement value = assignment.getValue();
                        while (value != null && OpenapiTypesUtil.isFunctionReference(value)) {
                            final FunctionReference call = (FunctionReference) value;
                            final PsiElement[] arguments = call.getParameters();
                            if (arguments.length != 1) {
                                value = null;
                            }  else if (arguments[0] instanceof Variable || OpenapiTypesUtil.isFunctionReference(arguments[0])) {
                                value = arguments[0];
                            } else {
                                value = null;
                            }
                        }
                        /* find out if we can apply nested calls */
                        if (value instanceof Variable) {
                            final String targetName = ((Variable) variable).getName();
                            final String sourceName = ((Variable) value).getName();
                            if (targetName.equals(sourceName)) {
                                PsiElement previous = ((PhpPsiElement) parent).getPrevPsiSibling();
                                previous            = previous == null ? null : previous.getFirstChild();
                                if (previous != null && OpenapiTypesUtil.isAssignment(previous)) {
                                    final AssignmentExpression check = (AssignmentExpression) previous;
                                    final PsiElement candidate       = check.getVariable();
                                    final PsiElement candidateValue  = check.getValue();
                                    if (candidateValue != null && candidate instanceof Variable) {
                                        final String candidateName = ((Variable) candidate).getName();
                                        if (candidateName.equals(sourceName)) {
                                            /* false-positive: parameter by reference */
                                            final PsiElement call = value.getParent().getParent();
                                            if (call instanceof FunctionReference) {
                                                final PsiElement function
                                                        = OpenapiResolveUtil.resolveReference((FunctionReference) call);
                                                if (function instanceof Function) {
                                                    final Parameter[] parameters = ((Function) function).getParameters();
                                                    if (parameters.length > 0 && parameters[0].isPassByRef()) {
                                                        return;
                                                    }
                                                }
                                            }

                                            holder.registerProblem(
                                                candidate,
                                                message,
                                                ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                                new MergeCallsFix(value, candidateValue, previous.getParent())
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class MergeCallsFix implements LocalQuickFix {
        private static final String title = "Use a nested call instead";

        private final SmartPsiElementPointer<PsiElement> target;
        private final SmartPsiElementPointer<PsiElement> value;
        private final SmartPsiElementPointer<PsiElement> absolete;

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

        MergeCallsFix(@NotNull PsiElement target, @NotNull PsiElement value, @NotNull PsiElement absolete) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(target.getProject());

            this.target   = factory.createSmartPsiElementPointer(target);
            this.value    = factory.createSmartPsiElementPointer(value);
            this.absolete = factory.createSmartPsiElementPointer(absolete);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            if (!project.isDisposed()) {
                final PsiElement target   = this.target.getElement();
                final PsiElement value    = this.value.getElement();
                final PsiElement absolete = this.absolete.getElement();
                if (target != null && value != null && absolete != null) {
                    target.replace(value.copy());
                    absolete.delete();
                }
            }
        }
    }
}
