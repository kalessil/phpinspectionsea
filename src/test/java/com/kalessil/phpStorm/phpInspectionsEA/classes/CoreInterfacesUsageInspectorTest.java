package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.CoreInterfacesUsageInspector;

final public class CoreInterfacesUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CoreInterfacesUsageInspector());
        myFixture.configureByFile("testData/fixtures/classes/core-interfaces-usage.php");
        myFixture.testHighlighting(true, false, true);
    }
}
