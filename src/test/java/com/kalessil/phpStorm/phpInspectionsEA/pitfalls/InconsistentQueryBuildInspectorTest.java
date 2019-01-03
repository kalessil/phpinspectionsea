package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.InconsistentQueryBuildInspector;

final public class InconsistentQueryBuildInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new InconsistentQueryBuildInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/inconsistent-http_build_query.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/pitfalls/inconsistent-http_build_query.fixed.php");
    }
}
