package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.InconsistentQueryBuildInspector;

final public class InconsistentQueryBuildInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/inconsistent-http_build_query.php");
        myFixture.enableInspections(InconsistentQueryBuildInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
