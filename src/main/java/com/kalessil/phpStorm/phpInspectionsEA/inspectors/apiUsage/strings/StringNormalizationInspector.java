package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class StringNormalizationInspector extends BasePhpInspection {

    @NotNull
    public String getShortName() {
        return "StringNormalizationInspection";
    }

    private static final Set<String> lengthManipulation = new HashSet<>();
    private static final Set<String> caseManipulation   = new HashSet<>();
    static {
        caseManipulation.add("strtolower");
        caseManipulation.add("strtoupper");
        caseManipulation.add("mb_convert_case");
        caseManipulation.add("mb_strtolower");
        caseManipulation.add("mb_strtoupper");
        caseManipulation.add("ucfirst");
        caseManipulation.add("lcfirst");
        caseManipulation.add("ucwords");

        lengthManipulation.add("ltrim");
        lengthManipulation.add("rtrim");
        lengthManipulation.add("trim");
        lengthManipulation.add("substr");
        lengthManipulation.add("mb_substr");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* general structure expectation */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (null == functionName || 0 == params.length || !OpenapiTypesUtil.isFunctionReference(params[0])) {
                    return;
                }

                /* general inner structure expectation */
                final FunctionReference innerCall = (FunctionReference) params[0];
                final String innerFunctionName    = innerCall.getName();
                final PsiElement[] innerParams    = innerCall.getParameters();
                if (null == innerFunctionName || 0 == innerParams.length) {
                    return;
                }

                if (lengthManipulation.contains(functionName) && caseManipulation.contains(innerFunctionName)) {
                    holder.registerProblem(reference, "invert nesting", ProblemHighlightType.WEAK_WARNING);
                    return;
                }

                if (caseManipulation.contains(functionName) && caseManipulation.contains(innerFunctionName)) {
                    holder.registerProblem(reference, "senseless nesting", ProblemHighlightType.WEAK_WARNING);
                }

            }
        };
    }

}
