package com.kalessil.phpStorm.phpInspectionsEA.watchdog;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

final public class PluginsIterator implements Iterator {
    @NotNull
    final private Set<String> pluginsIds = new HashSet<>();

    @NotNull
    final private Iterator<String> pluginsIterator;

    PluginsIterator() {
        for (final IdeaPluginDescriptor plugin : PluginManager.getPlugins()) {
            final String stringId = plugin.getPluginId().getIdString();
            if (StringUtils.countMatches(stringId, ".") > 1) {
                this.pluginsIds.add(stringId);
            }
        }

        this.pluginsIterator = this.pluginsIds.iterator();
    }

    @Override
    public boolean hasNext() {
        return this.pluginsIterator.hasNext();
    }

    @Override
    public String next() {
        return this.pluginsIterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
