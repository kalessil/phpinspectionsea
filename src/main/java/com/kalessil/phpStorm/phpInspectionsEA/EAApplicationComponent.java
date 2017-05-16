package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.PluginId;
import com.kalessil.phpStorm.phpInspectionsEA.utils.analytics.AnalyticsUtil;
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
        this.updated              = !plugin.getVersion().equals(settings.getVersion());
        if (this.updated) {
            settings.setVersion(plugin.getVersion());
            AnalyticsUtil.registerPluginEvent(settings, "install", settings.getOldestVersion());
        }

        AnalyticsUtil.registerPluginEvent(settings, "run", settings.getOldestVersion());
    }

    @Override
    public void disposeComponent() {
    }

/*    public void MonitorPerformance() {
        final ThreadMXBean threadManager = ManagementFactory.getThreadMXBean();
        for (final long threadId : threadManager.getAllThreadIds()) {
            final long threadTimeOccupied = threadManager.getThreadUserTime(threadId);
            final long time               = threadTimeOccupied == -1 ? -1 : threadTimeOccupied / 1000000;

            final ThreadInfo info                = threadManager.getThreadInfo(threadId, Byte.MAX_VALUE);
            final StackTraceElement[] stacktrace = info.getStackTrace();
            if (null != stacktrace && stacktrace.length > 0) {
                final StackTraceElement topElement = stacktrace[stacktrace.length - 1];
                System.out.println(String.format(
                        "#%s: ut %s ms; %s; %s",
                        info.getThreadName() + "#" + threadId,
                        time,
                        topElement.getClassName() + "::" + topElement.getMethodName(),
                        topElement.getFileName() + ":" + topElement.getLineNumber()
                ));
            }
        }
    }*/

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
