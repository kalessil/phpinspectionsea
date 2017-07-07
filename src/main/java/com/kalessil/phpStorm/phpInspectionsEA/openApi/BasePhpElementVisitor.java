package com.kalessil.phpStorm.phpInspectionsEA.openApi;

import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.Declare;
import com.jetbrains.php.lang.psi.elements.PhpEval;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
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

    public void visitPhpDeclare(@NotNull Declare declare) {
        this.visitElement(declare);
    }

    public void visitPhpEval(@NotNull PhpEval eval) {
        this.visitElement(eval);
    }

    public void visitPhpDocTag(@NotNull PhpDocTag tag) {
        this.visitElement(tag);
    }
}
