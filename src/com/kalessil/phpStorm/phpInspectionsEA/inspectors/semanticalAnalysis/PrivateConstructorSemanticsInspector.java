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
    private static final String messageSingletonConstructor = "Singleton constructor should be protected";
    private static final String messageUtilNotFinal         = "Utility class should be final (breaks backward compatibility)";
    private static final String messageUtilNaming           = "Utility class's name should end with 'Util'";

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
                    holder.registerProblem(nameNode, messageSingletonConstructor, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
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

                /* should have only static methods plus constructor */
                final Method[] ownMethods = clazz.getOwnMethods();
                if (null == ownMethods || ownMethods.length == 1) {
                    return;
                }
                for (Method objMethod : ownMethods) {
                    final String methodName = objMethod.getName();
                    if (!objMethod.isStatic() && !methodName.equals("__construct")) {
                        // non static method and it's not constructor - terminate inspection
                        return;
                    }

                    /* class has factory methods mixed in */
                    if (objMethod.isStatic() && (methodName.startsWith("create") || methodName.startsWith("from"))) {
                        return;
                    }
                }
                /* TODO: constants only - enum equivalent */

                /* ensure utility class is defined properly */
                final String classFQN = clazz.getFQN();
                if (!classFQN.endsWith("Util") && !classFQN.endsWith("Utils")) {
                    holder.registerProblem(nameNode, messageUtilNaming, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
                if (!clazz.isFinal()) {
                    holder.registerProblem(nameNode, messageUtilNotFinal, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }
}