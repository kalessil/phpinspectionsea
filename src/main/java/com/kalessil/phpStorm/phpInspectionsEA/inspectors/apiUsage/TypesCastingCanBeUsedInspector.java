package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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
    private static final String messagePattern  = "'%s' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).";
    private static final String messageInlining = "'%s' would express the intention here better (less types coercion magic).";
    private static final String messageMagic    = "'%s' would express the intention here better.";

    // Inspection options.
    public boolean REPORT_INLINES                = true;
    public boolean REPORT_TO_STRING_METHOD_CALLS = false;

    @NotNull
    @Override
    public String getShortName() {
        return "TypesCastingCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Type casting can be used";
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
                                        String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                        ProblemHighlightType.LIKE_DEPRECATED,
                                        new UseTypeCastingFix(replacement)
                                );
                            }
                        }
                    } else {
                        boolean isTarget = arguments.length == 1;
                        if (! isTarget && arguments.length == 2 && functionName.equals("intval")) {
                            final PsiElement base = arguments[1];
                            isTarget              = OpenapiTypesUtil.isNumber(base) && base.getText().equals("10");
                        }

                        if (isTarget) {
                            final boolean wrapArgument = arguments[0] instanceof BinaryExpression ||
                                                         arguments[0] instanceof TernaryExpression;
                            final String replacement = String.format(
                                    "(%s) %s",
                                    functionsMapping.get(functionName),
                                    String.format(wrapArgument ? "(%s)" : "%s", arguments[0].getText())
                            );
                            holder.registerProblem(
                                    reference,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                    ProblemHighlightType.LIKE_DEPRECATED,
                                    new UseTypeCastingFix(replacement)
                            );
                        }
                    }
                }
            }

            @Override
            public void visitPhpStringLiteralExpression(@NotNull StringLiteralExpression literal) {
                if (REPORT_INLINES && OpenapiTypesUtil.isString(literal) && !literal.isHeredoc()) {
                    final PsiElement[] children = literal.getChildren();
                    if (children.length == 1) {
                        final boolean isTarget = children[0].getPrevSibling() == literal.getFirstChild() &&
                                                 children[0].getNextSibling() == literal.getLastChild();
                        if (isTarget) {
                            final PsiElement candidate = children[0].getFirstChild();
                            final boolean isWrapped    = OpenapiTypesUtil.is(candidate, PhpTokenTypes.chLBRACE);
                            final String replacement   = String.format(
                                    isWrapped ? "(string) (%s)" : "(string) %s",
                                    (isWrapped ? candidate.getNextSibling() : children[0]).getText()
                            );
                            holder.registerProblem(
                                    literal,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messageInlining), replacement),
                                    new UseTypeCastingFix(replacement)
                            );
                        }
                    }
                }
            }

            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                if (REPORT_TO_STRING_METHOD_CALLS) {
                    final String methodName = reference.getName();
                    if (methodName != null && methodName.equals("__toString")) {
                        final PsiElement base = reference.getFirstChild();
                        if (!(base instanceof ClassReference) || !base.getText().equals("parent")) {
                            final String replacement = String.format("(string) %s", base.getText());
                            holder.registerProblem(
                                    reference,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messageMagic), replacement),
                                    new UseTypeCastingFix(replacement)
                            );
                        }
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create(component -> {
            component.addCheckbox("Report \"$inlined\" cases", REPORT_INLINES, (isSelected) -> REPORT_INLINES = isSelected);
            component.addCheckbox("Report '->__toString()' calls", REPORT_TO_STRING_METHOD_CALLS, (isSelected) -> REPORT_TO_STRING_METHOD_CALLS = isSelected);
        });
    }

    private static final class UseTypeCastingFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use type casting instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseTypeCastingFix(@NotNull String expression) {
            super(expression);
        }
    }
}