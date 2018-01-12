package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.PhpUnitAssertFixer;
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

    private final static Map<String, String> targetMapping = new HashMap<>();
    static {
        targetMapping.put("assertFalse",    "assertNotInstanceOf");
        targetMapping.put("assertNotTrue",  "assertNotInstanceOf");
        targetMapping.put("assertTrue",     "assertInstanceOf");
        targetMapping.put("assertNotFalse", "assertInstanceOf");
    }

    private final static String messagePattern = "'%s(...)' would fit more here.";

    static public boolean apply(@NotNull String methodName, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (targetMapping.containsKey(methodName)) {
            final PsiElement[] arguments = reference.getParameters();
            if (arguments.length > 0 && arguments[0] instanceof BinaryExpression) {
                final BinaryExpression binary = (BinaryExpression) arguments[0];
                if (binary.getOperationType() == PhpTokenTypes.kwINSTANCEOF) {
                    final PsiElement subject = binary.getLeftOperand();
                    final PsiElement clazz   = binary.getRightOperand();
                    if (subject != null && clazz != null) {
                        /* prepare class definition which can be used for QF-ing */
                        final Project project      = reference.getProject();
                        PsiElement classDefinition = clazz;
                        if (classDefinition instanceof ClassReference) {
                            final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel();
                            if (php.hasFeature(PhpLanguageFeature.CLASS_NAME_CONST)) {
                                classDefinition = PhpPsiElementFactory.createPhpPsiFromText(
                                        project,
                                        ClassConstantReference.class,
                                        classDefinition.getText() + "::class"
                                );
                            } else {
                                final String fqn = ((ClassReference) classDefinition).getFQN();
                                if (fqn != null) {
                                    classDefinition = PhpPsiElementFactory.createPhpPsiFromText(
                                            project,
                                            StringLiteralExpression.class,
                                            "'" + fqn.replaceAll("\\\\", "\\\\\\\\") + "'"
                                    );
                                }
                            }
                        }
                        /* report and provide QF */
                        final String[] suggestedArguments = new String[arguments.length + 1];
                        suggestedArguments[0] = classDefinition.getText();
                        suggestedArguments[1] = subject.getText();
                        if (arguments.length > 1) {
                            suggestedArguments[2] = arguments[1].getText();
                        }

                        final String suggestedAssertion = targetMapping.get(methodName);
                        final String message            = String.format(messagePattern, suggestedAssertion);
                        holder.registerProblem(reference, message, new PhpUnitAssertFixer(suggestedAssertion, suggestedArguments));

                        result = true;
                    }
                }
            }
        }
        return result;
    }
}
