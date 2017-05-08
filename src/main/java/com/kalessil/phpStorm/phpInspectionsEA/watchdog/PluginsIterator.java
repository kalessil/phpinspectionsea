package com.kalessil.phpStorm.phpInspectionsEA.watchdog;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

final public class PluginsIterator implements Iterator {
    @NotNull
    final private List<Pair<String, String>> pluginsIds = new ArrayList<>();

    @NotNull
    final private Iterator<Pair<String, String>> pluginsIterator;

    PluginsIterator() {
        for (final IdeaPluginDescriptor plugin : PluginManager.getPlugins()) {
            final PluginId pluginId = plugin.getPluginId();
            final String stringId   = pluginId.getIdString();
            if (StringUtils.countMatches(stringId, ".") > 1) {
                this.pluginsIds.add(Pair.create(stringId, plugin.getName()));
            }
        }

        this.pluginsIterator = this.pluginsIds.iterator();
    }

    @Override
    public boolean hasNext() {
        return this.pluginsIterator.hasNext();
    }

    @Override
    public Pair<String, String> next() {
        return this.pluginsIterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
