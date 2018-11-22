package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.ForeachSourceInspector;

final public class ForeachSourceInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final ForeachSourceInspector inspector = new ForeachSourceInspector();
        inspector.REPORT_MIXED_TYPES           = true;
        inspector.REPORT_UNRECOGNIZED_TYPES    = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/foreach/foreach-source.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testFalsePositives() {
        myFixture.enableInspections(new ForeachSourceInspector());
        myFixture.configureByFile("testData/fixtures/foreach/foreach-source-false-positives.setup.php");
        myFixture.configureByFile("testData/fixtures/foreach/foreach-source-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}
