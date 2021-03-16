package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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
            final PluginId pluginId           = PluginId.getId("com.kalessil.phpStorm.phpInspectionsEA");
            final IdeaPluginDescriptor plugin = Arrays.stream(PluginManagerCore.getPlugins())
                    .filter(descriptor -> pluginId.equals(descriptor.getPluginId()))
                    .findFirst( )
                    .orElse(null);
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
                },
                ModalityState.NON_MODAL
            );

            applicationComponent.setUpdateNotificationShown(true);
        }
    }

    @Override
    public void projectClosed() {}
}
