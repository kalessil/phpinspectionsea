package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClassReImplementsParentInterfaceInspector extends BasePhpInspection {
    private static final String strProblemDescription = "%i% is already announced in %c%.";

    @NotNull
    public String getShortName() {
        return "ClassReImplementsParentInterfaceInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                /** check if parent class and own implements are available */
                List<ClassReference> listImplements = clazz.getImplementsList().getReferenceElements();
                if (null == listImplements || listImplements.size() == 0 || null == clazz.getNameIdentifier()) {
                    return;
                }

                for (PhpClass objParentClass : clazz.getSupers()) {
                    /* ensure not interface, implements some interfaces and discoverable properly */
                    List<ClassReference> listImplementsParents = objParentClass.getImplementsList().getReferenceElements();
                    String strParentFQN = objParentClass.getFQN();
                    if (
                        null == strParentFQN ||
                        null == listImplementsParents || listImplementsParents.size() == 0 ||
                        objParentClass.isInterface()
                    ) {
                        continue;
                    }

                    /* check interfaces of a parent class */
                    for (ClassReference parentInterfaceRef : listImplementsParents) {
                        String parentInterfaceFQN = parentInterfaceRef.getFQN();
                        String parentInterfaceName = parentInterfaceRef.getName();
                        if (null == parentInterfaceFQN || null == parentInterfaceName) {
                            continue;
                        }

                        /* match parents interfaces against class we checking */
                        for (ClassReference ownInterface : listImplements) {
                            /* ensure FQNs matches */
                            String ownInterfaceFQN = ownInterface.getFQN();
                            if (null != ownInterfaceFQN && ownInterfaceFQN.equals(parentInterfaceFQN)) {
                                String strWarning = strProblemDescription
                                        .replace("%i%", parentInterfaceName)
                                        .replace("%c%", strParentFQN);
                                holder.registerProblem(clazz.getNameIdentifier(), strWarning, ProblemHighlightType.WEAK_WARNING);
                                break;
                            }
                        }
                    }
                }
            }
        };
    }
}