package com.kalessil.phpStorm.phpInspectionsEA.phpUnit;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.UnusedMockInspector;

final public class UnusedMockInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsPatterns() {
        myFixture.enableInspections(new UnusedMockInspector());
        myFixture.configureByFile("testData/fixtures/phpUnit/unused-mocks.php");
        myFixture.testHighlighting(true, false, true);
    }
}
