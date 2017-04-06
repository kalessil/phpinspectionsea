package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.ForeachSourceInspector;

final public class ForeachSourceInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/foreach/foreach-source.php");
        myFixture.enableInspections(ForeachSourceInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/foreach/foreach-source-false-positives.setup.php");
        myFixture.configureByFile("fixtures/foreach/foreach-source-false-positives.php");
        myFixture.enableInspections(ForeachSourceInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}