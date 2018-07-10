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
    private boolean SEND_VERSION_INFORMATION;
    private boolean COMPARISON_STYLE_REGULAR;
    private boolean COMPARISON_STYLE_YODA;

    @Nullable
    @Override
    public JComponent createComponent() {
        final EAUltimateSettings settings = EAUltimateSettings.getInstance();
        SEND_CRASH_REPORTS                = settings.getSendCrashReports();
        SEND_VERSION_INFORMATION          = settings.getSendVersionInformation();

        final ComparisonStyle comparisonStyle = settings.getComparisonStyle();
        COMPARISON_STYLE_REGULAR              = comparisonStyle == ComparisonStyle.REGULAR;
        COMPARISON_STYLE_YODA                 = comparisonStyle == ComparisonStyle.YODA;

        return OptionsComponent.create(component -> {
            component.addPanel("Anonymous data collect", panelComponent -> {
                panelComponent.addCheckbox("Automatically collect crash-reports", SEND_CRASH_REPORTS, (isSelected) -> SEND_CRASH_REPORTS = isSelected);
                panelComponent.addCheckbox("Automatically collect plugin version info", SEND_VERSION_INFORMATION, (isSelected) -> SEND_VERSION_INFORMATION = isSelected);
            });

            /* comparison style */
            component.addPanel("Comparison code style", panelComponent ->
                panelComponent.delegateRadioCreation(radioComponent -> {
                    radioComponent.addOption("Regular comparison style", COMPARISON_STYLE_REGULAR, (isSelected) -> COMPARISON_STYLE_REGULAR = isSelected);
                    radioComponent.addOption("Yoda comparison style", COMPARISON_STYLE_YODA, (isSelected) -> COMPARISON_STYLE_YODA = isSelected);
                }
            ));
        });
    }

    @Override
    public boolean isModified() {
        final EAUltimateSettings settings     = EAUltimateSettings.getInstance();
        final ComparisonStyle comparisonStyle = settings.getComparisonStyle();

        return SEND_CRASH_REPORTS != settings.getSendCrashReports() ||
               SEND_VERSION_INFORMATION != settings.getSendVersionInformation() ||
               COMPARISON_STYLE_YODA != (comparisonStyle == ComparisonStyle.YODA);
    }

    @Override
    public void apply() {
        final EAUltimateSettings settings = EAUltimateSettings.getInstance();
        settings.setSendCrashReports(SEND_CRASH_REPORTS);
        settings.setSendVersionInformation(SEND_VERSION_INFORMATION);
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
