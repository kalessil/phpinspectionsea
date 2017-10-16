package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.PhpIsset;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NotEmptyCanBeUsedStrategy {
    private final static String messagePattern = "'isset(%s) && %s' can be replaced with '!empty(%s)'.";

     static public boolean apply(
         @NotNull List<PsiElement> conditions,
         @Nullable IElementType operation,
         @NotNull ProblemsHolder holder
     ) {
         boolean result = false;
         if (conditions.size() > 1 && PhpTokenTypes.tsSHORT_CIRCUIT_AND_OPS.contains(operation)) {
             for (final PsiElement issetCandidate : conditions) {
                 if (issetCandidate instanceof PhpIsset) {
                     final PhpIsset isset         = (PhpIsset) issetCandidate;
                     final PsiElement[] arguments = isset.getVariables();
                     for (final PsiElement argument : arguments) {
                         final Class clazz = argument.getClass();
                         for (final PsiElement match : conditions) {
                             if (match == issetCandidate || match.getClass() != clazz) {
                                 continue;
                             }

                             if (OpeanapiEquivalenceUtil.areEqual(argument, match)) {
                                 result = true;
                                 final String argumentAsText = argument.getText();
                                 final String message        = String.format(messagePattern, argumentAsText, argumentAsText, argumentAsText);
                                 final PsiElement target     = arguments.length == 1 ? isset : argument;
                                 holder.registerProblem(target, message, ProblemHighlightType.WEAK_WARNING);
                                 break;
                             }
                         }
                     }
                 }
             }
         }
         return result;
     }
}
