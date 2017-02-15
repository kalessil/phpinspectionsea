package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PrivateConstructorSemanticsInspector extends BasePhpInspection {
    private static final String messageUtilNotFinal = "Utility class should be final (breaks backward compatibility).";
    private static final String messageUtilNaming   = "Utility class's name should end with 'Util'.";

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
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(clazz);
                if (null == constructor || null == nameNode || !constructor.getAccess().isPrivate()) {
                    return;
                }

                /* singletons constructors are normally private (we allow also protected), hence sto analysis */
                if (null != clazz.findOwnMethodByName("getInstance")) {
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
                for (Method method : ownMethods) {
                    final String methodName = method.getName();
                    final boolean isStatic  = method.isStatic();
                    if (!isStatic && !methodName.equals("__construct")) {
                        // non static method and it's not constructor - terminate inspection
                        return;
                    }

                    /* class has factory methods mixed in */
                    if (isStatic && (methodName.startsWith("create") || methodName.startsWith("from") || methodName.startsWith("valueOf"))) {
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