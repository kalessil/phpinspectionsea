package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.*;
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
            IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("com.kalessil.phpStorm.phpInspectionsUltimate"));
            if (null == plugin) {
                return;
            }

            final String pluginName       = plugin.getName();
            final NotificationGroup group = new NotificationGroup(pluginName, NotificationDisplayType.STICKY_BALLOON, true);
            Notifications.Bus.notify(
                group.createNotification(
                    "<b>" + pluginName + "</b> update v" + plugin.getVersion(),
                    plugin.getChangeNotes(),
                    NotificationType.INFORMATION,
                    NotificationListener.URL_OPENING_LISTENER
                )
            );

            applicationComponent.setUpdateNotificationShown();
        }
    }

    @Override
    public void projectClosed() {}
}
