package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.PluginId;
import com.kalessil.phpStorm.phpInspectionsEA.license.LicenseService;
import com.kalessil.phpStorm.phpInspectionsEA.utils.analytics.AnalyticsUtil;
import com.wyday.turboactivate.TurboActivate;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.jetbrains.annotations.NotNull;

public class EAApplicationComponent implements ApplicationComponent {
    private boolean updated;
    private boolean updateNotificationShown;

    private IdeaPluginDescriptor plugin;
    private TurboActivate limelm;
    private LicenseService licenseService;

    @NotNull
    public static EAApplicationComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(EAApplicationComponent.class);
    }

    /* TODO: separate component */
    private void initLicensing() {
        this.licenseService = new LicenseService();
        /* Headless mode: let allow execution from command line for now */
        if (this.licenseService.shouldCheckPluginLicense()) {
            try {
                this.limelm = this.licenseService.getLicenseClient();
            } catch (Throwable licensingIntegrationFailure) {
                final String pluginName       = this.plugin.getName();
                final NotificationGroup group = new NotificationGroup(pluginName, NotificationDisplayType.STICKY_BALLOON, true);
                Notifications.Bus.notify(
                    group.createNotification(
                        "<b>" + pluginName + "</b> license",
                        "Failed to initialize licensing sub-system: " + licensingIntegrationFailure.toString(),
                        NotificationType.WARNING,
                        NotificationListener.URL_OPENING_LISTENER
                    )
                );
            }
        }
    }

    @Override
    public void initComponent() {
        this.plugin = PluginManager.getPlugin(PluginId.getId("com.kalessil.phpStorm.phpInspectionsEA"));
        if (null == plugin) {
            return;
        }

        final EASettings settings = EASettings.getInstance();

        /* collect installation events (anonymous) */
        this.updated = !this.plugin.getVersion().equals(settings.getVersion());
        if (this.updated) {
            settings.setVersion(this.plugin.getVersion());
            AnalyticsUtil.registerPluginEvent(settings, "install", settings.getOldestVersion());
        }
        AnalyticsUtil.registerPluginEvent(settings, "run", settings.getOldestVersion());

        /* collect exceptions */
        final FileAppender appender = new FileAppender() {
            @Override
            public void append(@NotNull LoggingEvent event) {
                final ThrowableInformation exceptionDetails = event.getThrowableInformation();
                if (exceptionDetails != null) {
                    AnalyticsUtil.registerLoggedException(
                        settings.getVersion(),
                        settings.getUuid(),
                        exceptionDetails.getThrowable()
                    );
                }
            }
        };
        appender.setName("ea-exceptions-tracker");
        Logger.getRootLogger().addAppender(appender);

        this.initLicensing();
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "EAApplicationComponent";
    }

    boolean isUpdated() {
        return this.updated;
    }

    boolean isUpdateNotificationShown() {
        return this.updateNotificationShown;
    }

    void setUpdateNotificationShown() {
        this.updateNotificationShown = true;
    }
}
