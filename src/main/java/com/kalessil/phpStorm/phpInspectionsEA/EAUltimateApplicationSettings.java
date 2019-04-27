package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

@State(name = "EAUltimateApplicationSettings", storages = @Storage("$APP_CONFIG$/php-inspections-ea-ultimate.xml"))
public class EAUltimateApplicationSettings implements PersistentStateComponent<Element> {
    private String versionOldest;
    private String version;
    private String uuid;

    private static EAUltimateApplicationSettings instance;

    @NotNull
    public static EAUltimateApplicationSettings getInstance() {
        return instance == null
                ? instance = ServiceManager.getService(EAUltimateApplicationSettings.class)
                : instance;
    }

    @Nullable
    @Override
    public Element getState() {
        final Element element = new Element("EAUltimateApplicationSettings");

        if (this.versionOldest != null) {
            element.setAttribute("version", this.version);
        }
        if (this.versionOldest != null) {
            element.setAttribute("versionOldest", this.versionOldest);
        }
        if (this.uuid != null) {
            element.setAttribute("uuid", this.uuid);
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
}
