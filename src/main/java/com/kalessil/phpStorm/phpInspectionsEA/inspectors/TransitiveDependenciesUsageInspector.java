package com.kalessil.phpStorm.phpInspectionsEA.inspectors;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.kalessil.phpStorm.phpInspectionsEA.indexers.ComposerPackageManifestIndexer;
import com.kalessil.phpStorm.phpInspectionsEA.indexers.ComposerPackageRelationIndexer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

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
                final Project project        = holder.getProject();
                final String currentManifest = this.getManifest(reference, project);
                if (currentManifest != null) {
                    final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                    if (resolved != null) {
                        final String dependecyManifest = this.getManifest(resolved, project);
                        if (dependecyManifest != null && !currentManifest.equals(dependecyManifest)) {
                            final boolean isTarget = this.isTransitiveDependency(currentManifest, dependecyManifest, project);
                            if (isTarget) {
                                holder.registerProblem(reference, "Nope: transitive dependency");
                            }
                        }
                    }
                }
            }

            @Nullable
            private String getManifest(@NotNull PsiElement expression, @NotNull Project project) {
                String result         = null;
                final String filePath = expression.getContainingFile().getVirtualFile().getCanonicalPath();
                if (filePath != null) {
                    final List<String> manifests = FileBasedIndex.getInstance()
                            .getValues(ComposerPackageRelationIndexer.identity, filePath, GlobalSearchScope.allScope(project));
                    if (manifests.size() == 1) {
                        result = manifests.get(0);
                    }
                    manifests.clear();
                }
                return result;
            }

            private boolean isTransitiveDependency(@NotNull String current, @NotNull String dependecy, @NotNull Project project) {
                boolean result                       = false;
                final GlobalSearchScope scope        = GlobalSearchScope.allScope(project);
                final List<String> dependencyDetails = FileBasedIndex.getInstance()
                        .getValues(ComposerPackageManifestIndexer.identity, dependecy, scope);
                if (dependencyDetails.size() == 1) {
                    final String dependencyName = dependencyDetails.get(0).split(":")[0];
                    if (!dependencyName.isEmpty()) {
                        final List<String> currentDetails = FileBasedIndex.getInstance()
                                .getValues(ComposerPackageManifestIndexer.identity, dependecy, scope);
                        if (currentDetails.size() == 1) {
                            final String currentDependencies = currentDetails.get(0).split(":")[1];
                            result = Stream.of(currentDependencies.split(",")).anyMatch(d -> d.equals(dependencyName));
                        }
                        currentDetails.clear();
                    }
                }
                dependencyDetails.clear();
                return result;
            }
        };
    }
}
