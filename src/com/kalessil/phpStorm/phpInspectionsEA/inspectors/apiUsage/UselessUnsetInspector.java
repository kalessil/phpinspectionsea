package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class UselessUnsetInspector extends BasePhpInspection {
    private static final String strProblemNoSense = "Only local copy/reference will be unset. Probably this unset can be removed.";

    @NotNull
    public String getDisplayName() {
        return "Unused: useless unset";
    }

    @NotNull
    public String getShortName() {
        return "UselessUnsetInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {

        /**
         * foreach is also a case, but there is no way to get flow entry point in actual JB platform API
         */

        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                this.inspectUsages(method.getParameters(), method);
            }

            public void visitPhpFunction(Function function) {
                this.inspectUsages(function.getParameters(), function);
            }

            private void inspectUsages(Parameter[] arrParameters, PhpScopeHolder objScopeHolder) {
                PhpEntryPointInstruction objEntryPoint = objScopeHolder.getControlFlow().getEntryPoint();

                for (Parameter objParameter : arrParameters) {
                    String strParameterName = objParameter.getName();
                    if (StringUtil.isEmpty(strParameterName)) {
                        continue;
                    }

                    /** find all usages of a parameter */
                    PhpAccessVariableInstruction[] arrUsages = PhpControlFlowUtil.getFollowingVariableAccessInstructions(objEntryPoint, strParameterName, false);
                    if (arrUsages.length == 0) {
                        continue;
                    }

                    /** iterate over usages */
                    for (PhpAccessVariableInstruction objInstruction : arrUsages) {
                        PsiElement objExpression = objInstruction.getAnchor();
                        /** target pattern detection */
                        if (objExpression.getParent() instanceof PhpUnset) {
                            /** choose warning target */
                            int unsetParametersCount = 0;
                            for (PsiElement objChild : objExpression.getParent().getChildren()) {
                                if (objChild instanceof PhpExpression) {
                                    ++unsetParametersCount;
                                }
                            }
                            PsiElement objWaringTarget = (unsetParametersCount == 1 ? objExpression.getParent() : objExpression);

                            holder.registerProblem(objWaringTarget, strProblemNoSense, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                        }
                    }
                }
            }
        };
    }
}