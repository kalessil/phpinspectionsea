package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UriPartExtractionInspector extends PhpInspection {
    private static final String messagePattern = "'%s' could be used here (directly extracts the desired part).";

    private static final Map<String, String> mapping = new HashMap<>();
    static {
        mapping.put("scheme",    "PHP_URL_SCHEME");
        mapping.put("host",      "PHP_URL_HOST");
        mapping.put("port",      "PHP_URL_PORT");
        mapping.put("user",      "PHP_URL_USER");
        mapping.put("pass",      "PHP_URL_PASS");
        mapping.put("path",      "PHP_URL_PATH");
        mapping.put("query",     "PHP_URL_QUERY");
        mapping.put("fragment",  "PHP_URL_FRAGMENT");
        mapping.put("dirname",   "PATHINFO_DIRNAME");
        mapping.put("basename",  "PATHINFO_BASENAME");
        mapping.put("extension", "PATHINFO_EXTENSION");
        mapping.put("filename",  "PATHINFO_FILENAME");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "UriPartExtractionInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "URI parts extraction";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && (functionName.equals("parse_url") || functionName.equals("pathinfo"))) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > 0) {
                        final PsiElement context = reference.getParent();
                        if (context instanceof ArrayAccessExpression) {
                            final ArrayIndex index = ((ArrayAccessExpression) context).getIndex();
                            if (index != null) {
                                final PsiElement indexExpression = index.getValue();
                                if (indexExpression instanceof StringLiteralExpression) {
                                    final String indexContent = ((StringLiteralExpression) indexExpression).getContents();
                                    if (mapping.containsKey(indexContent)) {
                                        final String replacement = String.format(
                                                "%s%s(%s, %s)",
                                                reference.getImmediateNamespaceName(),
                                                functionName,
                                                arguments[0].getText(),
                                                mapping.get(indexContent)
                                        );
                                        holder.registerProblem(
                                                context,
                                                ReportingUtil.wrapReportedMessage(String.format(messagePattern, replacement))
                                        );
                                    }
                                }
                            }
                        } else if (context instanceof AssignmentExpression) {
                            final AssignmentExpression assignment = (AssignmentExpression) context;
                            if (OpenapiTypesUtil.isAssignment(assignment)) {
                                final PsiElement container = assignment.getVariable();
                                if (container instanceof Variable) {
                                    final Function scope = ExpressionSemanticUtil.getScope(reference);
                                    if (scope != null) {
                                        final String variableName            = ((Variable) container).getName();
                                        final Collection<Variable> variables = PsiTreeUtil.findChildrenOfType(ExpressionSemanticUtil.getGroupStatement(scope), Variable.class).stream()
                                                .filter(v -> v != container && variableName.equals(v.getName()))
                                                .collect(Collectors.toList());
                                        if (! variables.isEmpty() && variables.stream().allMatch(v -> v.getParent() instanceof ArrayAccessExpression)) {
                                            final Set<String> indexes = new HashSet<>();
                                            for (final Variable variable : variables) {
                                                final ArrayIndex index = ((ArrayAccessExpression) variable.getParent()).getIndex();
                                                if (index != null) {
                                                    final PsiElement indexExpression = index.getValue();
                                                    if (indexExpression instanceof StringLiteralExpression) {
                                                        indexes.add(((StringLiteralExpression) indexExpression).getContents());
                                                        continue;
                                                    }
                                                }

                                                indexes.clear();
                                                break;
                                            }
                                            if (indexes.size() == 1) {
                                                final String indexContent = indexes.iterator().next();
                                                if (mapping.containsKey(indexContent)) {
                                                    final String replacement = String.format(
                                                            "%s%s(%s, %s)",
                                                            reference.getImmediateNamespaceName(),
                                                            functionName,
                                                            arguments[0].getText(),
                                                            mapping.get(indexContent)
                                                    );
                                                    holder.registerProblem(
                                                            reference,
                                                            ReportingUtil.wrapReportedMessage(String.format(messagePattern, replacement))
                                                    );
                                                }
                                            }
                                            indexes.clear();
                                            variables.clear();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
