package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpDoc;


import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc.ThrowsResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpExceptions.CollectPossibleThrowsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class ThrowsAnnotatedProperlyInspector extends BasePhpInspection {
    private static final String strProblemMissing       = "Implicitly thrown exception is not handled/annotated: '%c%'";
    private static final String strProblemInternalCalls = "Nested call's exception is not handled/annotated: '%c%'";
    private static final String strProblemViolates      = "Implicitly thrown exception violates @inheritdoc (contract interface violation): '%c%'";

    @NotNull
    public String getShortName() {
        return "ThrowsAnnotatedProperlyInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                String strMethodName = method.getName();
                PsiElement objMethodName = method.getNameIdentifier();
                if (StringUtil.isEmpty(strMethodName) || null == objMethodName) {
                    return;
                }

                PhpClass clazz = method.getContainingClass();
                if (null == clazz) {
                    return;
                }
                String strClassFQN = clazz.getFQN();
                /* skip un-explorable and test classes */
                if (
                    StringUtil.isEmpty(strClassFQN) ||
                    strClassFQN.contains("\\Tests\\") || strClassFQN.contains("\\Test\\") ||
                    strClassFQN.endsWith("Test")
                ) {
                    return;
                }

                HashSet<PhpClass> annotatedExceptions = new HashSet<PhpClass>();
                if (ThrowsResolveUtil.ResolveType.NOT_RESOLVED == ThrowsResolveUtil.resolveThrownExceptions(method, annotatedExceptions)) {
                    return;
                }

                HashSet<PsiElement> processedRegistry = new HashSet<PsiElement>();
                HashSet<PhpClass> throwsExceptions = CollectPossibleThrowsUtil.collectNestedAndWorkflowExceptions(method, processedRegistry, holder);
holder.registerProblem(objMethodName, "Processed: " + processedRegistry.size(), ProblemHighlightType.WEAK_WARNING);
                processedRegistry.clear();

holder.registerProblem(objMethodName, "Annotated: " + annotatedExceptions.toString(), ProblemHighlightType.WEAK_WARNING);
holder.registerProblem(objMethodName, "Throws: "    + throwsExceptions.toString(), ProblemHighlightType.WEAK_WARNING);


//                ...
//                String strError = isInheritDoc ? strProblemViolates : strProblemMissing;
//                ProblemHighlightType highlight = isInheritDoc ?
//                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING :
//                        ProblemHighlightType.WEAK_WARNING;
//
//                strError = strError.replace("%c%", notCoveredThrows.toString());
//                holder.registerProblem(objMethodName, strError, highlight);

            }
        };
    }
}