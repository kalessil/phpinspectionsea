package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.OnlyWritesOnParameterInspector;

final public class OnlyWritesOnParameterInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new OnlyWritesOnParameterInspector());
        myFixture.configureByFile("testData/fixtures/deadCode/parameters-writes-only.php");
        myFixture.configureByFile("testData/fixtures/deadCode/parameters-writes-only.suppression.php");
        myFixture.testHighlighting(true, false, true);
    }
}
