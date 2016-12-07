package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.PrintfScanfArgumentsInspector;

final public class PrintfScanfArgumentsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/printf-scanf.php");
        myFixture.enableInspections(PrintfScanfArgumentsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
