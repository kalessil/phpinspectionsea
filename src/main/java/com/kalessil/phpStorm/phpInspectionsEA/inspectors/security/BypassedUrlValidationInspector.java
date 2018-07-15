package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class BypassedUrlValidationInspector extends LocalInspectionTool {
    private static final String messageFilterVar = "The call doesn't validate protocol and vulnerable to Java Script injections and Arbitrary files loading.";
    private static final String messageProtocol  = "Because of missing '^' at the beginning, it's possible to bypass the validation (e.g. 'URL?whatever=http://...').";
    private static final String messageExtension = "Because of missing '$' at the end, it's possible to bypass the validation (e.g. 'file.jpg.php').";

    final static private Pattern regexProtocolCheck;
    final static private Pattern regexFileExtensionCheck;
    final static private List<Pattern> matchers = new ArrayList<>();
    static {
        regexProtocolCheck      = Pattern.compile("^(?:\\\\b)?[\\[(?:]*(http|ftp|ssh|git)");
        regexFileExtensionCheck = Pattern.compile("\\\\\\.[(?:]*[a-z?]+(?:\\|[a-z?]+)*[)?]*$");

        /* same regexes in NotOptimalRegularExpressionsInspector (in order to not couple inspections) */
        matchers.add(Pattern.compile("^([^{<(\\[])(.*)(\\1)([a-zA-Z]+)?$"));
        matchers.add(Pattern.compile("^(\\{)(.*)(\\})([a-zA-Z]+)?$"));
        matchers.add(Pattern.compile("^(<)(.*)(>)([a-zA-Z]+)?$"));
        matchers.add(Pattern.compile("^(\\()(.*)(\\))([a-zA-Z]+)?$"));
        matchers.add(Pattern.compile("^(\\[)(.*)(\\])([a-zA-Z]+)?$"));
    }

    @NotNull
    public String getShortName() {
        return "BypassedUrlValidationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final String functionName = reference.getName();
                if (functionName != null) {
                    if (functionName.equals("preg_match")) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length >= 2) {
                            final Set<String> patterns = ExpressionSemanticUtil.resolveAsString(arguments[0]);
                            for (final String pattern : patterns) {
                                if (pattern != null && !pattern.isEmpty()) {
                                    for (final Pattern regex : matchers) {
                                        final Matcher matcher = regex.matcher(pattern);
                                        if (matcher.find()) {
                                            final String candidate = matcher.group(2);
                                            if (!candidate.startsWith("^") && regexProtocolCheck.matcher(candidate).find()) {
                                                holder.registerProblem(reference, messageProtocol);
                                            }
                                            if (!candidate.endsWith("$") && regexFileExtensionCheck.matcher(candidate).find()) {
                                                holder.registerProblem(reference, messageExtension);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                            patterns.clear();
                        }
                    } else if (functionName.equals("filter_var")) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length >= 2) {
                            for (final ConstantReference candidate : PsiTreeUtil.findChildrenOfType(reference, ConstantReference.class)) {
                                final String constantName = candidate.getName();
                                if (constantName != null && constantName.equals("FILTER_VALIDATE_URL")) {
                                    holder.registerProblem(reference, messageFilterVar);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
