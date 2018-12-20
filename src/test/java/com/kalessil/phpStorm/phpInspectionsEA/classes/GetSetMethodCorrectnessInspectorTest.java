package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.GetSetMethodCorrectnessInspector;

final public class GetSetMethodCorrectnessInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new GetSetMethodCorrectnessInspector());
        myFixture.configureByFile("testData/fixtures/classes/get-set-method-correctness.php");
        myFixture.testHighlighting(true, false, true);
    }
}
