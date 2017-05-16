package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.CompactArgumentsInspector;

final public class CompactArgumentsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CompactArgumentsInspector());

        myFixture.configureByFile("fixtures/api/compact-arguments-existence.php");
        myFixture.testHighlighting(true, false, true);
    }
}