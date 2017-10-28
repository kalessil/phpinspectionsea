package com.kalessil.phpStorm.phpInspectionsEA.license;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.*;
import com.wyday.turboactivate.TurboActivate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final public class StartTrialAction {
    public void perform(
        @NotNull LicenseService service,
        @Nullable TurboActivate client,
        @Nullable IdeaPluginDescriptor plugin
    ) {
        final StringBuilder trialError = new StringBuilder();
        final boolean trialStarted     = service.startTrial(client, trialError);

        final String pluginName       = plugin.getName();
        final NotificationGroup group = new NotificationGroup(pluginName, NotificationDisplayType.STICKY_BALLOON, true);
        Notifications.Bus.notify(group.createNotification(
            "<b>" + pluginName + "</b>",
            trialStarted ?
                String.format("Congrats, a trial license was successfully applied (expires in %s days).", service.getTrialDaysRemaining()) :
                String.format("Something went wrong, we were not able to obtain a trial license: %s", trialError.toString()),
            trialStarted ?
                NotificationType.INFORMATION :
                NotificationType.WARNING,
            NotificationListener.URL_OPENING_LISTENER
        ));
    }
}
