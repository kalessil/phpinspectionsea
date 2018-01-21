package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class TypesCastingCanBeUsedInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' should be used instead (up to 6x times faster).";

    @NotNull
    public String getShortName() {
        return "TypesCastingCanBeUsedInspection";
    }

    private static final HashMap<String, String> functionsMapping = new HashMap<>();
    private static final HashMap<String, String> typesMapping     = new HashMap<>();
    static {
        functionsMapping.put("intval",   "int");
        functionsMapping.put("floatval", "float");
        functionsMapping.put("strval",   "string");
        functionsMapping.put("boolval",  "bool");
        functionsMapping.put("settype",  null);

        typesMapping.put("boolean", "bool");
        typesMapping.put("bool",    "bool");
        typesMapping.put("integer", "int");
        typesMapping.put("int",     "int");
        typesMapping.put("float",   "float");
        typesMapping.put("double",  "float");
        typesMapping.put("string",  "string");
        typesMapping.put("array",   "array");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionsMapping.containsKey(functionName)) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (functionName.equals("settype")) {
                        final boolean isTarget = arguments.length == 2 && arguments[1] instanceof StringLiteralExpression;
                        if (isTarget && this.isFromRootNamespace(reference)) {
                            final String type = ((StringLiteralExpression) arguments[1]).getContents();
                            if (typesMapping.containsKey(type) && OpenapiTypesUtil.isStatementImpl(reference.getParent())) {
                                final String replacement = String.format(
                                        "%s = (%s) %s",
                                        arguments[0].getText(),
                                        typesMapping.get(type),
                                        arguments[0].getText()
                                );
                                holder.registerProblem(
                                        reference,
                                        String.format(messagePattern, replacement),
                                        ProblemHighlightType.LIKE_DEPRECATED,
                                        new UseTypeCastingFix(replacement)
                                );
                            }
                        }
                    } else {
                        final boolean isTarget = arguments.length == 1;
                        if (isTarget) {
                            final String replacement = String.format(
                                    "(%s) %s",
                                    functionsMapping.get(functionName),
                                    arguments[0].getText()
                            );
                            holder.registerProblem(
                                    reference,
                                    String.format(messagePattern, replacement),
                                    ProblemHighlightType.LIKE_DEPRECATED,
                                    new UseTypeCastingFix(replacement)
                            );
                        }
                    }
                }
            }

            @Override
            public void visitPhpStringLiteralExpression(@NotNull StringLiteralExpression literal) {
                if (!literal.isHeredoc() && !(ExpressionSemanticUtil.getBlockScope(literal) instanceof PhpDocComment)) {
                    final PsiElement[] children = literal.getChildren();
                    if (children.length == 1) {
                        final boolean isTarget =
                                children[0].getPrevSibling() == literal.getFirstChild() &&
                                children[0].getNextSibling() == literal.getLastChild();
                        if (isTarget) {
                            final String replacement = String.format("(string) %s", children[0].getText());
                            holder.registerProblem(
                                    literal,
                                    String.format(messagePattern, replacement),
                                    new UseTypeCastingFix(replacement)
                            );
                        }
                    }
                }
            }
        };
    }

    private static class UseTypeCastingFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use type casting instead";
        }

        UseTypeCastingFix(@NotNull String expression) {
            super(expression);
        }
    }
}