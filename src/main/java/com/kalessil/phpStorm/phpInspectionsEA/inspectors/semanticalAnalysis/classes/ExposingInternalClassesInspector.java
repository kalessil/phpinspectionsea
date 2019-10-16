package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.indexers.InternalAnnotatedClassesIndexer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
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

public class ExposingInternalClassesInspector extends PhpInspection {
    private static final String message = "Exposes an @internal class, which should not be exposed via public methods.";

    @NotNull
    @Override
    public String getShortName() {
        return "ExposingInternalClassesInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Exposed @internal classes";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (this.shouldSkipAnalysis(clazz, StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE)) { return; }

                if (!clazz.isInterface() && !clazz.isTrait() && !clazz.isInternal() && !this.isTestContext(clazz)) {
                    final FileBasedIndex index    = FileBasedIndex.getInstance();
                    final GlobalSearchScope scope = GlobalSearchScope.allScope(holder.getProject());
                    methods: for (final Method method : clazz.getOwnMethods()) {
                        if (method.getAccess().isPublic()) {
                            /* case: parameter type declaration */
                            for (final Parameter parameter: method.getParameters()) {
                                final PhpType type = OpenapiResolveUtil.resolveDeclaredType(parameter);
                                if (!type.isEmpty() && this.isReferencingInternal(type, index, scope)) {
                                    holder.registerProblem(
                                            parameter,
                                            ReportingUtil.wrapReportedMessage(message)
                                    );
                                    continue methods;
                                }
                            }
                            /* case: return type declaration */
                            final PsiElement returnTypeHint = OpenapiElementsUtil.getReturnType(method);
                            if (returnTypeHint != null) {
                                final PhpType type = method.getType();
                                if (!type.isEmpty() && this.isReferencingInternal(type, index, scope)) {
                                    holder.registerProblem(
                                            returnTypeHint,
                                            ReportingUtil.wrapReportedMessage(message)
                                    );
                                }
                            }
                        }
                    }
                }
            }

            private boolean isReferencingInternal(@NotNull PhpType type, @NotNull FileBasedIndex index, @NotNull GlobalSearchScope scope) {
                return type.getTypes().stream().map(Types::getType).anyMatch(t ->
                        t.startsWith("\\") && !index.getValues(InternalAnnotatedClassesIndexer.identity, t, scope).isEmpty()
                );
            }
        };
    }
}
