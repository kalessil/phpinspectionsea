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

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class EAUltimateReleaseNotesAnnouncerComponent implements ProjectComponent {
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
        return "EAUltimateReleaseNotesAnnouncerComponent";
    }

    @Override
    public void projectOpened() {
        if (applicationComponent.isUpdated() && !applicationComponent.isUpdateNotificationShown()) {
            final IdeaPluginDescriptor ultimatePlugin = PluginManager.getPlugin(PluginId.getId("com.kalessil.phpStorm.phpInspectionsUltimate"));
            if (ultimatePlugin == null) {
                return;
            }

            final NotificationGroup group = EAUltimateApplicationComponent.getInstance().getNotificationGroup();
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                final String pluginName = ultimatePlugin.getName();
                /* release notes notification */
                Notifications.Bus.notify(group.createNotification(
                        String.format("<b>%s</b> update v%s", pluginName, ultimatePlugin.getVersion()),
                        ultimatePlugin.getChangeNotes(),
                        NotificationType.INFORMATION,
                        NotificationListener.URL_OPENING_LISTENER
                ));
                /* marketplace migration and JS Inspections (EA Extended) announcements */
                Notifications.Bus.notify(group.createNotification(
                        String.format("<b>%s</b>: important", pluginName),
                        "Two great news folks! First, new crowd-funding campaign starts 15 January 2020. " +
                                "It'll fund JS Inspections (EA Extended) creation, and we need your support: when campaign starts, please share it in social networks. " +
                                "Preview is available <a href='https://www.indiegogo.com/project/preview/a98db068'>here</a>!<br/>" +
                                "Second, we are migrating to JetBrains Plugin Marketplace in Q1 2020. " +
                                "More details follows, but in nutshell payments and license management will be part of your JetBrains account. " +
                                "Please follow me on Twitter (@kalessil) for further updates and your feedback.<br/>",
                        NotificationType.INFORMATION,
                        NotificationListener.URL_OPENING_LISTENER
                ));
            });

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
