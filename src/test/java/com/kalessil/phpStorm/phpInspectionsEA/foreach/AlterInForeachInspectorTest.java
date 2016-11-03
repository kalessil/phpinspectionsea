package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.AlterInForeachInspector;

public class AlterInForeachInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        AlterInForeachInspector inspector    = new AlterInForeachInspector();
        inspector.SUGGEST_USING_VALUE_BY_REF = true;

        myFixture.configureByFile("fixtures/foreach/alter-in-foreach.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/foreach/alter-in-foreach-false-positives.php");
        myFixture.enableInspections(AlterInForeachInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
