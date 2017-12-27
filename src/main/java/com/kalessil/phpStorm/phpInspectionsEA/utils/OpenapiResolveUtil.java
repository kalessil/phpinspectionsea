package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

        /*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class OpenapiResolveUtil {
    @Nullable
    static public PsiElement resolveReference(@NotNull PsiReference reference) {
        try {
            return reference.resolve();
        } catch (Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return null;
        }
    }

    @Nullable
    static public PhpType resolveType(@NotNull PhpTypedElement expression, @NotNull Project project) {
        try {
            return expression.getType().global(project);
        } catch (Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return null;
        }
    }

    @NotNull
    static public Collection<PhpClass> resolveClassesByFQN(@NotNull String name, @NotNull PhpIndex index) {
        try {
            return index.getClassesByFQN(name);
        } catch (Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return new ArrayList<>();
        }
    }

    @NotNull
    static public Collection<PhpClass> resolveInterfacesByFQN(@NotNull String name, @NotNull PhpIndex index) {
        try {
            return index.getInterfacesByFQN(name);
        } catch (Throwable error) {
            if (error instanceof ProcessCanceledException) {
                throw error;
            }
            return new ArrayList<>();
        }
    }
}
