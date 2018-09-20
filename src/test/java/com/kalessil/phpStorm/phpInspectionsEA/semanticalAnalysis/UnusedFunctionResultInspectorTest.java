package com.kalessil.phpStorm.phpInspectionsEA.semanticalAnalysis;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.UnusedFunctionResultInspector;

public class UnusedFunctionResultInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnusedFunctionResultInspector());
        myFixture.configureByFile("testData/fixtures/semanticalAnalysis/unused-function-result.php");
        myFixture.testHighlighting(true, false, true);
    }
}

