package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
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

public class UnsupportedSerializeTypesInspector extends PhpInspection {
    private static final String messagePattern = "'%s' doesn't support serialization (causes serialization is not allowed error).";

    private static final Set<String> targetClasses = new HashSet<>();
    static {
        targetClasses.add("\\SimpleXMLElement");
        targetClasses.add("\\HashContext");
        targetClasses.add("\\Closure");
        targetClasses.add("\\SplFileInfo");
        targetClasses.add("\\Generator");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "UnsupportedSerializeTypesInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Unsupported serialize types";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("serialize")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1 && arguments[0] instanceof PhpTypedElement && isFromRootNamespace(reference)) {
                        final PhpType type = OpenapiResolveUtil.resolveType((PhpTypedElement) arguments[0], holder.getProject());
                        if (type != null) {
                            final List<String> foundTypes = type.filterUnknown().getTypes().stream()
                                    .filter(targetClasses::contains)
                                    .collect(Collectors.toList());
                            if (!foundTypes.isEmpty()) {
                                holder.registerProblem(
                                        arguments[0],
                                        String.format(messagePattern, foundTypes.get(0)),
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
