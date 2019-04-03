package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NotNull;

public class EAUltimateUpdateComponent implements ProjectComponent {
    private EAUltimateApplicationComponent applicationComponent;

    @Override
    public void initComponent() {
        applicationComponent = EAUltimateApplicationComponent.getInstance();
    }

    @Override
    public void disposeComponent() {}

    @NotNull
    @Override
    public String getComponentName() {
        return "EAUltimateUpdateComponent";
    }

    @Override
    public void projectOpened() {
        if (applicationComponent.isUpdated() && !applicationComponent.isUpdateNotificationShown()) {
            final IdeaPluginDescriptor ultimatePlugin = PluginManager.getPlugin(PluginId.getId("com.kalessil.phpStorm.phpInspectionsUltimate"));
            if (ultimatePlugin == null) {
                return;
            }

            final NotificationGroup group = EAUltimateApplicationComponent.getInstance().getNotificationGroup();
            ApplicationManager.getApplication().executeOnPooledThread(() ->
                Notifications.Bus.notify(group.createNotification(
                        "<b>" + ultimatePlugin.getName() + "</b> update v" + ultimatePlugin.getVersion(),
                        ultimatePlugin.getChangeNotes(),
                        NotificationType.INFORMATION,
                        NotificationListener.URL_OPENING_LISTENER
                ))
            );

            final IdeaPluginDescriptor extendedPlugin = PluginManager.getPlugin(PluginId.getId("com.kalessil.phpStorm.phpInspectionsEA"));
            if (extendedPlugin != null) {
                ApplicationManager.getApplication().executeOnPooledThread(() ->
                    Notifications.Bus.notify(group.createNotification(
                            "<b>" + ultimatePlugin.getName() + "</b>",
                            "Please uninstall Php Inspections (EA Extended) in order to avoid unexpected surprises.",
                            NotificationType.WARNING,
                            NotificationListener.URL_OPENING_LISTENER
                    ))
                );
            }

            applicationComponent.setUpdateNotificationShown();
        }
    }

    @Override
    public void projectClosed() {}
}
