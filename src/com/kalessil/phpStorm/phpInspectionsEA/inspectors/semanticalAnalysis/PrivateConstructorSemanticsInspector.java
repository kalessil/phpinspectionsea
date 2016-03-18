package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
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
    private static final String strProblemUtilNotFinal         = "Utility class should be final (breaks backward compatibility)";
    private static final String strProblemUtilNaming           = "Utility class's name should end with 'Util'";

    @NotNull
    public String getShortName() {
        return "PrivateConstructorSemanticsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                final Method constructor  = clazz.getOwnConstructor();
                final PsiElement nameNode = clazz.getNameIdentifier();
                if (null == constructor || null == nameNode || !constructor.getAccess().isPrivate()) {
                    return;
                }

                if (null != clazz.findOwnMethodByName("getInstance")) {
                    holder.registerProblem(nameNode, strProblemSingletonConstructor, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }

                /* ensure class has no any inheritance */
                final List<ClassReference> listExtends    = clazz.getExtendsList().getReferenceElements();
                final List<ClassReference> listImplements = clazz.getImplementsList().getReferenceElements();
                if (listExtends.size() > 0 || listImplements.size() > 0) {
                    listExtends.clear();
                    listImplements.clear();

                    return;
                }

                /** should have only static methods plus constructor */
                final Method[] arrMethods = clazz.getOwnMethods();
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

                /* ensure utility class is defined properly */
                final String strFqn = clazz.getFQN();
                if (!strFqn.endsWith("Util") && !strFqn.endsWith("Utils")) {
                    holder.registerProblem(nameNode, strProblemUtilNaming, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
                if (!clazz.isFinal()) {
                    holder.registerProblem(nameNode, strProblemUtilNotFinal, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }
}