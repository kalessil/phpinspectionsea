package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FileSystemUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SenselessMethodDuplicationInspector extends BasePhpInspection {
    // configuration flags automatically saved by IDE
    @SuppressWarnings("WeakerAccess")
    public int MAX_METHOD_SIZE = 20;
    /* TODO: configurable via drop-down; clean code: 20 lines/method; PMD: 50; Checkstyle: 100 */

    private static final String messagePattern = "'%s%' method can be dropped, as it identical to parent's one";

    @NotNull
    public String getShortName() {
        return "SenselessMethodDuplicationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                /* process non-test and reportable classes only */
                final PhpClass clazz        = method.getContainingClass();
                final PsiElement methodName = NamedElementUtil.getNameIdentifier(method);
                final GroupStatement body   = ExpressionSemanticUtil.getGroupStatement(method);
                if (null == methodName || null == body || null == clazz|| FileSystemUtil.isTestClass(clazz)) {
                    return;
                }
                /* process only real classes and methods */
                if (clazz.isTrait() || clazz.isAnonymous() || clazz.isInterface() || method.isAbstract()) {
                    return;
                }

                /* don't take too heavy work */
                final int countExpressions = ExpressionSemanticUtil.countExpressionsInGroup(body);
                if (0 == countExpressions || countExpressions > MAX_METHOD_SIZE) {
                    return;
                }

                /* ensure parent, parent methods are existing and contains the same amount of expressions */
                final PhpClass parent           = clazz.getSuperClass();
                final Method parentMethod       = null == parent ? null : parent.findMethodByName(method.getName());
                final GroupStatement parentBody = null == parentMethod ? null : ExpressionSemanticUtil.getGroupStatement(parentMethod);
                if (null == parentBody || countExpressions != ExpressionSemanticUtil.countExpressionsInGroup(parentBody)) {
                    return;
                }

                /* iterate and compare expressions */
                PhpPsiElement ownExpression    = body.getFirstPsiChild();
                PhpPsiElement parentExpression = parentBody.getFirstPsiChild();
                for (int index = 0; index <= countExpressions; ++index) {
                    /* skip doc-blocks */
                    while (ownExpression instanceof PhpDocComment) {
                        ownExpression = ownExpression.getNextPsiSibling();
                    }
                    while (parentExpression instanceof PhpDocComment) {
                        parentExpression = parentExpression.getNextPsiSibling();
                    }
                    if (null == ownExpression || null == parentExpression) {
                        break;
                    }

                    /* process comparing 2 nodes */
                    if (!PsiEquivalenceUtil.areElementsEquivalent(ownExpression, parentExpression)) {
                        boolean mismatched = true;
                        /* PsiEquivalenceUtil.areElementsEquivalent is not handling assignments properly */
                        /* FIXME: ugly workaround / https://youtrack.jetbrains.com/issue/WI-34368 */
                        if (ownExpression.getTextLength() == parentExpression.getTextLength()) {
                            mismatched = !ownExpression.getText().equals(parentExpression.getText());
                        }

                        if (mismatched) {
                            return;
                        }
                    }
                    ownExpression    = ownExpression.getNextPsiSibling();
                    parentExpression = parentExpression.getNextPsiSibling();
                }

                final String message = messagePattern.replace("%s%", method.getName());
                holder.registerProblem(methodName, message, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
