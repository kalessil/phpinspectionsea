package com.kalessil.phpStorm.phpInspectionsEA.inspectors;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.kalessil.phpStorm.phpInspectionsEA.indexers.ComposerPackageDependenciesIndexer;
import com.kalessil.phpStorm.phpInspectionsEA.indexers.ComposerPackageRelationIndexer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class TransitiveDependenciesUsageInspector extends PhpInspection {
    private static final String message = "The class belongs to a package which is not directly required in your composer.json. Please add the package into your composer.json";

    final static private Set<String> references = new HashSet<>();
    static {
        references.add("self");
        references.add("static");
        references.add("parent");
    }

    // Inspection options.
    public final List<String> configuration  = new ArrayList<>();

    @NotNull
    @Override
    public String getShortName() {
        return "TransitiveDependenciesUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Transitive dependencies usage";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpClassReference(@NotNull ClassReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE)) { return; }

                if (this.isTarget(reference) && !this.isTestContext(reference)) {
                    final Project project    = holder.getProject();
                    final String ownManifest = this.getManifest(reference, project);
                    if (ownManifest != null) {
                        final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                        if (resolved != null) {
                            final String dependencyManifest = this.getManifest(resolved, project);
                            if (dependencyManifest != null && !ownManifest.equals(dependencyManifest)) {
                                final boolean isTarget = this.isTransitiveDependency(ownManifest, dependencyManifest, project);
                                if (isTarget) {
                                    holder.registerProblem(reference, message, new OpenDependencyManifest(dependencyManifest));
                                }
                            }
                        }
                    }
                }
            }

            private boolean isTarget(@NotNull ClassReference reference) {
                final String asString = reference.getText();
                if (!references.contains(asString)) {
                    if (asString.indexOf('\\') != -1 || reference.getParent() instanceof PhpUse)  {
                        return true;
                    }
                    final PhpClass clazz = PsiTreeUtil.getParentOfType(reference, PhpClass.class, false, (Class) PsiFile.class);
                    if (clazz == null || clazz.getNamespaceName().equals("\\")) {
                        return true;
                    }
                }
                return false;
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
                            manifests.clear();
                        } catch (final Throwable failure) {
                            result = null;
                        }
                    }
                }
                return result;
            }

            private boolean isTransitiveDependency(
                    @NotNull String ownManifest,
                    @NotNull String dependencyManifest,
                    @NotNull Project project
            ) {
                boolean result             = false;
                final FileBasedIndex index = this.getIndex();
                if (index != null) {
                    final GlobalSearchScope scope        = GlobalSearchScope.allScope(project);
                    final List<String> dependencyDetails = index.getValues(ComposerPackageDependenciesIndexer.identity, dependencyManifest, scope);
                    if (dependencyDetails.size() == 1) {
                        /* dependency should be listed in own manifest */
                        final String[] dependencySplit    = dependencyDetails.get(0).split(":", 2);
                        final String[] dependencyPackages = (dependencySplit.length == 2 ? dependencySplit[0] : "").split(",");
                        final String dependencyName       = dependencyPackages.length > 0 ? dependencyPackages[0] : "";
                        if (!dependencyName.isEmpty() && !this.isDependencyIgnored(dependencyName)) {
                            final List<String> currentDetails = index.getValues(ComposerPackageDependenciesIndexer.identity, ownManifest, scope);
                            if (currentDetails.size() == 1) {
                                final String[] currentSplit            = currentDetails.get(0).split(":", 2);
                                final List<String> currentDependencies = Arrays.asList((currentSplit.length == 2 ? currentSplit[1] : "").split(","));
                                /* false-positive: one of own dependencies replaces the dependency being analyzed */
                                if (result = currentDependencies.stream().noneMatch(d -> d.equals(dependencyName))) {
                                    for (final String file : index.getAllKeys(ComposerPackageDependenciesIndexer.identity, project)) {
                                        if (file.equals(ownManifest) || file.equals(dependencyManifest)) {
                                            continue;
                                        }
                                        /* find manifest replacing the dependency */
                                        final List<String> details = index.getValues(ComposerPackageDependenciesIndexer.identity, file, scope);
                                        if (details.size() == 1) {
                                            final String[] split        = details.get(0).split(":", 2);
                                            final List<String> packages = Arrays.asList((split.length == 2 ? split[0] : "").split(","));
                                            if (packages.contains(dependencyName)) {
                                                result = packages.stream().noneMatch(currentDependencies::contains);
                                                break;
                                            }
                                        }
                                        details.clear();
                                    }
                                }
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

    private static final class OpenDependencyManifest implements LocalQuickFix {
        private static final String title = "Open dependency manifest";
        private final String file;

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        OpenDependencyManifest(@NotNull String file) {
            this.file = file;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            if (!project.isDisposed()) {
                final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(this.file);
                if (file != null) {
                    (new OpenFileDescriptor(project, file)).navigate(true);
                }
            }
        }
    }
}
