package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.AlterInForeachInspector;

final public class AlterInForeachInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final AlterInForeachInspector inspector = new AlterInForeachInspector();
        inspector.SUGGEST_USING_VALUE_BY_REF    = true;
        myFixture.configureByFile("testData/fixtures/foreach/alter-in-foreach.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.enableInspections(new AlterInForeachInspector());
        myFixture.configureByFile("testData/fixtures/foreach/alter-in-foreach-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}
