package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.ExposingInternalClassesInspector;

final public class ExposingInternalClassesInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsInconsistentGetsSetsPatterns() {
        myFixture.enableInspections(new ExposingInternalClassesInspector());
        myFixture.configureByFile("testData/fixtures/classes/exposing-internal-classes.php");
        myFixture.testHighlighting(true, false, true);
    }
}