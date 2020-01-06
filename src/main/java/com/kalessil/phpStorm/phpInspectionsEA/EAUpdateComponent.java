package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NotNull;

public class EAUpdateComponent implements ProjectComponent {
    private EAApplicationComponent applicationComponent;

    @Override
    public void initComponent() {
        applicationComponent = EAApplicationComponent.getInstance();
    }

    @Override
    public void disposeComponent() {}

    @NotNull
    @Override
    public String getComponentName() {
        return "EAUpdateComponent";
    }

    @Override
    public void projectOpened() {
        if (applicationComponent.isUpdated() && !applicationComponent.isUpdateNotificationShown()) {
            IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("com.kalessil.phpStorm.phpInspectionsEA"));
            if (null == plugin) {
                return;
            }

            final NotificationGroup group = new NotificationGroup(plugin.getName(), NotificationDisplayType.STICKY_BALLOON, true);
            ApplicationManager.getApplication().invokeLater(() -> {
                    final String pluginName = plugin.getName();
                    /* release notes notification */
                    Notifications.Bus.notify(group.createNotification(
                            String.format("<b>%s</b> update v%s", pluginName, plugin.getVersion()),
                            plugin.getChangeNotes(),
                            NotificationType.INFORMATION,
                            NotificationListener.URL_OPENING_LISTENER
                    ));
                    /* JS Inspections (EA Extended) announcements */
                    Notifications.Bus.notify(group.createNotification(
                            String.format("<b>%s</b>: important", pluginName),
                            "Big news folks, new crowd-funding campaign starts 15 January 2020. " +
                                "It'll fund JS Inspections (EA Extended) creation, and we need your support: when campaign starts, please share it in social networks. " +
                                "Preview is available <a href='https://www.indiegogo.com/project/preview/a98db068'>here</a>!",
                            NotificationType.INFORMATION,
                            NotificationListener.URL_OPENING_LISTENER
                    ));
                },
                ModalityState.NON_MODAL
            );

            applicationComponent.setUpdateNotificationShown(true);
        }
    }

    @Override
    public void projectClosed() {}
}
