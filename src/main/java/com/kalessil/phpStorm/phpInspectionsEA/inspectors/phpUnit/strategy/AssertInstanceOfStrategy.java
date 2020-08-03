package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class AssertInstanceOfStrategy {
    private final static Map<String, String> binaryTargetMapping   = new HashMap<>();
    private final static Map<String, String> getClassTargetMapping = new HashMap<>();
    static {
        binaryTargetMapping.put("assertFalse",    "assertNotInstanceOf");
        binaryTargetMapping.put("assertNotTrue",  "assertNotInstanceOf");
        binaryTargetMapping.put("assertTrue",     "assertInstanceOf");
        binaryTargetMapping.put("assertNotFalse", "assertInstanceOf");

        getClassTargetMapping.put("assertSame",      "assertInstanceOf");
        getClassTargetMapping.put("assertEquals",    "assertInstanceOf");
        getClassTargetMapping.put("assertNotSame",   "assertNotInstanceOf");
        getClassTargetMapping.put("assertNotEquals", "assertNotInstanceOf");
    }

    private final static String messagePattern = "'%s(...)' would fit more here.";

    static public boolean apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (binaryTargetMapping.containsKey(methodName)) {
            final PsiElement[] arguments = reference.getParameters();
            if (arguments.length > 0 && arguments[0] instanceof BinaryExpression) {
                final BinaryExpression binary = (BinaryExpression) arguments[0];
                if (binary.getOperationType() == PhpTokenTypes.kwINSTANCEOF) {
                    final PsiElement subject = binary.getLeftOperand();
                    final PsiElement clazz   = binary.getRightOperand();
                    if (subject != null && clazz != null) {
                        /* prepare class definition which can be used for QF-ing */
                        String classDefinition = clazz.getText();
                        if (clazz instanceof ClassReference) {
                            if (PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP550)) {
                                classDefinition = clazz.getText() + "::class";
                            } else {
                                final String fqn = ((ClassReference) clazz).getFQN();
                                if (fqn != null) {
                                    classDefinition = '\'' + fqn.replaceAll("\\\\", "\\\\\\\\") + '\'';
                                }
                            }
                        }
                        /* report and provide QF */
                        final String suggestedAssertion   = binaryTargetMapping.get(methodName);
                        final String[] suggestedArguments = new String[arguments.length + 1];
                        suggestedArguments[0]             = classDefinition;
                        suggestedArguments[1]             = subject.getText();
                        if (arguments.length > 1) {
                            suggestedArguments[2] = arguments[1].getText();
                        }
                        holder.registerProblem(
                                reference,
                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), suggestedAssertion),
                                new PhpUnitAssertFixer(suggestedAssertion, suggestedArguments)
                        );
                        result = true;
                    }
                }
            }
        } else if (getClassTargetMapping.containsKey(methodName)) {
            final PsiElement[] arguments = reference.getParameters();
            if (arguments.length >= 2) {
                final PsiElement literal = arguments[0] instanceof StringLiteralExpression ? arguments[0] : arguments[1];
                if (literal instanceof StringLiteralExpression) {
                    final StringLiteralExpression string = (StringLiteralExpression) literal;
                    final String contents                = string.getContents();
                    if (string.getFirstPsiChild() == null && contents.length() > 3) {
                        final PsiElement call = literal == arguments[0] ? arguments[1] : arguments[0];
                        if (OpenapiTypesUtil.isFunctionReference(call)) {
                            final FunctionReference candidate = (FunctionReference) call;
                            final String functionName         = candidate.getName();
                            if (functionName != null && functionName.equals("get_class")) {
                                final PsiElement[] innerArguments = candidate.getParameters();
                                if (innerArguments.length == 1) {
                                    /* prepare class definition which can be used for QF-ing */
                                    final String fqn = '\\' + contents.replaceAll("\\\\\\\\", "\\\\");
                                    final String classDefinition;
                                    if (PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP550)) {
                                        classDefinition = fqn + "::class";
                                    } else {
                                        classDefinition = '\'' + fqn.replaceAll("\\\\", "\\\\\\\\") + '\'';
                                    }
                                    /* report and provide QF */
                                    final String suggestedAssertion   = getClassTargetMapping.get(methodName);
                                    final String[] suggestedArguments = new String[arguments.length];
                                    suggestedArguments[0]             = classDefinition;
                                    suggestedArguments[1]             = innerArguments[0].getText();
                                    if (arguments.length > 2) {
                                        suggestedArguments[2] = arguments[2].getText();
                                    }
                                    holder.registerProblem(
                                            reference,
                                            String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), suggestedAssertion),
                                            new PhpUnitAssertFixer(suggestedAssertion, suggestedArguments)
                                    );
                                    result = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
