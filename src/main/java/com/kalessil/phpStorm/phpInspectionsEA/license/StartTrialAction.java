package com.kalessil.phpStorm.phpInspectionsEA.license;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import org.jetbrains.annotations.NotNull;

final public class StartTrialAction {
    /* repetitive calls are succeeding, but the license stays the same */
    public void perform(
        @NotNull LicenseService service,
        @NotNull IdeaPluginDescriptor plugin
    ) {
        final StringBuilder trialError = new StringBuilder();
        final boolean trialStarted     = service.startTrial(trialError);
        final NotificationGroup group  = EAUltimateApplicationComponent.getInstance().getNotificationGroup();
        ApplicationManager.getApplication().executeOnPooledThread(() ->
            Notifications.Bus.notify(group.createNotification(
                "<b>" + plugin.getName() + "</b>",
                trialStarted ?
                    String.format("Congratulations, a trial license was successfully applied (expires in %s days).", service.getTrialDaysRemaining()) :
                    String.format("Something went wrong, the activation process encountered an issue: %s", trialError.toString()),
                trialStarted ?
                    NotificationType.INFORMATION :
                    NotificationType.WARNING,
                NotificationListener.URL_OPENING_LISTENER
            ))
        );
    }
}
