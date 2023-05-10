package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiPlatformUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EAStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        final IdeaPluginDescriptor plugin = OpenapiPlatformUtil.getPluginById("com.kalessil.phpStorm.phpInspectionsEA");
        if (null == plugin) {
            return;
        }

        final EASettings settings = EASettings.getInstance();

        /* dump new plugin version */
        final String pluginVersion = plugin.getVersion();

        if (!pluginVersion.equals(settings.getVersion())) {
            settings.setVersion(pluginVersion);

            final String pluginName = plugin.getName();
            final String popupTitle = String.format("<b>%s</b> update v%s", pluginName, pluginVersion);
            final String popupContent = Optional.ofNullable(plugin.getChangeNotes()).orElse("");

            NotificationGroupManager.getInstance()
                    .getNotificationGroup("Php Inspections (EA Extended) Update Notification")
                    .createNotification(popupContent, NotificationType.INFORMATION)
                    .setTitle(popupTitle)
                    .notify(project);
        }
    }
}
