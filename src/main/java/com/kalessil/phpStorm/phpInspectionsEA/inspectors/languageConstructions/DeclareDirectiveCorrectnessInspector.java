package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Declare;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class DeclareDirectiveCorrectnessInspector extends PhpInspection {
    private static final String messagePattern = "Unknown directive '%d%'.";

    private static final Set<String> directives = new HashSet<>();
    static {
        directives.add("strict_types");
        directives.add("ticks");
        directives.add("encoding");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "DeclareDirectiveCorrectnessInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Declare directive correctness";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpDeclare(@NotNull Declare declare) {
                if (this.shouldSkipAnalysis(declare, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final PsiElement declaration = declare.getFirstPsiChild();
                final String declarationText = declaration == null ? null : declaration.getText();
                if (declarationText != null && declarationText.indexOf('=') != -1) {
                    final String directive = declarationText.split("=")[0];
                    if (!directives.contains(directive.trim())) {
                        final String message = messagePattern.replace("%d%", directive);
                        holder.registerProblem(declare, ReportingUtil.wrapReportedMessage(message));
                    }
                }
            }
        };
    }
}
