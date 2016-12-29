package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpDoc;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.containers.MultiMap;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnknownInspectionInspector extends BasePhpInspection {
    final private static Set<String> inspectionsNames = new HashSet<>();
    private static int minInspectionNameLength;
    static {
        collectInspections("com.jetbrains.php", inspectionsNames);
        collectInspections("com.kalessil.phpStorm.phpInspectionsEA", inspectionsNames);
        collectInspections("fr.adrienbrault.idea.symfony2plugin", inspectionsNames);

        /* shortest length is a threshold for separating inspections and comments mixed in */
        minInspectionNameLength = Integer.MAX_VALUE;
        for (String shortName : inspectionsNames) {
            int nameLength = shortName.length();
            if (nameLength < minInspectionNameLength) {
                minInspectionNameLength = nameLength;
            }
        }
    }

    @NotNull
    public String getShortName() {
        return "UnknownInspectionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpDocTag(@NotNull PhpDocTag tag) {
                if (!tag.getName().equals("@noinspection")) {
                    return;
                }

                /* cleanup the tag and ensure we have anything to check */
                final String tagValue     = tag.getTagValue().replaceAll("[^\\p{L}\\p{Nd}]+", " ").trim();
                final String[] suppressed = tagValue.split("\\s+");
                if (0 == suppressed.length || 0 == tagValue.length()) {
                    return;
                }

                /* check if all suppressed inspections are known */
                final List<String> reported = new ArrayList<>();
                for (String suppression : suppressed) {
                    if (suppression.length() >= minInspectionNameLength && !inspectionsNames.contains(suppression)) {
                        reported.add(suppression);
                    }
                }

                /* report unknown inspections: we also might be not aware of other plugins */
                if (reported.size() > 0) {
                    final PsiElement target = tag.getFirstChild();
                    if (null != target) {
                        final String message = "Unknown inspection: %i%".replace("%i%", String.join(", ", reported));
                        holder.registerProblem(target, message, ProblemHighlightType.WEAK_WARNING);
                    }

                    reported.clear();
                }
            }
        };
    }

    private static void collectInspections(@NotNull String id, @NotNull Set<String> inspectionsNames) {
        /* check plugin and its' extensions accessibility */
        final IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId(id));
        if (!(plugin instanceof IdeaPluginDescriptorImpl)) {
            return;
        }
        final MultiMap<String, Element> extensions = ((IdeaPluginDescriptorImpl) plugin).getExtensions();
        if (null == extensions) {
            return;
        }

        /* extract inspections; short names */
        for (Element node : extensions.values()) {
            final String nodeName = node.getName();
            if (null == nodeName || !nodeName.equals("localInspection")) {
                continue;
            }

            final Attribute name   = node.getAttribute("shortName");
            final String shortName = null == name ? null : name.getValue();
            if (null != shortName && shortName.length() > 0) {
                inspectionsNames.add(shortName);
            }
        }
    }
}
