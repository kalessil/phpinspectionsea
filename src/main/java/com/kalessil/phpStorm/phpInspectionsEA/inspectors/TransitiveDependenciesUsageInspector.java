package com.kalessil.phpStorm.phpInspectionsEA.inspectors;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.kalessil.phpStorm.phpInspectionsEA.indexers.ComposerPackageRelationIndexer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TransitiveDependenciesUsageInspector extends BasePhpInspection {

    @NotNull
    public String getShortName() {
        return "TransitiveDependenciesUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClassReference(@NotNull ClassReference reference) {
                final String currentManifest = this.getManifest(reference);
                if (currentManifest != null) {
                    final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                    if (resolved != null) {
                        final String providerManifest = this.getManifest(resolved);
                        if (providerManifest != null && !currentManifest.equals(providerManifest)) {

                        }
                    }
                }
            }

            @Nullable
            private String getManifest(@NotNull PsiElement expression) {
                String result         = null;
                final String filePath = expression.getContainingFile().getVirtualFile().getCanonicalPath();
                if (filePath != null) {
                    final List<String> manifests = FileBasedIndex.getInstance()
                            .getValues(ComposerPackageRelationIndexer.identity, filePath, GlobalSearchScope.allScope(expression.getProject()));
                    if (manifests.size() == 1) {
                        result = manifests.get(0);
                    }
                    manifests.clear();
                }
                return result;
            }
        };
    }
}
