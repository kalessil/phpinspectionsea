package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.PrintfScanfArgumentsInspector;

final public class PrintfScanfArgumentsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/printf-scanf.php");
        myFixture.enableInspections(PrintfScanfArgumentsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
