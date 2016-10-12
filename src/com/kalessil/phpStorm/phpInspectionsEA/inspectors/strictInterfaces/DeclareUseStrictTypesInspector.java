package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictInterfaces;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.impl.DeclareImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class DeclareUseStrictTypesInspector extends BasePhpInspection {
    private static final String message = "The file does not contain 'declare(strict_types=1)' declaration";

    @NotNull
    public String getShortName() {
        return "DeclareUseStrictTypesInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFile(PhpFile file) {
                /* ensure language level supports strict types declaration */
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (!phpVersion.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) { // PHP below 7 version
                    return;
                }

                /* find and inspect all declarations, hopefully you respect small files approach :) */
                boolean isStrictRegardingTypes = false;
                final Collection<DeclareImpl> declares = PsiTreeUtil.findChildrenOfType(file, DeclareImpl.class);
                if (declares.size() > 0) {
                    for (DeclareImpl declare : declares) {
                        if (declare.getText().replaceAll("\\s+","").contains("trict_types=1")) {
                            isStrictRegardingTypes = true;
                            break;
                        }
                    }

                    declares.clear();
                }

                /* report if strict types declaration missing */
                if (!isStrictRegardingTypes) {
                    holder.registerProblem(file, message, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}
