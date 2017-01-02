package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPlatformResolverUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class ClassMethodNameMatchesFieldNameInspector extends BasePhpInspection {
    private static final String strProblemDescription = "There is a field with the same name, please give the method another name like is*, get*, set* and etc.";
    private static final String strProblemNotAnalyzed = "There is a field with the same name, but it's type can not be resolved.";

    @NotNull
    public String getShortName() {
        return "ClassMethodNameMatchesFieldNameInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                /* TODO: stick to class */
                PhpClass objClass = method.getContainingClass();
                String strMethodName = method.getName();
                if (null == objClass || StringUtil.isEmpty(strMethodName) || null == method.getNameIdentifier()) {
                    return;
                }

                HashSet<String> resolvedTypes = new HashSet<>();
                for (Field objField : objClass.getFields()) {
                    if (!objField.isConstant() && objField.getName().equals(strMethodName)) {
                        TypeFromPlatformResolverUtil.resolveExpressionType(objField, resolvedTypes);
                        if (resolvedTypes.size() > 0) {
                            if (resolvedTypes.contains(Types.strCallable) || resolvedTypes.contains("\\Closure")) {
                                holder.registerProblem(method.getNameIdentifier(), strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                            }

                            resolvedTypes.clear();
                        } else {
                            holder.registerProblem(method.getNameIdentifier(), strProblemNotAnalyzed, ProblemHighlightType.WEAK_WARNING);
                        }

                        return;
                    }
                }
            }
        };
    }
}