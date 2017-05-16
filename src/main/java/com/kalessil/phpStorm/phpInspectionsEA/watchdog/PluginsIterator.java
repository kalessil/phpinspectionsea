package com.kalessil.phpStorm.phpInspectionsEA.watchdog;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final public class PluginsIterator implements Iterator {
    private static List<String> jetBrainsPlugins = new ArrayList<>();
    static {
        jetBrainsPlugins.add("com.jetbrains.php");
    }

    @NotNull
    final private Iterator<Pair<String, String>> pluginsIterator;

    PluginsIterator() {
        final List<Pair<String, String>> corePlugins = new ArrayList<>();
        final List<Pair<String, String>> plugins     = new ArrayList<>();
        for (final IdeaPluginDescriptor plugin : PluginManager.getPlugins()) {
            final PluginId pluginId = plugin.getPluginId();
            final String stringId   = pluginId.getIdString();
            if (StringUtils.countMatches(stringId, ".") > 1) {
                /* core plugins needs to be pushed into the end and are basically fallbacks */
                if (jetBrainsPlugins.contains(stringId)) {
                    corePlugins.add(Pair.create(stringId, plugin.getName()));
                } else {
                    plugins.add(Pair.create(stringId, plugin.getName()));
                }
            }
        }
        plugins.addAll(corePlugins);
        corePlugins.clear();

        /* TODO: find this in stack traces directly */
        // org.intellij.plugins.*.
        // org.intellij.lang.*.
        // com.intellij.lang.*.

        this.pluginsIterator = plugins.iterator();
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
