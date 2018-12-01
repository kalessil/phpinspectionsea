package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.EmptyClassInspector;

final public class EmptyClassInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new EmptyClassInspector());
        myFixture.configureByFile("testData/fixtures/classes/empty-class.php");
        myFixture.testHighlighting(true, false, true);
    }
}
