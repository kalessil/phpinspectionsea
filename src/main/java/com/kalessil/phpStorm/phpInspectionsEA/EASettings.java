package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@State(
    name = "EASettings",
    storages = @Storage(id = "other", file = "$APP_CONFIG$/ea_extended.xml")
)
public class EASettings implements PersistentStateComponent<Element> {
    private String versionOldest;
    private String version;
    private String uuid;

    public static EASettings getInstance() {
        return ServiceManager.getService(EASettings.class);
    }

    @Nullable
    @Override
    public Element getState() {
        final Element element = new Element("EASettings");
        element.setAttribute("version", this.version);
        element.setAttribute("versionOldest", this.versionOldest);
        element.setAttribute("uuid", this.uuid);

        return element;
    }

    @Override
    public void loadState(Element element) {
        final String versionValue = element.getAttributeValue("version");
        if (versionValue != null) {
            this.version = versionValue;
        }

        final String versionOldestValue = element.getAttributeValue("versionOldest");
        this.versionOldest              = (null == versionOldestValue ? versionValue : versionOldestValue);

        final String uuidValue = element.getAttributeValue("uuid");
        this.uuid              = (null == uuidValue ? UUID.randomUUID().toString() : uuidValue);
    }

    public void setVersion(@NotNull String version) {
        this.version       = version;
        this.versionOldest = (null == this.versionOldest ? version : this.versionOldest);
    }

    public String getVersion() {
        return this.version;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getOldestVersion() {
        return this.versionOldest;
    }
}
