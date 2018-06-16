package com.kalessil.phpStorm.phpInspectionsEA.inspectors;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.indexers.ComposerPackageManifestIndexer;
import com.kalessil.phpStorm.phpInspectionsEA.indexers.ComposerPackageRelationIndexer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class TransitiveDependenciesUsageInspector extends BasePhpInspection {
    private static final String message = "The class belongs to a package which is not directly required in your composer.json. Please add the package into your composer.json";

    // Inspection options.
    public final List<String> configuration  = new ArrayList<>();

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
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                if (!this.isTestContext(reference)) {
                    final Project project    = holder.getProject();
                    final String ownManifest = this.getManifest(reference, project);
                    if (ownManifest != null) {
                        final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                        if (resolved != null) {
                            final String dependencyManifest = this.getManifest(resolved, project);
                            if (dependencyManifest != null && !ownManifest.equals(dependencyManifest)) {
                                final boolean isTarget
                                        = this.isTransitiveDependency(ownManifest, dependencyManifest, project);
                                if (isTarget) {
                                    holder.registerProblem(reference, message);
                                }
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
                    final FileBasedIndex index = this.getIndex();
                    if (index != null) {
                        List<String> manifests;
                        try {
                            manifests = index.getValues(ComposerPackageRelationIndexer.identity, filePath, GlobalSearchScope.allScope(project));
                            if (manifests.size() == 1) {
                                result = manifests.get(0);
                            }
                        } catch (final Throwable failure) {
                            manifests = new ArrayList<>();
                        }
                        manifests.clear();
                    }
                }
                return result;
            }

            private boolean isTransitiveDependency(@NotNull String current, @NotNull String dependency, @NotNull Project project) {
                boolean result             = false;
                final FileBasedIndex index = this.getIndex();
                if (index != null) {
                    final GlobalSearchScope scope        = GlobalSearchScope.allScope(project);
                    final List<String> dependencyDetails = index.getValues(ComposerPackageManifestIndexer.identity, dependency, scope);
                    if (dependencyDetails.size() == 1) {
                        final String[] dependencySplit = dependencyDetails.get(0).split(":");
                        final String dependencyName    = dependencySplit.length == 2 ? dependencySplit[0] : "";
                        if (!dependencyName.isEmpty() && !this.isDependencyIgnored(dependencyName)) {
                            final List<String> currentDetails = index.getValues(ComposerPackageManifestIndexer.identity, current, scope);
                            if (currentDetails.size() == 1) {
                                final String[] currentSplit      = currentDetails.get(0).split(":");
                                final String currentDependencies = currentSplit.length == 2 ? currentSplit[1] : "";
                                result = Stream.of(currentDependencies.split(",")).noneMatch(d -> d.equals(dependencyName));
                            }
                            currentDetails.clear();
                        }
                    }
                    dependencyDetails.clear();
                }
                return result;
            }

            @Nullable
            private FileBasedIndex getIndex() {
                try {
                    return FileBasedIndex.getInstance();
                } catch (final Throwable failure) {
                    return null;
                }
            }

            private boolean isDependencyIgnored(@NotNull String packageName) {
                boolean result = configuration.contains(packageName);
                if (!result) {
                    final String[] packageDetails = packageName.split("/");
                    result = packageDetails.length == 2 && configuration.contains(packageDetails[0] + "/*");
                }
                return result;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component)
            -> component.addList(
                    "Ignored packages:",
                    configuration,
                    null,
                    null,
                    "Adding ignored package...",
                    "Examples: 'psr/container', 'psr/*'")
        );
    }

    @Override
    public void readSettings(@NotNull Element node) throws InvalidDataException {
        super.readSettings(node);

        /* ensure entries are unique */
        final Set<String> uniqueEntries = new HashSet<>(configuration);
        configuration.clear();
        configuration.addAll(uniqueEntries);

        /* sort entries */
        Collections.sort(configuration);
    }
}
