package com.kalessil.phpStorm.phpInspectionsEA.license;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.wyday.turboactivate.TurboActivateException;

final public class ActivateLicenseMenuAction extends AnAction implements DumbAware {
    public ActivateLicenseMenuAction() {
    }

    @Override
    public void update(AnActionEvent event) {
        final Presentation presentation = event.getPresentation();
        presentation.setVisible(true);
        presentation.setEnabled(true);

        final LicenseService service = EAUltimateApplicationComponent.getLicenseService();
        if (service != null && service.shouldCheckPluginLicense() && service.isClientInitialized()) {
            try {
                presentation.setEnabled(!service.isActiveLicense());
            } catch (TurboActivateException serviceError) {
                presentation.setEnabled(false);
            }
        }
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final LicenseService service      = EAUltimateApplicationComponent.getLicenseService();
        final IdeaPluginDescriptor plugin = EAUltimateApplicationComponent.getPluginDescriptor();
        if (service != null && plugin != null && service.isClientInitialized()) {
            (new ActivateLicenseAction()).perform(service, plugin);
            this.update(event);
        }
    }
}