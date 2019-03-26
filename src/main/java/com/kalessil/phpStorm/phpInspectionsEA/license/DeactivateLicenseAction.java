package com.kalessil.phpStorm.phpInspectionsEA.license;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import org.jetbrains.annotations.NotNull;

final public class DeactivateLicenseAction {
    public void perform(
        @NotNull LicenseService service,
        @NotNull IdeaPluginDescriptor plugin
    ) {
        final StringBuilder deactivationError = new StringBuilder();
        final StringBuilder keyHolder         = new StringBuilder();
        final boolean licenseDeactivated      = service.deactivateLicenseKey(keyHolder, deactivationError);

        final NotificationGroup group = EAUltimateApplicationComponent.getInstance().getNotificationGroup();
        Notifications.Bus.notify(group.createNotification(
            "<b>" + plugin.getName() + "</b>",
            licenseDeactivated ?
                String.format("The product key has been released ('%s' can be used for activation again).", keyHolder.toString()) :
                String.format("Something went wrong, we were not able to complete the deactivation: %s", deactivationError.toString()),
            licenseDeactivated ?
                NotificationType.INFORMATION :
                NotificationType.WARNING,
            NotificationListener.URL_OPENING_LISTENER
        ));
    }
}
