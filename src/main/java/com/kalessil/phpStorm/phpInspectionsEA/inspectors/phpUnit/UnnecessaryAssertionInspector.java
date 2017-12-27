package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class UnnecessaryAssertionInspector extends BasePhpInspection {
    private final static String message = "...";

    final private static Map<String, Integer> targets = new HashMap<>();
    static {
        targets.put("assertInstanceOf",   1);
        targets.put("assertEmpty",        0);
        targets.put("assertNull",         0);
        targets.put("assertInternalType", 1);
    }

    @NotNull
    public String getShortName() {
        return "UnnecessaryAssertionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                final String methodName = reference.getName();
                if (methodName != null && targets.containsKey(methodName)) {
                    final int position           = targets.get(methodName);
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length >= position + 1 && arguments[position] instanceof FunctionReference) {
                        final FunctionReference call      = (FunctionReference) arguments[position];
                        final PsiElement resolvedFunction = OpenapiResolveUtil.resolveReference(call);
                        if (resolvedFunction instanceof Function) {
                            final boolean hasReturnType = OpenapiElementsUtil.getReturnType((Function) resolvedFunction) != null;
                            if (hasReturnType) {
                                final PhpType type = OpenapiResolveUtil.resolveType(call, holder.getProject());
                                if (type != null && !type.hasUnknown() && type.size() == 1) {
                                    holder.registerProblem(reference, message);
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
