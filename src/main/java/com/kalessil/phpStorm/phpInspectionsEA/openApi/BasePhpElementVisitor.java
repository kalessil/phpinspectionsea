package com.kalessil.phpStorm.phpInspectionsEA.openApi;

import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
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
    @Override
    public void visitPhpMethodReference(MethodReference reference) {}
    @Override
    public void visitPhpFunctionCall(FunctionReference reference)  {}
}
