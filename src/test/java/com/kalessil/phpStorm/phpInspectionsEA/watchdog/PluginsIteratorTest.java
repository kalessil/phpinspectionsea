package com.kalessil.phpStorm.phpInspectionsEA.watchdog;

import com.google.common.collect.Sets;
import com.intellij.openapi.util.Pair;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

import java.util.Set;

final public class PluginsIteratorTest extends CodeInsightFixtureTestCase {
    public void testPluginsIterator() {
        final Set<Pair<String, String>> plugins = Sets.newHashSet(new PluginsIterator());

        boolean hasPhpPlugin = false;
        for (final Pair<String, String> pair : plugins) {
            if (pair.getFirst().equals("com.jetbrains.php")) {
                hasPhpPlugin = true;
                break;
            }
        }
        assertTrue(hasPhpPlugin);

        boolean hasEaPlugin = false;
        for (final Pair<String, String> pair : plugins) {
            if (pair.getFirst().equals("com.kalessil.phpStorm.phpInspectionsEA")) {
                hasEaPlugin = true;
                break;
            }
        }
        assertTrue(hasEaPlugin);
    }
}
