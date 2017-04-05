package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.NamingConvention;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class NamingConventionInspector extends BasePhpInspection {
    private static final String message = "Class name does not follow naming convention";

    public NamingRules rules = new NamingRules();


    @NotNull
    public String getShortName() {
        return "NamingConventionInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean b) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClass(PhpClass phpClass) {
                final PsiElement phpClassNameIdentifier = phpClass.getNameIdentifier();
                if (phpClassNameIdentifier != null) {
                    // Create custom validate object to cache object type and extend list
                    final PhpValidatableClass validatableClass = new PhpValidatableClass(phpClass);
                    final Set<NamingRule> objectRules = rules.getRulesByType(validatableClass.getType());
                    for (NamingRule rule : objectRules) {
                        if (rule.isSupported(validatableClass)) {
                            if (!rule.isValid(phpClass)) {
                                holder.registerProblem(phpClassNameIdentifier, message, ProblemHighlightType.WEAK_WARNING);
                            }
                        }
                    }
                }

            }
        };
    }


}
