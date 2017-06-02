package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

public class MockingFinalClassesInspector extends BasePhpInspection {
    private final static String message = "Causes reflection errors as the referenced class is final.";

    private final static Map<String, String> methods = new HashMap<>();
    static {
        methods.put("\\PHPUnit_Framework_TestCase.getMockBuilder",   "getMockBuilder");
        methods.put("\\PHPUnit\\Framework\\TestCase.getMockBuilder", "getMockBuilder");
        methods.put("\\PhpSpec\\ObjectBehavior.shouldHaveType",      "shouldHaveType");
        methods.put("\\PhpSpec\\ObjectBehavior.shouldNotHaveType",   "shouldNotHaveType");
    }

    @NotNull
    public String getShortName() {
        return "MockingFinalClassesInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                final String methodName      = reference.getName();
                final PsiElement[] arguments = reference.getParameters();
                if (methodName != null && arguments.length == 1 && methods.containsValue(methodName)) {
                    final PsiElement resolved = reference.resolve();
                    if (resolved instanceof Method && methods.get(((Method) resolved).getFQN()) != null) {
                        final PhpClass referencedClass = this.getClass(arguments[0]);
                        if (referencedClass != null && referencedClass.isFinal()) {
                            holder.registerProblem(arguments[0], message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                }
            }

            @Nullable
            private PhpClass getClass(@NotNull PsiElement expression) {
                PhpClass result = null;
                if (expression instanceof ClassConstantReference) {
                    final ClassConstantReference reference = (ClassConstantReference) expression;
                    final String constantName              = reference.getName();
                    if (constantName != null && constantName.equals("class")) {
                        final PhpExpression classReference = reference.getClassReference();
                        if (classReference instanceof ClassReference) {
                            final PsiElement resolved = ((ClassReference) classReference).resolve();
                            result = resolved instanceof PhpClass ? (PhpClass) resolved : null;
                        }
                    }
                } else if (expression instanceof StringLiteralExpression) {
                    final StringLiteralExpression string = (StringLiteralExpression) expression;
                    final String contents                = string.getContents();
                    if (string.getFirstPsiChild() == null && contents.length() > 3) {
                        String fqn = contents.replaceAll("\\\\\\\\", "\\\\");
                        fqn        = fqn.charAt(0) == '\\' ? fqn : "\\" + fqn;
                        for (PhpClass clazz : PhpIndex.getInstance(expression.getProject()).getClassesByFQN(fqn)) {
                            if (clazz.isFinal()) {
                                result = clazz;
                                break;
                            }
                        }
                    }
                }
                /* TODO: handle __NAMESPACE__.'\Class' */
                return result;
            }

        };
    }
}
