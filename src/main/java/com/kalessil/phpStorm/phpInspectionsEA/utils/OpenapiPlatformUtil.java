package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class OpenapiPlatformUtil {
    @Nullable
    public static IdeaPluginDescriptor getPluginById(@NotNull String id) {
        final PluginId pluginId = PluginId.getId(id);
        return Arrays.stream(PluginManagerCore.getPlugins())
                .filter(descriptor -> pluginId.equals(descriptor.getPluginId()))
                .findFirst()
                .orElse(null);
    }
}
