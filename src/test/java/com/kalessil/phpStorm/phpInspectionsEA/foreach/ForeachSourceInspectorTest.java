package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.ForeachSourceInspector;

final public class ForeachSourceInspectorTest extends CodeInsightFixtureTestCase {
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