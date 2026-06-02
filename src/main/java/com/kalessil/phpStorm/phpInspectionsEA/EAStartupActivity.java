package com.kalessil.phpStorm.phpInspectionsEA;

//import com.intellij.ide.plugins.IdeaPluginDescriptor;
//import com.intellij.ide.plugins.PluginManager;
//import com.intellij.notification.NotificationGroupManager;
//import com.intellij.notification.NotificationType;
//import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class EAStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
//        Temporarily deactivated because PluginManager.getPlugin and its alternatives are designated as private APIs.
//        final PluginId pluginId           = PluginId.getId("com.kalessil.phpStorm.phpInspectionsEA");
//        final IdeaPluginDescriptor plugin = PluginManager.getPlugin(pluginId);
//        if (plugin != null) {
//            final EASettings settings  = EASettings.getInstance();
//            final String pluginVersion = plugin.getVersion();
//            if (! pluginVersion.equals(settings.getVersion())) {
//                settings.setVersion(pluginVersion);
//
//                final String popupTitle  = String.format("<b>%s</b> update v%s", plugin.getName(), pluginVersion);
//                final String popupContent = Optional.ofNullable(plugin.getChangeNotes()).orElse("");
//                NotificationGroupManager.getInstance()
//                        .getNotificationGroup("Php Inspections (EA Extended) Update Notification")
//                        .createNotification(popupContent, NotificationType.INFORMATION)
//                        .setTitle(popupTitle)
//                        .notify(project);
//            }
//        }
    }
}
