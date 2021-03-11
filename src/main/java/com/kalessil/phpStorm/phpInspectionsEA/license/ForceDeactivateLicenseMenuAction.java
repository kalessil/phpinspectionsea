package com.kalessil.phpStorm.phpInspectionsEA.license;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.wyday.turboactivate.TurboActivateException;

/**
 * The action is intended to force license deactivation, in order to troubleshoot license state issues.
 */
final public class ForceDeactivateLicenseMenuAction extends AnAction implements DumbAware {
    public ForceDeactivateLicenseMenuAction() {
    }

    @Override
    public void update(final AnActionEvent event) {
        /* Same as ActivateLicenseMenuAction::update */
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
    public void actionPerformed(final AnActionEvent event) {
        /* Same as DeactivateLicenseMenuAction::actionPerformed */
        final LicenseService service      = EAUltimateApplicationComponent.getLicenseService();
        final IdeaPluginDescriptor plugin = EAUltimateApplicationComponent.getPluginDescriptor();
        if (service != null && plugin != null && service.isClientInitialized()) {
            (new DeactivateLicenseAction()).perform(service, plugin);
            this.update(event);
        }
    }
}