package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.RealpathOnRelativePathsInspector;

public class RealpathOnRelativePathsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/realpath-on-relative-paths.php");
        myFixture.enableInspections(RealpathOnRelativePathsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
