package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPlatformResolverUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class ClassMethodNameMatchesFieldNameInspector extends BasePhpInspection {
    private static final String messageMatches   = "There is a field with the same name, please give the method another name like is*, get*, set* and etc.";
    private static final String messageFieldType = "There is a field with the same name, but it's type can not be resolved.";

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
                final PhpClass clazz      = method.getContainingClass();
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
                if (null == clazz || null == nameNode || clazz.isInterface() || clazz.isTrait()) {
                    return;
                }

                final String methodName = method.getName();
                for (Field field : clazz.getFields()) {
                    if (field.isConstant() || !field.getName().equals(methodName)) {
                        continue;
                    }

                    final HashSet<String> resolvedTypes = new HashSet<>();
                    TypeFromPlatformResolverUtil.resolveExpressionType(field, resolvedTypes);
                    if (resolvedTypes.size() > 0) {
                        if (resolvedTypes.contains(Types.strCallable)) {
                            holder.registerProblem(nameNode, messageMatches, ProblemHighlightType.WEAK_WARNING);
                        }

                        resolvedTypes.clear();
                    } else {
                        holder.registerProblem(nameNode, messageFieldType, ProblemHighlightType.WEAK_WARNING);
                    }

                    break;
                }
            }
        };
    }
}