package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpDoc;

import com.intellij.codeInspection.LocalInspectionEP;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.intellij.codeInspection.LocalInspectionEP.LOCAL_INSPECTION;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnknownInspectionInspector extends BasePhpInspection {
    private static final String message = "Unknown inspection: %s.";

    final private static Set<String> inspectionsNames;
    private static int minInspectionNameLength;
    static {
        inspectionsNames = collectPhpRelatedInspections();
        inspectionsNames.add("phpinspectionsea");
        inspectionsNames.add("SpellCheckingInspection");
        inspectionsNames.add("PhpUnused");

        /* shortest length is a threshold for separating inspections and comments mixed in */
        minInspectionNameLength = Integer.MAX_VALUE;
        for (final String shortName : inspectionsNames) {
            final int nameLength = shortName.length();
            if (nameLength < minInspectionNameLength) {
                minInspectionNameLength = nameLength;
            }
        }
    }

    @NotNull
    @Override
    public String getShortName() {
        return "UnknownInspectionInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Unknown inspection suppression";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpDocTag(@NotNull PhpDocTag tag) {
                if (tag.getName().equals("@noinspection")) {
                    final String[] candidates = tag.getTagValue().replaceAll("[^\\p{L}\\p{Nd}]+", " ").trim().split("\\s+");
                    if (candidates.length > 0) {
                        final List<String> inspections = Arrays.stream(candidates)
                                .filter(candidate -> candidate.length() >= minInspectionNameLength && !inspectionsNames.contains(candidate) && ! candidate.startsWith("Sql"))
                                .collect(Collectors.toList());
                        if (!inspections.isEmpty()) {
                            final PsiElement target = tag.getFirstChild();
                            if (target != null) {
                                holder.registerProblem(target, String.format(message, String.join(", ", inspections)));
                            }
                            inspections.clear();
                        }
                    }
                }
            }
        };
    }

    private static Set<String> collectPhpRelatedInspections() {
        final Set<String> names = new HashSet<>();

        final PluginId phpPlugin = PluginId.getId("com.jetbrains.php");
        for (final LocalInspectionEP inspection : LOCAL_INSPECTION.getExtensions()) {
            final IdeaPluginDescriptor plugin = (IdeaPluginDescriptor) inspection.getPluginDescriptor();
            if (phpPlugin.equals(plugin.getPluginId()) || ArrayUtils.contains(plugin.getDependentPluginIds(), phpPlugin)) {
                names.add(inspection.getShortName());
            }
        }

        return names;
    }
}
