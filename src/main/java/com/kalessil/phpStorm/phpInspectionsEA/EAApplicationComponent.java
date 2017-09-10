package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.PluginId;
import com.kalessil.phpStorm.phpInspectionsEA.utils.analytics.AnalyticsUtil;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.jetbrains.annotations.NotNull;

public class EAApplicationComponent implements ApplicationComponent {
    private boolean updated;
    private boolean updateNotificationShown;

    @NotNull
    public static EAApplicationComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(EAApplicationComponent.class);
    }

    @Override
    public void initComponent() {
        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("com.kalessil.phpStorm.phpInspectionsEA"));
        if (null == plugin) {
            return;
        }

        final EASettings settings = EASettings.getInstance();

        /* collect installation events (anonymous) */
        this.updated = !plugin.getVersion().equals(settings.getVersion());
        if (this.updated) {
            settings.setVersion(plugin.getVersion());
            AnalyticsUtil.registerPluginEvent(settings, "install", settings.getOldestVersion());
        }
        AnalyticsUtil.registerPluginEvent(settings, "run", settings.getOldestVersion());

        /* collect exceptions */
        final FileAppender appender = new FileAppender() {
            @Override
            public void append(@NotNull LoggingEvent event) {
                final ThrowableInformation exceptionDetails = event.getThrowableInformation();
                if (exceptionDetails != null) {
                    AnalyticsUtil.registerPluginException(settings, exceptionDetails.getThrowable());
                }
            }
        };
        appender.setName("ea-exceptions-tracker");
        Logger.getRootLogger().addAppender(appender);
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

    void setUpdateNotificationShown(boolean shown) {
        this.updateNotificationShown = shown;
    }
}
