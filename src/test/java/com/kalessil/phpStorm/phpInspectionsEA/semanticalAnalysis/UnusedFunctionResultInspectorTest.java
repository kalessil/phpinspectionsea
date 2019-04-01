package com.kalessil.phpStorm.phpInspectionsEA.semanticalAnalysis;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.UnusedFunctionResultInspector;

public class UnusedFunctionResultInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final UnusedFunctionResultInspector inspector = new UnusedFunctionResultInspector();
        inspector.REPORT_ONLY_SCALARS                 = true;
        inspector.REPORT_MIXED_TYPE                   = true;
        inspector.REPORT_FLUENT_INTERFACES            = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/semanticalAnalysis/unused-function-result.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsNonFluentPatterns() {
        final UnusedFunctionResultInspector inspector = new UnusedFunctionResultInspector();
        inspector.REPORT_ONLY_SCALARS                 = false;
        inspector.REPORT_MIXED_TYPE                   = false;
        inspector.REPORT_FLUENT_INTERFACES            = false;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/semanticalAnalysis/unused-function-result.non-fluent.php");
        myFixture.testHighlighting(true, false, true);
    }
}

