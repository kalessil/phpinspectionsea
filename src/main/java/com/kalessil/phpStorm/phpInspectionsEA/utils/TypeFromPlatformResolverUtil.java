package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

import java.util.HashSet;

final public class TypeFromPlatformResolverUtil {
    public static void resolveExpressionType(PsiElement objSubjectExpression, HashSet<String> objTypesSet) {
        Project project = objSubjectExpression.getProject();

        PhpType indexValueType = ((PhpTypedElement)objSubjectExpression).getType().global(project);
        if (indexValueType.getTypes().size() > 0) {
            for (String strType : indexValueType.getTypes()) {
                final boolean isSignatureProvided = strType.contains("?") || strType.contains("#");
                if (isSignatureProvided) {
                    TypeFromPsiResolvingUtil.resolveExpressionType(
                            objSubjectExpression,
                            ExpressionSemanticUtil.getScope(objSubjectExpression),
                            PhpIndex.getInstance(project),
                            objTypesSet
                    );
                    continue;
                }

                objTypesSet.add(Types.getType(strType));
            }
        }

        objTypesSet.remove(Types.strClassNotResolved);
        objTypesSet.remove(Types.strResolvingAbortedOnPsiLevel);
    }
}
