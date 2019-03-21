package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.SenselessPropertyInspector;

final public class SenselessPropertyInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsPatterns() {
        myFixture.enableInspections(new SenselessPropertyInspector());
        myFixture.configureByFile("testData/fixtures/classes/senseless-property.php");
        myFixture.testHighlighting(true, false, true);
    }
}
