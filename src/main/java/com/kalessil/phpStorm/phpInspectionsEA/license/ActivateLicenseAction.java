package com.kalessil.phpStorm.phpInspectionsEA.license;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

final public class ActivateLicenseAction {
    public void perform(
        @NotNull LicenseService service,
        @NotNull IdeaPluginDescriptor plugin
    ) {
        final String licenseKey = JOptionPane.showInputDialog(
            null,
            "Please provide a product key",
            String.format("%s activation", plugin.getName()),
            JOptionPane.QUESTION_MESSAGE
        );
        if (licenseKey == null) {
            return;
        }

        final StringBuilder activationError = new StringBuilder();
        final boolean licenseActivated      = service.applyLicenseKey(licenseKey, activationError);

        final NotificationGroup group = EAUltimateApplicationComponent.getInstance().getNotificationGroup();
        Notifications.Bus.notify(group.createNotification(
            "<b>" + plugin.getName() + "</b>",
            licenseActivated ?
                String.format("Congratulations, your copy of %s has been successfully activated.", plugin.getName()) :
                String.format("Something went wrong, the activation process encountered an issue: %s", activationError.toString()),
            licenseActivated ?
                NotificationType.INFORMATION :
                NotificationType.WARNING,
            NotificationListener.URL_OPENING_LISTENER
        ));
    }
}
