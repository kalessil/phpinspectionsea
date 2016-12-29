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
import org.apache.commons.lang.ArrayUtils;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnknownInspectionInspector extends BasePhpInspection {
    final private static Set<String> inspectionsNames;
    private static int minInspectionNameLength;
    static {
        inspectionsNames = collectKnownInspections();

        /* shortest length is a threshold for separating inspections and comments mixed in */
        minInspectionNameLength = Integer.MAX_VALUE;
        for (String shortName : inspectionsNames) {
            final int nameLength = shortName.length();
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

    private static Set<String> collectKnownInspections() {
        final Set<String> names   = new HashSet<>();
        final PluginId phpSupport = PluginId.getId("com.jetbrains.php");

        for (IdeaPluginDescriptor plugin : PluginManager.getPlugins()) {
            /* check plugins' dependencies and extensions */
            /* we have to rely on impl-class, see https://youtrack.jetbrains.com/issue/WI-34555 */
            final MultiMap<String, Element> extensions = ((IdeaPluginDescriptorImpl) plugin).getExtensions();
            if (null == extensions || !ArrayUtils.contains(plugin.getDependentPluginIds(), phpSupport)) {
                continue;
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
                    names.add(shortName);
                }
            }
        }

        return names;
    }
}
