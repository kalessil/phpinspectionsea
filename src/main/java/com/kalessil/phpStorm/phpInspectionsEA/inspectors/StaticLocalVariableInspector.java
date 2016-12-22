package com.kalessil.phpStorm.phpInspectionsEA.inspectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FileSystemUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StaticLocalVariableInspector extends BasePhpInspection {

    @NotNull
    public String getShortName() {
        return "StaticLocalVariableInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                /* common expectations regarding the method and class */
                final PhpClass clazz = method.getContainingClass();
                if (null == clazz || clazz.isInterface() || method.isAbstract() || FileSystemUtil.isTestClass(clazz)) {
                    return;
                }
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                if (null == body || 0 == ExpressionSemanticUtil.countExpressionsInGroup(body)) {
                    return;
                }

                /* Filtering step 1: variables assigned with array without dynamic parts */
                final List<Variable> candidates = new ArrayList<>();
                for (AssignmentExpression expression : PsiTreeUtil.findChildrenOfType(body, AssignmentExpression.class)) {
                    /* check if a variable has been assigned a non-empty array */
                    final PhpPsiElement variable = expression.getVariable();
                    final PhpPsiElement value    = expression.getValue();
                    if (
                        !(variable instanceof Variable) || !(value instanceof ArrayCreationExpression) ||
                        !OpenapiTypesUtil.isAssignment(expression) || null == value.getFirstPsiChild()
                    ) {
                        continue;
                    }

                    /* analyze injections, ensure that only static content used */
                    boolean canBeStatic = true;
                    for (PhpReference injection : PsiTreeUtil.findChildrenOfType(value, PhpReference.class)) {
                        if (
                            injection instanceof ConstantReference || injection instanceof ClassConstantReference ||
                            injection instanceof ArrayCreationExpression
                        ) {
                            continue;
                        }

                        canBeStatic = false;
                        break;
                    }
                    if (!canBeStatic) {
                        continue;
                    }

                    /* store a variable, uniqueness is not checked here */
                    candidates.add((Variable) variable);
                }


                /* Filtering step 2: only unique variables from candidates which are not parameters */
                final List<Variable> filteredCandidates = new ArrayList<>();
                final Set<String> paramsNames           = new HashSet<>();
                for (Parameter param : method.getParameters()) {
                    paramsNames.add(param.getName());
                }
                for (Variable variable : candidates) {
                    if (paramsNames.contains(variable.getName())) {
                        continue;
                    }

                    boolean isDuplicated = false;
                    for (Variable possibleDuplicate : candidates) {
                        if (variable != possibleDuplicate && PsiEquivalenceUtil.areElementsEquivalent(variable, possibleDuplicate)) {
                            isDuplicated = true;
                            break;
                        }
                    }
                    if (isDuplicated) {
                        continue;
                    }

                    filteredCandidates.add(variable);
                }
                paramsNames.clear();
                candidates.clear();


                /* Analysis itself (sub-routine): variable is used in read context, no dispatching by reference */
            }
        };
    }
}
