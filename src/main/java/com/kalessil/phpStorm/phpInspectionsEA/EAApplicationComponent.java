package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.PluginId;
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

        EASettings settings = EASettings.getInstance();
        updated = !plugin.getVersion().equals(settings.getVersion());
        if (updated) {
            settings.setVersion(plugin.getVersion());
        }
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
        return updated;
    }

    boolean isUpdateNotificationShown() {
        return updateNotificationShown;
    }

    void setUpdateNotificationShown(boolean shown) {
        this.updateNotificationShown = shown;
    }
}
