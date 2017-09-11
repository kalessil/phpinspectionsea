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
    private static final String message = "Unknown inspection: %i%.";

    final private static Set<String> inspectionsNames;
    private static int minInspectionNameLength;
    static {
        inspectionsNames = collectKnownInspections();
        /* spell checker is a nameless plugin with no deps, hence hardcoding it's inspections */
        inspectionsNames.add("SpellCheckingInspection");

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

                /* report unknown inspections; if inspections provided by not loaded plugin they are reported */
                if (reported.size() > 0) {
                    final PsiElement target = tag.getFirstChild();
                    if (null != target) {
                        holder.registerProblem(target, message.replace("%i%", String.join(", ", reported)), ProblemHighlightType.WEAK_WARNING);
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
            /* we have to rely on impl-class, see  */
            final MultiMap<String, Element> extensions = ((IdeaPluginDescriptorImpl) plugin).getExtensions();
            final boolean isPhpPlugin                  = plugin.getPluginId().equals(phpSupport);
            if (null == extensions || (!ArrayUtils.contains(plugin.getDependentPluginIds(), phpSupport)) && !isPhpPlugin) {
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
