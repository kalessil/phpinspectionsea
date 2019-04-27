package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

@State(name = "EAUltimateProjectSettings", storages = @Storage("$PROJECT_CONFIG_DIR$/php-inspections-ea-ultimate.xml"))
public class EAUltimateProjectSettings extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    private boolean preferYodaComparisonStyle           = false;
    private boolean analyzeOnlyModifiedFiles            = false;
    private Map<StrictnessCategory, Boolean> categories = new LinkedHashMap<>();

    protected EAUltimateProjectSettings(@NotNull Project project) {
        super(project);
        Arrays.stream(StrictnessCategory.values()).forEach(category -> categories.put(category, true));
    }

    @Nullable
    @Override
    public Element getState() {
        final Element categoriesNode = new Element("categories");
        Arrays.stream(StrictnessCategory.values()).forEach(category -> {
            final Element categoryNode     = new Element(category.name());
            final boolean isCategoryActive = categories.containsKey(category) && categories.get(category);
            categoryNode.setAttribute("enabled", isCategoryActive ? "yes" : "no");
            categoriesNode.addContent(categoryNode);
        });
        
        final Element analyzeOnlyModifiedFilesNode = new Element("ANALYZE_ONLY_MODIFIED_FILES");
        analyzeOnlyModifiedFilesNode.setAttribute("value", analyzeOnlyModifiedFiles ? "yes" : "no");

        final Element preferYodaComparisonStyleNode = new Element("PREFER_YODA_COMPARISON_STYLE");
        preferYodaComparisonStyleNode.setAttribute("value", preferYodaComparisonStyle ? "yes" : "no");

        final Element settingsNode = new Element("settings");
        settingsNode.addContent(analyzeOnlyModifiedFilesNode);
        settingsNode.addContent(preferYodaComparisonStyleNode);

        final Element configuration = new Element("any-name-here");
        configuration.addContent(categoriesNode);
        configuration.addContent(settingsNode);

        return configuration;
    }

    @Override
    public void loadState(@NotNull Element state) {
        final Element categoriesNode = state.getChild("categories");
        if (categoriesNode != null) {
            categoriesNode.getChildren().forEach(categoryNode -> {
                final boolean isCategoryActive    = !"no".equals(categoryNode.getAttributeValue("enabled"));
                final StrictnessCategory category = StrictnessCategory.valueOf(categoryNode.getName());
                categories.put(category, isCategoryActive);
            });
        }

        final Element settingsNode = state.getChild("settings");
        if (settingsNode != null) {
            final Element analyzeOnlyModifiedFilesNode = settingsNode.getChild("ANALYZE_ONLY_MODIFIED_FILES");
            if (analyzeOnlyModifiedFilesNode != null) {
                analyzeOnlyModifiedFiles = "yes".equals(analyzeOnlyModifiedFilesNode.getAttributeValue("value"));
            }
            final Element preferYodaComparisonStyleNode = settingsNode.getChild("PREFER_YODA_COMPARISON_STYLE");
            if (preferYodaComparisonStyleNode != null) {
                preferYodaComparisonStyle = "yes".equals(preferYodaComparisonStyleNode.getAttributeValue("value"));
            }
        }
    }

    public boolean isCategoryActive(@NotNull StrictnessCategory category) {
        return categories.containsKey(category) && categories.get(category);
    }

    public void setCategoryActiveFlag(@NotNull StrictnessCategory category, boolean value) {
        categories.put(category, value);
    }

    public boolean isAnalyzingOnlyModifiedFiles() {
        return analyzeOnlyModifiedFiles;
    }

    public void setAnalyzingOnlyModifiedFiles(boolean value) {
        analyzeOnlyModifiedFiles = value;
    }

    public boolean isPreferringYodaComparisonStyle() {
        return preferYodaComparisonStyle;
    }

    public void setPreferringYodaComparisonStyle(boolean value) {
        preferYodaComparisonStyle = value;
    }
}
