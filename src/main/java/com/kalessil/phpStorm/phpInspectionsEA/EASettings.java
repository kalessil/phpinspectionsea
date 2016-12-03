package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "EASettings",
    storages = @Storage(id = "other", file = "$APP_CONFIG$/ea_extended.xml")
)
public class EASettings implements PersistentStateComponent<Element> {
    private String version;

    public static EASettings getInstance() {
        return ServiceManager.getService(EASettings.class);
    }

    @Nullable
    @Override
    public Element getState() {
        final Element element = new Element("EASettings");
        element.setAttribute("version", version);

        return element;
    }

    @Override
    public void loadState(Element element) {
        String value = element.getAttributeValue("version");
        if (value != null) version = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(@NotNull String version) {
        this.version = version;
    }
}
