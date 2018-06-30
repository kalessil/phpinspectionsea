package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.options.Configurable;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class EAUltimateApplicationConfiguration implements Configurable {
    private boolean SEND_CRASH_REPORTS;
    private boolean SEND_VERSION_INFORMATION;

    @Nullable
    @Override
    public JComponent createComponent() {
        final EAUltimateSettings settings = EAUltimateSettings.getInstance();
        SEND_CRASH_REPORTS                = settings.getSendCrashReports();
        SEND_VERSION_INFORMATION          = settings.getSendVersionInformation();

        return OptionsComponent.create((component) -> {
            component.addCheckbox("Automatically collect crash-reports", SEND_CRASH_REPORTS, (isSelected) -> SEND_CRASH_REPORTS = isSelected);
            component.addCheckbox("Automatically collect plugin version info", SEND_VERSION_INFORMATION, (isSelected) -> SEND_VERSION_INFORMATION = isSelected);
        });
    }

    @Override
    public boolean isModified() {
        final EAUltimateSettings settings = EAUltimateSettings.getInstance();
        return SEND_CRASH_REPORTS != settings.getSendCrashReports() ||
               SEND_VERSION_INFORMATION != settings.getSendVersionInformation();
    }

    @Override
    public void apply() {
        final EAUltimateSettings settings = EAUltimateSettings.getInstance();
        settings.setSendCrashReports(SEND_CRASH_REPORTS);
        settings.setSendVersionInformation(SEND_VERSION_INFORMATION);
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
