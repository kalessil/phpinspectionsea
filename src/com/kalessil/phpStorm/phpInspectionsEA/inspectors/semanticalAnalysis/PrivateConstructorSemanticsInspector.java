package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PrivateConstructorSemanticsInspector extends BasePhpInspection {
    private static final String strProblemSingletonConstructor = "Singleton constructor should be protected";
    private static final String strProblemUtilNotFinal = "Utility class should be final (breaks backward compatibility)";
    private static final String strProblemUtilNaming = "Utility class's name should end with 'Util'";

    @NotNull
    public String getShortName() {
        return "PrivateConstructorSemanticsInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                Method objConstructor = clazz.getOwnConstructor();
                if (null == objConstructor || !objConstructor.getAccess().isPrivate() || null == clazz.getNameIdentifier()) {
                    return;
                }

                if (null != clazz.findOwnMethodByName("getInstance")) {
                    holder.registerProblem(clazz.getNameIdentifier(), strProblemSingletonConstructor, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }

                /** ensure class has no any inheritance */
                List<ClassReference> listExtends = clazz.getExtendsList().getReferenceElements();
                List<ClassReference> listImplements = clazz.getImplementsList().getReferenceElements();
                if (
                    (listExtends != null && listExtends.size() > 0) ||
                    (listImplements != null && listImplements.size() > 0)
                ) {
                    return;
                }

                /** should have only static methods plus constructor */
                Method[] arrMethods = clazz.getOwnMethods();
                if (null == arrMethods || arrMethods.length == 1) {
                    return;
                }
                for (Method objMethod : arrMethods) {
                    if (!objMethod.isStatic() && !objMethod.getName().equals("__construct")) {
                        // non static method and it's not constructor - terminate inspection
                        return;
                    }
                }
                /** TODO: constants only - enum equivalent */

                /** ensure utility class is defined properly */
                String strFqn = clazz.getFQN();
                if (null != strFqn && !strFqn.endsWith("Util") && !strFqn.endsWith("Utils")) {
                    holder.registerProblem(clazz.getNameIdentifier(), strProblemUtilNaming, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
                if (!clazz.isFinal()) {
                    holder.registerProblem(clazz.getNameIdentifier(), strProblemUtilNotFinal, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }
}