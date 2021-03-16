package com.kalessil.phpStorm.phpInspectionsEA.openApi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public abstract class BasePhpElementVisitor extends PhpElementVisitor {
    @Override
    final public void visitPhpElement(@NotNull PhpPsiElement element) {
        if (element instanceof PhpDocTag) {
            this.visitPhpDocTag((PhpDocTag) element);
        } else if (element instanceof Declare) {
            this.visitPhpDeclare((Declare) element);
        } else if (element instanceof PhpEval) {
            this.visitPhpEval((PhpEval) element);
        } else if (element instanceof PhpShellCommandExpression) {
            this.visitPhpShellCommand((PhpShellCommandExpression) element);
        } else {
            if (OpenapiTypesUtil.is(element.getFirstChild(), PhpTokenTypes.kwTHROW)) {
                // PS 2020.3, 2021.1 has changed the throw structure, hence we have to rely on low-level structures.
                this.visitPhpThrow(element);
            } else {
                this.visitElement(element);
            }
        }
    }

    public void visitPhpDeclare(@NotNull Declare declare)                           {}
    public void visitPhpEval(@NotNull PhpEval eval)                                 {}
    public void visitPhpDocTag(@NotNull PhpDocTag tag)                              {}
    public void visitPhpShellCommand(@NotNull PhpShellCommandExpression expression) {}
    public void visitPhpThrow(@NotNull PhpPsiElement expression)                    {}

    /* overrides to reduce amount of 'com.jetbrains.php.lang.psi.visitors.PhpElementVisitor.visitElement' calls */
    @Override public void visitPhpFile(PhpFile PhpFile)        {}
    @Override public void visitWhiteSpace(PsiWhiteSpace space) {}

    @Override public void visitPhpMethodReference(MethodReference reference)                       {}
    @Override public void visitPhpFunctionCall(FunctionReference reference)                        {}
    @Override public void visitPhpConstantReference(ConstantReference reference)                   {}
    @Override public void visitPhpClassConstantReference(ClassConstantReference constantReference) {}

    @Override public void visitPhpForeach(ForeachStatement foreachStatement) {}
    @Override public void visitPhpFor(For forStatement)                      {}
    @Override public void visitPhpWhile(While whileStatement)                {}
    @Override public void visitPhpDoWhile(DoWhile doWhileStatement)          {}
    @Override public void visitPhpContinue(PhpContinue continueStatement)    {}

    @Override public void visitPhpTernaryExpression(TernaryExpression expression)                 {}
    @Override public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {}
    @Override public void visitPhpSelfAssignmentExpression(SelfAssignmentExpression expression)   {}
    @Override public void visitPhpParenthesizedExpression(ParenthesizedExpression expression)     {}
    @Override public void visitPhpMultiassignmentExpression(MultiassignmentExpression expression) {}
    @Override public void visitPhpUnaryExpression(UnaryExpression expression)                     {}

    @Override public void visitPhpIf(If ifStatement)             {}
    @Override public void visitPhpElseIf(ElseIf elseIfStatement) {}
    @Override public void visitPhpElse(Else elseStatement)       {}

    @Override public void visitPhpBinaryExpression(BinaryExpression expression) {}
    @Override public void visitPhpIsset(PhpIsset issetExpression)               {}
    @Override public void visitPhpUnset(PhpUnset unsetStatement)                {}
    @Override public void visitPhpEmpty(PhpEmpty emptyExpression)               {}
    @Override public void visitPhpNewExpression(NewExpression expression)       {}
    @Override public void visitPhpEchoStatement(PhpEchoStatement echo)          {}
    @Override public void visitPhpPrint(PhpPrintExpression print)               {}
    @Override public void visitPhpGroupStatement(GroupStatement groupStatement) {}
    @Override public void visitPhpInclude(Include include)                      {}
    @Override public void visitPhpGotoLabel(PhpGotoLabel label)                 {}
    @Override public void visitPhpUse(PhpUse expression)                        {}

    @Override public void visitPhpClass(PhpClass clazz)                                            {}
    @Override public void visitPhpMethod(Method method)                                            {}
    @Override public void visitPhpField(Field field)                                               {}
    @Override public void visitPhpFunction(Function function)                                      {}

    @Override public void visitPhpArrayCreationExpression(ArrayCreationExpression expression) {}
    @Override public void visitPhpArrayAccessExpression(ArrayAccessExpression expression)     {}
    @Override public void visitPhpStringLiteralExpression(StringLiteralExpression expression) {}

    @Override public void visitPhpTry(Try tryStatement)      {}
    @Override public void visitPhpCatch(Catch phpCatch)      {}
    @Override public void visitPhpFinally(Finally element)   {}

    @Override public void visitPhpSwitch(PhpSwitch switchStatement) {}
    @Override public void visitPhpReturn(PhpReturn returnStatement) {}

    protected boolean isTestContext(@NotNull PsiElement expression) {
        boolean result        = false;
        final String filePath = expression.getContainingFile().getVirtualFile().getPath();
        if (filePath.endsWith("Test.php") || filePath.endsWith("Spec.php") || filePath.endsWith(".phpt") || filePath.contains("/Fixtures/")) {
            result = true;
        } else {
            final PhpClass containingClass = expression instanceof PhpClass
                    ? (PhpClass) expression
                    : PsiTreeUtil.getParentOfType(expression, PhpClass.class, false, (Class) PsiFile.class);
            if (containingClass != null) {
                final String fqn = containingClass.getFQN();
                result = fqn.endsWith("Test") || fqn.contains("\\Tests\\") || fqn.contains("\\Test\\");
            }
        }
        return result;
    }

    protected boolean isFromRootNamespace(@NotNull FunctionReference reference) {
        boolean result            = false;
        final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
        if (resolved instanceof Function) {
            final Function function = (Function) resolved;
            result = function.getFQN().equals('\\' + function.getName());
        }
        return result && !(reference.getParent() instanceof PhpUse);
    }
}
