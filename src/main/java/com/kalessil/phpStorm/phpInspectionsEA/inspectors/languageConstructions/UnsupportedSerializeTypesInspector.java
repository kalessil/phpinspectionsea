package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnsupportedSerializeTypesInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' doesn't support serialization (caused serialization is not allowed error).";

    private static final Set<String> targetClasses = new HashSet<>();
    static {
        targetClasses.add("\\SimpleXMLElement");
        targetClasses.add("\\HashContext");
        targetClasses.add("\\Closure");
        targetClasses.add("\\SplFileInfo");
        targetClasses.add("\\Generator");
    }

    @NotNull
    public String getShortName() {
        return "UnsupportedSerializeTypesInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("serialize")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > 0 && arguments[0] instanceof PhpTypedElement && isFromRootNamespace(reference)) {
                        final PhpType type = OpenapiResolveUtil.resolveType((PhpTypedElement) arguments[0], holder.getProject());
                        if (type != null) {
                            final List<String> foundTypes = type.filterUnknown().getTypes().stream()
                                    .filter(t -> targetClasses.contains(Types.getType(t)))
                                    .collect(Collectors.toList());
                            if (!foundTypes.isEmpty()) {
                                holder.registerProblem(
                                        reference,
                                        String.format(messagePattern, Types.getType(foundTypes.get(0))),
                                        ProblemHighlightType.GENERIC_ERROR
                                );
                                foundTypes.clear();
                            }
                        }
                    }
                }
            }
        };
    }
}
