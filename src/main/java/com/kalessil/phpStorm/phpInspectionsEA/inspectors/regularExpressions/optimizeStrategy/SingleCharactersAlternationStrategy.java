package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.optimizeStrategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class SingleCharactersAlternationStrategy {
    private static final String messagePattern = "'%s' is 'greedy'. Please use '%s' instead.";

    final static private Pattern regexAlternations;
    static {
        /* original regex: (?:\(((?:\\[dDwWsS]|\\?.)(?:\|(?:\\[dDwWsS]|\\?.))+)\)) */
        regexAlternations = Pattern.compile("(?:\\(((?:\\\\[dDwWsS]|\\\\?.)(?:\\|(?:\\\\[dDwWsS]|\\\\?.))+)\\))");
    }

    static public void apply(@NotNull String pattern, @NotNull PsiElement target, @NotNull ProblemsHolder holder) {
        if (!pattern.isEmpty() && pattern.indexOf('|') >= 0) {
            final Matcher regexMatcher = regexAlternations.matcher(pattern);
            if (regexMatcher.find()) {
                boolean adviceOptimization  = true;
                final List<String> branches = new ArrayList<>();
                for (final String branch : regexMatcher.group(1).split("\\|")) {
                    final int charactersCount = branch.length();
                    if (charactersCount == 1 && (branch.equals("^") || branch.equals("$"))) {
                        adviceOptimization = false;
                        break;
                    }
                    branches.add(charactersCount == 1 && (branch.equals("]")) ? '\\' + branch : branch);
                }
                if (adviceOptimization) {
                    holder.registerProblem(
                            target,
                            String.format(messagePattern, regexMatcher.group(0), String.format("([%s])", String.join("", branches)))
                    );
                }
                branches.clear();
            }
        }
    }
}
