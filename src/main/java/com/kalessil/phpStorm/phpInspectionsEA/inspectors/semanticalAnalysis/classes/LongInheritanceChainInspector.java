package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class LongInheritanceChainInspector extends BasePhpInspection {
    private static final String messagePattern = "Class has %c% parent classes, consider using appropriate design patterns.";

    private static final Set<String> showStoppers = new HashSet<>();
    static {
        /* for future people: controller classes must not appear here - deal with you debts! */

        /* allows to introduce own abstraction and test cases */
        showStoppers.add("\\PHPUnit_Framework_TestCase");
        showStoppers.add("\\PHPUnit\\Framework\\TestCase");

        /* prevents over-inheritance in user space; Yii 2+ */
        showStoppers.add("\\yii\\base\\Component");
        showStoppers.add("\\yii\\base\\Behavior");

        /* prevents over-inheritance in user space; Yii 1.* */
        showStoppers.add("\\CComponent");

        /* prevents over-inheritance in user space; Zend Framework 2+ */
        showStoppers.add("\\Zend\\Form\\Form");

        /* prevents over-inheritance in user space; Phalcon */
        showStoppers.add("\\Phalcon\\Di\\Injectable");
    }

    @NotNull
    public String getShortName() {
        return "LongInheritanceChainInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                final PsiElement psiClassName = NamedElementUtil.getNameIdentifier(clazz);
                final String className        = clazz.getName();
                /* skip un-reportable, exception and test classes */
                if (psiClassName == null || className.endsWith("Exception") || this.isTestContext(clazz)) {
                    return;
                }

                PhpClass classToCheck = clazz;
                PhpClass parent       = OpenapiResolveUtil.resolveSuperClass(classToCheck);
                /* false-positives: abstract class implementation */
                if (null != parent && !classToCheck.isAbstract() && parent.isAbstract()) {
                    return;
                }

                int parentsCount = 0;
                /* in source code class CAN extend itself, PS will report it but data structure is incorrect still */
                while (null != parent && clazz != parent) {
                    classToCheck = parent;
                    parent       = OpenapiResolveUtil.resolveSuperClass(classToCheck);
                    ++parentsCount;

                    if (null != parent) {
                        /* show-stoppers: frameworks god classes */
                        if (showStoppers.contains(parent.getFQN())) {
                            ++parentsCount;
                            break;
                        }
                        /* exceptions named according to DDD, check parents named with exception suffix */
                        if (parent.getName().endsWith("Exception")) {
                            return;
                        }
                    }
                }

                if (parentsCount >= 3 && !clazz.isDeprecated()) {
                    final String message = messagePattern.replace("%c%", String.valueOf(parentsCount));
                    holder.registerProblem(psiClassName, message, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}
