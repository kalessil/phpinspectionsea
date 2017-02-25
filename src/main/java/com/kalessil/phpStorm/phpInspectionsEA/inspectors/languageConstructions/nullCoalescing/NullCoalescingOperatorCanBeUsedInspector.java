package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy.GenerateAlternativeFromArrayKeyExistsStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy.GenerateAlternativeFromIssetStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy.GenerateAlternativeFromNullComparisonStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiPsiSearchUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

public class NullCoalescingOperatorCanBeUsedInspector extends BasePhpInspection {
    private static final String messageUseOperator = "'%e%' construction should be used instead.";

    @NotNull
    public String getShortName() {
        return "NullCoalescingOperatorCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpTernaryExpression(TernaryExpression expression) {
                final PhpLanguageLevel phpVersion = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (!phpVersion.hasFeature(PhpLanguageFeature.COALESCE_OPERATOR)) {
                    return;
                }

                final String replacementIsset = GenerateAlternativeFromIssetStrategy.generate(expression);
                if (null != replacementIsset) {
                    final String message = messageUseOperator.replace("%e%", replacementIsset);
                    holder.registerProblem(expression, message, ProblemHighlightType.WEAK_WARNING, new UseSuggestedReplacementFixer(replacementIsset));

                    return;
                }

                final String replacementNc = GenerateAlternativeFromNullComparisonStrategy.generate(expression);
                if (null != replacementNc) {
                    final String message = messageUseOperator.replace("%e%", replacementNc);
                    holder.registerProblem(expression, message, ProblemHighlightType.WEAK_WARNING, new UseSuggestedReplacementFixer(replacementIsset));

                    return;
                }

                final String replacementAke = GenerateAlternativeFromArrayKeyExistsStrategy.generate(expression);
                if (null != replacementAke) {
                    final String message = messageUseOperator.replace("%e%", replacementAke);
                    holder.registerProblem(expression, message, ProblemHighlightType.WEAK_WARNING, new UseSuggestedReplacementFixer(replacementAke));
                }
            }
        };
    }
}