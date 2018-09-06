package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.options.Configurable;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class EAApplicationConfiguration implements Configurable {
    private boolean SEND_CRASH_REPORTS;
    private boolean COMPARISON_STYLE_REGULAR;
    private boolean COMPARISON_STYLE_YODA;

    @Nullable
    @Override
    public JComponent createComponent() {
        final EASettings settings = EASettings.getInstance();
        SEND_CRASH_REPORTS        = settings.getSendCrashReports();

        final ComparisonStyle comparisonStyle = settings.getComparisonStyle();
        COMPARISON_STYLE_REGULAR              = comparisonStyle == ComparisonStyle.REGULAR;
        COMPARISON_STYLE_YODA                 = comparisonStyle == ComparisonStyle.YODA;

        return OptionsComponent.create(component -> {
            component.addPanel("Anonymous data collect", panelComponent ->
                panelComponent.addCheckbox("Automatically collect crash-reports", SEND_CRASH_REPORTS, (isSelected) -> SEND_CRASH_REPORTS = isSelected)
            );

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
        final EASettings      settings        = EASettings.getInstance();
        final ComparisonStyle comparisonStyle = settings.getComparisonStyle();

        return SEND_CRASH_REPORTS != settings.getSendCrashReports() ||
               COMPARISON_STYLE_YODA != (comparisonStyle == ComparisonStyle.YODA);
    }

    @Override
    public void apply() {
        final EASettings settings = EASettings.getInstance();
        settings.setSendCrashReports(SEND_CRASH_REPORTS);
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
        return "Php Inspections (EA Extended)";
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return null;
    }
}
