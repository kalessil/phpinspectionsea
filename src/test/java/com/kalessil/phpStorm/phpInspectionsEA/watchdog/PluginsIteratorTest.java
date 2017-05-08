package com.kalessil.phpStorm.phpInspectionsEA.watchdog;

import com.google.common.collect.Sets;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

import java.util.Set;

final public class PluginsIteratorTest extends CodeInsightFixtureTestCase {
    public void testPluginsIterator() {
        final Set<String> plugins = Sets.newHashSet(new PluginsIterator());
        assertFalse(plugins.isEmpty());

        assertTrue(plugins.contains("com.jetbrains.php"));
        assertTrue(plugins.contains("com.kalessil.phpStorm.phpInspectionsEA"));
    }
}
