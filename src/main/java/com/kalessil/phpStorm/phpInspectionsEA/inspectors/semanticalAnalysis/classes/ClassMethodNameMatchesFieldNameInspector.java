package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
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

public class ClassMethodNameMatchesFieldNameInspector extends PhpInspection {
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
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.shouldSkipAnalysis(method, StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE)) { return; }

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
                                        ReportingUtil.wrapReportedMessage(messageFieldType)
                                );
                            } else {
                                final boolean isCallable = knownType.getTypes().stream().anyMatch(t -> Types.getType(t).equals(Types.strCallable));
                                if (isCallable) {
                                    holder.registerProblem(
                                            nameNode,
                                            ReportingUtil.wrapReportedMessage(messageMatches)
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