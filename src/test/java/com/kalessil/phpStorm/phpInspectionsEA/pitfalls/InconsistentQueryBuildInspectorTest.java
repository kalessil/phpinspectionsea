package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.InconsistentQueryBuildInspector;

final public class InconsistentQueryBuildInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/inconsistent-http_build_query.php");
        myFixture.enableInspections(InconsistentQueryBuildInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
