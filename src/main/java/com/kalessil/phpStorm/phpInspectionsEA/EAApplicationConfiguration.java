package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class EAApplicationConfiguration implements Configurable {
    private JPanel configurationPanel;

    private boolean SEND_CRASH_REPORTS;
    private boolean SEND_VERSION_INFORMATION;

    @Nullable
    @Override
    public JComponent createComponent() {
        return this.configurationPanel = OptionsComponent.create((component) -> {
            component.addCheckbox("Automatically collect crash-reports", SEND_CRASH_REPORTS, (isSelected) -> SEND_CRASH_REPORTS = isSelected);
            component.addCheckbox("Automatically collect plugin updates", SEND_VERSION_INFORMATION, (isSelected) -> SEND_VERSION_INFORMATION = isSelected);
        });
    }

    @Override
    public boolean isModified() {
        /* TODO: compare form and settings values */
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        /* get form settings, dispatch into settings */
    }

    @Override
    public void reset() {
        /* TODO: update from settings */
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
}
