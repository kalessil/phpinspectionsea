package com.kalessil.phpStorm.phpInspectionsEA.phpUnit;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.MockingFinalClassesInspector;

final public class MockingFinalClassesInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MockingFinalClassesInspector());
        myFixture.configureByFile("fixtures/pitfalls/mocking-final-classes.php");
        myFixture.testHighlighting(true, false, true);
    }
}
