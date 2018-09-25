package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.options.Configurable;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class EAUltimateApplicationConfiguration implements Configurable {
    private boolean SEND_CRASH_REPORTS;
    private boolean CHECK_ONLY_CHANGED_FILES;
    private boolean COMPARISON_STYLE_REGULAR;
    private boolean COMPARISON_STYLE_YODA;

    @Nullable
    @Override
    public JComponent createComponent() {
        final EAUltimateSettings settings = EAUltimateSettings.getInstance();
        SEND_CRASH_REPORTS                = settings.getSendCrashReports();
        CHECK_ONLY_CHANGED_FILES          = settings.getCheckOnlyChangedFiles();

        final ComparisonStyle comparisonStyle = settings.getComparisonStyle();
        COMPARISON_STYLE_REGULAR              = comparisonStyle == ComparisonStyle.REGULAR;
        COMPARISON_STYLE_YODA                 = comparisonStyle == ComparisonStyle.YODA;

        return OptionsComponent.create(component -> {
            component.addPanel("Privacy", panel ->
                panel.addCheckbox("Automatically send crash-reports", SEND_CRASH_REPORTS, (isSelected) -> SEND_CRASH_REPORTS = isSelected)
            );

            component.addPanel("Distraction level", panel ->
                panel.addCheckbox("Analyze only modified files", CHECK_ONLY_CHANGED_FILES, (isSelected) -> CHECK_ONLY_CHANGED_FILES = isSelected)
            );

            /* comparison style */
            component.addPanel("Comparison code style", panel ->
                panel.delegateRadioCreation(radio -> {
                    radio.addOption("Regular comparison style", COMPARISON_STYLE_REGULAR, (isSelected) -> COMPARISON_STYLE_REGULAR = isSelected);
                    radio.addOption("Yoda comparison style", COMPARISON_STYLE_YODA, (isSelected) -> COMPARISON_STYLE_YODA = isSelected);
                }));
        });
    }

    @Override
    public boolean isModified() {
        final EAUltimateSettings settings = EAUltimateSettings.getInstance();
        return SEND_CRASH_REPORTS       != settings.getSendCrashReports() ||
               CHECK_ONLY_CHANGED_FILES != settings.getCheckOnlyChangedFiles() ||
               COMPARISON_STYLE_YODA    != (settings.getComparisonStyle() == ComparisonStyle.YODA);
    }

    @Override
    public void apply() {
        final EAUltimateSettings settings = EAUltimateSettings.getInstance();
        settings.setSendCrashReports(SEND_CRASH_REPORTS);
        settings.setCheckOnlyChangedFiles(CHECK_ONLY_CHANGED_FILES);
        settings.setComparisonStyle(COMPARISON_STYLE_REGULAR ? ComparisonStyle.REGULAR : ComparisonStyle.YODA);
    }

    @Override
    public void reset() {
        /* nothing should happen here as losing settings here extremely frustrating */
    }

    @Override
    public void disposeUIResources() {
        /* nothing to dispose so far */
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Php Inspections (EA Ultimate)";
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return null;
    }
}
