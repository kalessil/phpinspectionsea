package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@State (name = "EASettings", storages = @Storage (file = "$APP_CONFIG$/ea_extended.xml"))
public class EASettings implements PersistentStateComponent<Element> {
    private String sendCrashReports;
    private String sendVersionInformation;
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

        if (version != null) {
            element.setAttribute("version", version);
        }

        if (versionOldest != null) {
            element.setAttribute("versionOldest", versionOldest);
        }

        if (uuid != null) {
            element.setAttribute("uuid", uuid);
        }

        if (sendCrashReports != null) {
            element.setAttribute("sendCrashReports", sendCrashReports);
        }

        if (sendVersionInformation != null) {
            element.setAttribute("sendVersionInformation", sendVersionInformation);
        }

        return element;
    }

    @Override
    public void loadState(@NotNull Element element) {
        /* version information */
        final String versionValue = element.getAttributeValue("version");

        if (versionValue != null) {
            version = versionValue;
        }

        final String versionOldestValue = element.getAttributeValue("versionOldest");
        versionOldest = (versionOldestValue == null ? versionValue : versionOldestValue);

        /* anonymous identity information */
        final String uuidValue = element.getAttributeValue("uuid");
        uuid = (uuidValue == null ? UUID.randomUUID().toString() : uuidValue);

        /* misc. statistics collection */
        final String sendCrashReportsValue = element.getAttributeValue("sendCrashReports");
        sendCrashReports = sendCrashReportsValue == null ? "true" : sendCrashReportsValue;

        final String sendVersionInformationValue = element.getAttributeValue("sendVersionInformation");
        sendVersionInformation = sendVersionInformationValue == null ? "true" : sendVersionInformationValue;
    }

    public void setVersion(@NotNull String version) {
        this.version = version;
        versionOldest = (versionOldest == null ? version : versionOldest);
    }

    public String getVersion() {
        return version;
    }

    public String getUuid() {
        return uuid;
    }

    String getOldestVersion() {
        return versionOldest;
    }

    boolean getSendCrashReports() {
        return "true".equals(sendCrashReports);
    }

    void setSendCrashReports(boolean value) {
        sendCrashReports = (value ? "true" : "false");
    }

    boolean getSendVersionInformation() {
        return "true".equals(sendVersionInformation);
    }

    void setSendVersionInformation(boolean value) {
        sendVersionInformation = (value ? "true" : "false");
    }
}
