package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@State(name = "EAUltimateSettings", storages = @Storage(file = "$APP_CONFIG$/ea_ultimate.xml"))
public class EAUltimateSettings implements PersistentStateComponent<Element> {
    private ComparisonStyle comparisonStyle;
    private String sendCrashReports;
    private String sendVersionInformation;

    private String versionOldest;
    private String version;
    private String uuid;

    public static EAUltimateSettings getInstance() {
        return ServiceManager.getService(EAUltimateSettings.class);
    }

    @Nullable
    @Override
    public Element getState() {
        final Element element = new Element("EAUltimateSettings");

        if (this.versionOldest != null) {
            element.setAttribute("version", this.version);
        }
        if (this.versionOldest != null) {
            element.setAttribute("versionOldest", this.versionOldest);
        }
        if (this.uuid != null) {
            element.setAttribute("uuid", this.uuid);
        }
        if (this.sendCrashReports != null) {
            element.setAttribute("sendCrashReports", this.sendCrashReports);
        }
        if (this.sendVersionInformation != null) {
            element.setAttribute("sendVersionInformation", this.sendVersionInformation);
        }
        if (this.comparisonStyle != null) {
            element.setAttribute("comparisonStyle", this.comparisonStyle.getValue());
        }

        return element;
    }

    @Override
    public void loadState(Element element) {
        /* version information */
        final String versionValue = element.getAttributeValue("version");
        if (versionValue != null) {
            this.version = versionValue;
        }
        final String versionOldestValue = element.getAttributeValue("versionOldest");
        this.versionOldest              = (versionOldestValue == null ? versionValue : versionOldestValue);

        /* anonymous identity information */
        final String uuidValue = element.getAttributeValue("uuid");
        this.uuid              = (uuidValue == null ? UUID.randomUUID().toString() : uuidValue);

        /* misc. statistics collection */
        final String sendCrashReportsValue       = element.getAttributeValue("sendCrashReports");
        this.sendCrashReports                    = sendCrashReportsValue == null ? "true" : sendCrashReportsValue;
        final String sendVersionInformationValue = element.getAttributeValue("sendVersionInformation");
        this.sendVersionInformation              = sendVersionInformationValue == null ? "true" : sendVersionInformationValue;

        /* comparison style */
        final String comparisonStyleValue = element.getAttributeValue("comparisonStyle");
        this.comparisonStyle              = comparisonStyleValue == null || comparisonStyleValue.equals(ComparisonStyle.REGULAR.getValue())
                                                ? ComparisonStyle.REGULAR
                                                : ComparisonStyle.YODA;
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

    public boolean getSendCrashReports() {
        return this.sendCrashReports != null && this.sendCrashReports.equals("true");
    }
    public void setSendCrashReports(boolean value) {
        this.sendCrashReports = (value ? "true" : "false");
    }

    public boolean getSendVersionInformation() {
        return this.sendVersionInformation != null && this.sendVersionInformation.equals("true");
    }
    public void setSendVersionInformation(boolean value) {
        this.sendVersionInformation = (value ? "true" : "false");
    }

    public void setComparisonStyle(final ComparisonStyle comparisonStyleValue) {
        this.comparisonStyle = comparisonStyleValue;
    }

    public ComparisonStyle getComparisonStyle() {
        return this.comparisonStyle;
    }
}
