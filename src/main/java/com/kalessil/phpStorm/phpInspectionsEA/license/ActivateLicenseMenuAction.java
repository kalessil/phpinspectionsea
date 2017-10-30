package com.kalessil.phpStorm.phpInspectionsEA.license;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.util.Options;
import com.kalessil.phpStorm.phpInspectionsEA.EAApplicationComponent;
import com.wyday.turboactivate.TurboActivateException;

final public class ActivateLicenseMenuAction extends AnAction implements DumbAware {
    public ActivateLicenseMenuAction() {
    }

    @Override
    public void update(AnActionEvent event) {
        final Presentation presentation = event.getPresentation();
        presentation.setVisible(true);
        presentation.setEnabled(false);

        final LicenseService service = EAApplicationComponent.getLicenseService();
        if (service != null && service.shouldCheckPluginLicense() && service.isClientInitialized()) {
            try {
                if (!service.isActiveLicense()) {
                    presentation.setEnabled(true);
                }
            } catch (TurboActivateException serviceError) {
                presentation.setEnabled(false);
            }
        }
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final LicenseService service      = EAApplicationComponent.getLicenseService();
        final IdeaPluginDescriptor plugin = EAApplicationComponent.getPluginDescriptor();
        if (service != null && plugin != null && service.isClientInitialized()) {
            (new ActivateLicenseAction()).perform(service, plugin);
            this.update(event);
        }
    }
}