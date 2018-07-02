package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
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
        /* PhpUnit-related */
        methods.put("\\PHPUnit_Framework_TestCase.getMockBuilder",        "getMockBuilder");
        methods.put("\\PHPUnit_Framework_TestCase.getMock",               "getMock");
        methods.put("\\PHPUnit_Framework_TestCase.getMockClass",          "getMockClass");
        methods.put("\\PHPUnit_Framework_MockObject_Generator.getMock",   "getMock");
        methods.put("\\PHPUnit_Framework_MockObject_MockBuilder.getMock", "getMock");
        methods.put("\\PHPUnit\\Framework\\TestCase.getMockBuilder",      "getMockBuilder");
        methods.put("\\PHPUnit\\Framework\\TestCase.getMockClass",        "getMockClass");
        /* PhpSpec-related */
        methods.put("\\Prophecy\\Prophet.prophesize",                     "prophesize");
        methods.put("\\Prophecy\\Prophecy\\ObjectProphecy.willExtend",    "willExtend");
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
            public void visitPhpClazz(@NotNull PhpClass clazz) {
                final PhpClass parent = OpenapiResolveUtil.resolveSuperClass(clazz);
                if (parent != null && parent.getFQN().equals("\\PhpSpec\\ObjectBehavior")) {
                    for (final Method method : clazz.getOwnMethods()) {
                        for (final Parameter parameter : method.getParameters()) {
                            final PsiElement typeCandidate = parameter.getFirstPsiChild();
                            if (typeCandidate instanceof ClassReference) {
                                final PsiElement resolved = OpenapiResolveUtil.resolveReference((ClassReference) typeCandidate);
                                if (resolved instanceof PhpClass && ((PhpClass) resolved).isFinal()) {
                                    holder.registerProblem(typeCandidate, message);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                final String methodName      = reference.getName();
                final PsiElement[] arguments = reference.getParameters();
                if (methodName != null && arguments.length > 0 && methods.containsValue(methodName)) {
                    final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                    if (resolved instanceof Method && methods.get(((Method) resolved).getFQN()) != null) {
                        final PhpClass referencedClass = this.getClass(arguments[0]);
                        if (referencedClass != null && referencedClass.isFinal()) {
                            holder.registerProblem(arguments[0], message);
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
                            final PsiElement resolved = OpenapiResolveUtil.resolveReference((ClassReference) classReference);
                            result = resolved instanceof PhpClass ? (PhpClass) resolved : null;
                        }
                    }
                } else if (expression instanceof StringLiteralExpression) {
                    final StringLiteralExpression string = (StringLiteralExpression) expression;
                    final String contents                = string.getContents();
                    if (string.getFirstPsiChild() == null && contents.length() > 3) {
                        String fqn           = contents.replaceAll("\\\\\\\\", "\\\\");
                        fqn                  = fqn.charAt(0) == '\\' ? fqn : '\\' + fqn;
                        final PhpIndex index = PhpIndex.getInstance(expression.getProject());
                        for (final PhpClass clazz : OpenapiResolveUtil.resolveClassesByFQN(fqn, index)) {
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
