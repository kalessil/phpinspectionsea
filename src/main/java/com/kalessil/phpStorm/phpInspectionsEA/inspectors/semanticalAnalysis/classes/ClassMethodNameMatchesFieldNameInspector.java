package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ClassMethodNameMatchesFieldNameInspector extends BasePhpInspection {
    private static final String messageMatches   = "There is a field with the same name, please give the method another name like is*, get*, set* and etc.";
    private static final String messageFieldType = "There is a field with the same name, but its type can not be resolved.";

    @NotNull
    @Override
    public String getShortName() {
        return "ClassMethodNameMatchesFieldNameInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Method name matches existing field name";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                final PhpClass clazz = method.getContainingClass();
                if (clazz != null && !clazz.isInterface()) {
                    final Field field = OpenapiResolveUtil.resolveField(clazz, method.getName());
                    if (field != null) {
                        final PsiElement nameNode  = NamedElementUtil.getNameIdentifier(method);
                        final PhpType resolvedType = OpenapiResolveUtil.resolveType(field, holder.getProject());
                        if (resolvedType != null && nameNode != null) {
                            final PhpType knownType = resolvedType.filterUnknown();
                            if (knownType.isEmpty()) {
                                holder.registerProblem(
                                        nameNode,
                                        MessagesPresentationUtil.prefixWithEa(messageFieldType)
                                );
                            } else {
                                final boolean isCallable = knownType.getTypes().stream().anyMatch(t -> Types.getType(t).equals(Types.strCallable));
                                if (isCallable) {
                                    holder.registerProblem(
                                            nameNode,
                                            MessagesPresentationUtil.prefixWithEa(messageMatches)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}