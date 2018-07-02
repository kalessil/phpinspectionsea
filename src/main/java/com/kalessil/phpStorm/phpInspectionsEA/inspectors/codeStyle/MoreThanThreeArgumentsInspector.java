package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpTooManyParametersInspection;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Community request leaded to duplicating functionality of native PS inspection, so we just inherit it.
 *  see https://bitbucket.org/kalessil/phpinspectionsea/issues/289/allow-more-than-3-parameters-in
 */
public class MoreThanThreeArgumentsInspector extends PhpTooManyParametersInspection {
    @NotNull
    public String getShortName() {
        return "MoreThanThreeArgumentsInspection";
    }

    public MoreThanThreeArgumentsInspector() {
        limit = 3;
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new ProxyVisitor((PhpElementVisitor) super.buildVisitor(holder, isOnTheFly));
    }

    private static class ProxyVisitor extends BasePhpElementVisitor {
        final PhpElementVisitor visitor;

        ProxyVisitor(@NotNull PhpElementVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void visitPhpFunction(@NotNull Function function) {
            visitor.visitPhpFunction(function);
        }

        @Override
        public void visitPhpClass(@NotNull PhpClass clazz) {
            if (!this.isTestContext(clazz)) {
                visitor.visitPhpClass(clazz);
            }
        }

        @Override
        public void visitPhpMethod(@NotNull Method method) {
            if (!this.isTestContext(method)) {
                visitor.visitPhpMethod(method);
            }
        }
    }
}