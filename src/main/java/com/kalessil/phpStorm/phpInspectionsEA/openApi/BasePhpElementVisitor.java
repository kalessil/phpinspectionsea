package com.kalessil.phpStorm.phpInspectionsEA.openApi;

import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpEval;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.NotNull;

public abstract class BasePhpElementVisitor extends PhpElementVisitor {

    /** adds PhpEval visitor */
    @Override
    final public void visitPhpElement(PhpPsiElement element) {
        if (element instanceof PhpEval) {
            visitPhpEval((PhpEval) element);
            return;
        }

        if (element instanceof PhpDocTag) {
            visitPhpDocTag((PhpDocTag) element);
            return;
        }

        this.visitElement(element);
    }

    public void visitPhpEval(@NotNull PhpEval eval) {
        this.visitElement(eval);
    }

    public void visitPhpDocTag(@NotNull PhpDocTag tag) {
        this.visitElement(tag);
    }
}
