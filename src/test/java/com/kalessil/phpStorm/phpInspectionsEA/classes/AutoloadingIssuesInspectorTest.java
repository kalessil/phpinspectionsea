package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.AutoloadingIssuesInspector;

final public class AutoloadingIssuesInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsPatterns() {
        myFixture.enableInspections(new AutoloadingIssuesInspector());

        myFixture.configureByFile("fixtures/classes/brokenAutoloading/case1.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.configureByFile("fixtures/classes/brokenAutoloading/case1.class.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.enableInspections(new AutoloadingIssuesInspector());

        myFixture.configureByFile("fixtures/classes/brokenAutoloading/ValidCase.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.configureByFile("fixtures/classes/brokenAutoloading/ValidCase.class.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.configureByFile("fixtures/classes/brokenAutoloading/ValidCase.multiple.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.configureByFile("fixtures/classes/brokenAutoloading/0000_00_00_000000_Version000000_.php");
        myFixture.testHighlighting(true, false, true);
    }
}