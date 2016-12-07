package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.RealpathInSteamContextInspector;

final public class RealpathInSteamContextInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/realpath-in-stream-context.php");
        myFixture.enableInspections(RealpathInSteamContextInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
