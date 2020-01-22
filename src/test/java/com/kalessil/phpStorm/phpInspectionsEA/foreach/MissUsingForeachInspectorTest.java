package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.MissUsingForeachInspector;

final public class MissUsingForeachInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MissUsingForeachInspector());
        myFixture.configureByFile("testData/fixtures/foreach/misused-foreach.php");
        myFixture.testHighlighting(true, false, true);
    }
}