package com.kalessil.phpStorm.phpInspectionsEA.openApi;

import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.NotNull;

public abstract class BasePhpElementVisitor extends PhpElementVisitor {
    @Override
    final public void visitPhpElement(@NotNull PhpPsiElement element) {
        if (element instanceof PhpEval) {
            this.visitPhpEval((PhpEval) element);
        } else if (element instanceof PhpDocTag) {
            this.visitPhpDocTag((PhpDocTag) element);
        } else if (element instanceof Declare) {
            this.visitPhpDeclare((Declare) element);
        } else {
            this.visitElement(element);
        }
    }

    public void visitPhpDeclare(@NotNull Declare declare) {}
    public void visitPhpEval(@NotNull PhpEval eval)       {}
    public void visitPhpDocTag(@NotNull PhpDocTag tag)    {}

    /* overrides to reduce amount of 'com.jetbrains.php.lang.psi.visitors.PhpElementVisitor.visitElement' calls */
    @Override public void visitPhpFile(PhpFile PhpFile) {}

    @Override public void visitPhpMethodReference(MethodReference reference) {}
    @Override public void visitPhpFunctionCall(FunctionReference reference)  {}

    @Override public void visitPhpForeach(ForeachStatement foreachStatement) {}
    @Override public void visitPhpFor(For forStatement)                      {}
    @Override public void visitPhpWhile(While whileStatement)                {}
    @Override public void visitPhpDoWhile(DoWhile doWhileStatement)          {}

    @Override public void visitPhpTernaryExpression(TernaryExpression expression)                 {}
    @Override public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {}
    @Override public void visitPhpSelfAssignmentExpression(SelfAssignmentExpression expression)   {}
    @Override public void visitPhpParenthesizedExpression(ParenthesizedExpression expression)     {}

    @Override public void visitPhpIf(If ifStatement)             {}
    @Override public void visitPhpElseIf(ElseIf elseIfStatement) {}
    @Override public void visitPhpElse(Else elseStatement)       {}

    @Override public void visitPhpIsset(PhpIsset issetExpression) {}
    @Override public void visitPhpUnset(PhpUnset unsetStatement)  {}
    @Override public void visitPhpEmpty(PhpEmpty emptyExpression) {}

    @Override public void visitPhpClass(PhpClass clazz)                          {}
    @Override public void visitPhpConstantReference(ConstantReference reference) {}

    @Override public void visitPhpArrayCreationExpression(ArrayCreationExpression expression) {}
    @Override public void visitPhpArrayAccessExpression(ArrayAccessExpression expression)     {}

    @Override public void visitPhpTry(Try tryStatement)    {}
    @Override public void visitPhpCatch(Catch phpCatch)    {}
    @Override public void visitPhpFinally(Finally element) {}

    @Override public void visitPhpSwitch(PhpSwitch switchStatement) {}
}
