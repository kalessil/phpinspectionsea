package com.kalessil.phpStorm.phpInspectionsEA.license;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import org.jetbrains.annotations.NotNull;

final public class PurchaseLicenseAction {
    public void perform(@NotNull IdeaPluginDescriptor plugin) {
        BrowserUtil.browse("https://kalessil.github.io/php-inspections-ultimate.html");
    }
}