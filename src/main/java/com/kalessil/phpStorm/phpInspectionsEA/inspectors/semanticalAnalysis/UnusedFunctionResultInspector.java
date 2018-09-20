package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class UnusedFunctionResultInspector extends BasePhpInspection {
    private static final String message = "Function result is not used.";

    @NotNull
    public String getShortName() {
        return "UnusedFunctionResultInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (EAUltimateApplicationComponent.areFeaturesEnabled()) {
                    final boolean isTargetContext = OpenapiTypesUtil.isStatementImpl(reference.getParent());
                    if (isTargetContext) {
                        final PhpType resolved = OpenapiResolveUtil.resolveType(reference, reference.getProject());
                        if (resolved != null) {
                            final Set<String> types = resolved.filterUnknown().getTypes().stream()
                                    .map(Types::getType)
                                    .collect(Collectors.toSet());
                            types.remove(Types.strBoolean); /* APIs returning false on failures */
                            types.remove(Types.strInteger); /* APIs returning c-alike result codes */
                            if (!types.isEmpty()) {
                                final PsiElement target = NamedElementUtil.getNameIdentifier(reference);
                                if (target != null) {
                                    holder.registerProblem(target, message);
                                }
                            }
                            types.clear();
                        }
                    }
                }
            }
        };
    }
}
